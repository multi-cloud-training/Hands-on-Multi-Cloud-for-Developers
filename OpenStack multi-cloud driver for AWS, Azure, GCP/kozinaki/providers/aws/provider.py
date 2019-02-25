# Copyright (c) 2016 CompuNova Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import base64
import hashlib
import logging
from Crypto.PublicKey import RSA

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from haikunator import Haikunator
from oslo_config import cfg
from nova.image import glance
from oslo_service import loopingcall
from nova.compute import power_state, task_states

from .config import volume_map, flavor_map
from ..common import BaseProvider


LOG = logging.getLogger(__name__)

haikunator = Haikunator()


POWER_STATE_MAP = {
    0:  power_state.NOSTATE,
    16: power_state.RUNNING,
    32: power_state.NOSTATE,
    48: power_state.SHUTDOWN,
    64: power_state.NOSTATE,
    80: power_state.SHUTDOWN,
    # power_state.PAUSED,
    # power_state.CRASHED,
    # power_state.STATE_MAP,
    # power_state.SUSPENDED,
}


class AWSProvider(BaseProvider):

    def __init__(self):
        super(AWSProvider, self).__init__()
        self.name = 'AWS'
        self.config_name = 'kozinaki_' + self.name
        self.driver = self.get_driver()
        self._mounts = {}

    def get_driver(self):
        config = self.load_config()

        session = boto3.session.Session(
            aws_access_key_id=config['aws_access_key_id'],
            aws_secret_access_key=config['aws_secret_access_key'],
            region_name=config['region']
        )
        return session.resource('ec2')

    def load_config(self):
        """Load config options from nova config file or command line (for example: /etc/nova/nova.conf)

        Sample settings in nova config:
            [kozinaki_EC2]
            user=AKIAJR7NAEIZPWSTFBEQ
            key=zv9zSem8OE+k/axFkPCgZ3z3tLrhvFBaIIa0Ik0j
        """

        provider_opts = [
            cfg.StrOpt('aws_secret_access_key', help='AWS secret key', secret=True),
            cfg.StrOpt('aws_access_key_id', help='AWS access key id', secret=True),
            cfg.StrOpt('region', help='AWS region name'),
        ]

        cfg.CONF.register_opts(provider_opts, self.config_name)
        return cfg.CONF[self.config_name]

    def create_node(self, instance, image_meta, *args, **kwargs):
        # Get info
        image_id = getattr(image_meta.properties, 'os_distro')
        subnet_id = instance.metadata.get('subnet_id')
        flavor_name = instance.flavor['name']

        image_config = {
            'ImageId': image_id,
            'InstanceType': flavor_name,
            'MinCount': 1,
            'MaxCount': 1
        }

        if instance.key_name:
            self._check_keypair(instance.key_name, instance.key_data)
            image_config['KeyName'] = instance.key_name

        if subnet_id:
            image_config.update({'SubnetId': subnet_id})

        aws_instance = self.driver.create_instances(**image_config)[0]

        # Add openstack image uuid to tag
        aws_instance.create_tags(Tags=[{'Key': 'openstack_server_id', 'Value': instance.uuid}])
        instance['metadata']['ec2_id'] = aws_instance.id
        return aws_instance

    def _check_keypair(self, key_name, key_data):
        aws_key_fingerprint = None
        try:
            aws_key_fingerprint = self.driver.KeyPair(key_name).key_fingerprint
        except ClientError as e:
            if 'not exist' not in e.message:
                raise Exception(e)

        if aws_key_fingerprint:
            if aws_key_fingerprint != self._key_to_aws_fingerprint(key_data):
                raise Exception('Local and AWS public key pair with name "{}" has different fingerprints.'
                                'Please set another key name.'.format(key_name))
        else:
            self.driver.import_key_pair(KeyName=key_name, PublicKeyMaterial=key_data)

    def _key_to_aws_fingerprint(self, key_data):
        key = RSA.importKey(key_data)
        key_der = key.publickey().exportKey("DER")
        fp_plain = hashlib.md5(key_der).hexdigest()
        return ':'.join(a + b for a, b in zip(fp_plain[::2], fp_plain[1::2]))

    def list_nodes(self):
        return list(self.driver.instances.all())

    def destroy(self, instance, *args, **kwargs):
        aws_node = self._get_node_by_uuid(instance.uuid)
        if aws_node:
            aws_node.terminate()

    def list_instances(self):
        return list(self.driver.instances.all())

    def list_sizes(self):
        return list(self.driver.images.all())

    def power_on(self, context, instance, network_info, block_device_info=None):
        aws_node = self._get_node_by_uuid(instance.uuid)
        if aws_node:
            aws_node.start()

    def list_instance_uuids(self):
        return [node.id for node in self.list_nodes()]

    def power_off(self, instance, timeout=0, retry_interval=0):
        aws_node = self._get_node_by_uuid(instance.uuid)
        if aws_node:
            aws_node.stop()

    def get_info(self, instance):
        aws_node = self._get_node_by_uuid(instance.uuid)

        if aws_node:
            node_power_state = POWER_STATE_MAP[aws_node.state['Code']]
            node_id = aws_node.id
        else:
            node_power_state = power_state.NOSTATE
            node_id = 0

        node_info = {
            'state':        node_power_state,
            'max_mem_kb':   0,  # '(int) the maximum memory in KBytes allowed',
            'mem_kb':       0,  # '(int) the memory in KBytes used by the instance',
            'num_cpu':      0,  # '(int) the number of virtual CPUs for the instance',
            'cpu_time_ns':  0,  # '(int) the CPU time used in nanoseconds',
            'id':           node_id
        }

        return node_info

    def reboot(self, instance, *args, **kwargs):
        aws_node = self._get_node_by_uuid(instance.uuid)
        if aws_node:
            aws_node.reboot()

    def attach_volume(self, context, connection_info, instance, mountpoint,
                      disk_bus=None, device_type=None, encryption=None):
        """Attach the disk to the instance at mountpoint using info."""
        instance_name = instance['name']
        if instance_name not in self._mounts:
            self._mounts[instance_name] = {}
        self._mounts[instance_name][mountpoint] = connection_info

        volume_id = connection_info['data']['volume_id']
        # ec2 only attaches volumes at /dev/sdf through /dev/sdp
        self.driver.attach_volume(volume_map[volume_id], instance['metadata']['ec2_id'], "/dev/sdn", dry_run=False)

    def detach_volume(self, connection_info, instance, mountpoint,
                      encryption=None):
        """Detach the disk attached to the instance."""
        try:
            del self._mounts[instance['name']][mountpoint]
        except KeyError:
            pass
        volume_id = connection_info['data']['volume_id']
        self.driver.detach_volume(volume_map[volume_id], instance_id=instance['metadata']['ec2_id'],
                                  device="/dev/sdn", force=False, dry_run=False)

    def snapshot(self, context, instance, image_id, update_task_state):
        aws_node = self._get_node_by_uuid(instance.uuid)

        update_task_state(task_state=task_states.IMAGE_UPLOADING, expected_state=task_states.IMAGE_SNAPSHOT)

        if aws_node.state['Name'] == 'running':
            ec2_image = aws_node.create_image(Name=str(image_id),
                                                 Description="Image from OpenStack", NoReboot=False, DryRun=False)
        else:
            # TODO: else case
            LOG.error('Node state: "{}". Must be "running" for snapshot.'.format(aws_node.state['Name']))
            return
        # The instance will be in pending state when it comes up, waiting for it to be in available
        self._wait_for_image_state(ec2_image, "available")

        image_api = glance.get_default_image_service()
        image_ref = glance.generate_image_url(image_id)

        metadata = {'is_public': False,
                    'location': image_ref,
                    'properties': {
                        'kernel_id': instance['kernel_id'],
                        'image_state': 'available',
                        'owner_id': instance['project_id'],
                        'ramdisk_id': instance['ramdisk_id'],
                        'ec2_image_id': ec2_image.id}
                    }
        # TODO: HTTPInternalServerError: 500 Internal Server Error:
        # TODO: The server has either erred or is incapable of performing the requested operation.
        image_api.update(context, image_id, metadata)

    def finish_migration(self, context, migration, instance, disk_info, network_info, image_meta, resize_instance,
                         block_device_info=None, power_on=True):
        LOG.info("***** Calling FINISH MIGRATION *******************")

        aws_node = self._get_node_by_uuid(instance.uuid)

        # EC2 instance needs to be stopped to modify it's attribute. So we stop the instance,
        # modify the instance type in this case, and then restart the instance.
        aws_node.stop()
        self._wait_for_state(instance, aws_node, "stopped", power_state.SHUTDOWN)
        new_instance_type = flavor_map[migration['new_instance_type_id']]
        aws_node.modify_attribute('instanceType', new_instance_type)

    def confirm_migration(self, migration, instance, network_info):
        LOG.info("***** Calling CONFIRM MIGRATION *******************")
        aws_node = self._get_node_by_uuid(instance.uuid)
        aws_node.start()
        self._wait_for_state(instance, aws_node, "running", power_state.RUNNING)

    def _get_node_by_uuid(self, uuid):
        aws_nodes = list(self.driver.instances.filter(Filters=[{'Name': 'tag:openstack_server_id', 'Values': [uuid]}]))
        return aws_nodes[0] if aws_nodes else None

    def _wait_for_image_state(self, ami, desired_state):
        """Timer to wait for the image/snapshot to reach a desired state
        :params:ami_id: correspoding image id in Amazon
        :params:desired_state: the desired new state of the image to be in.
        """

        def _wait_for_state():
            """Called at an interval until the AMI image is available."""
            try:
                # LOG.info("\n\n\nImage id = %s" % ami_id + ", state = %s\n\n\n" % state)
                image = self.driver.Image(ami.id)
                if image.state == desired_state:
                    LOG.info("Image has changed state to %s." % desired_state)
                    raise loopingcall.LoopingCallDone()
                else:
                    LOG.info("Image state %s." % image.state)
            except BotoCoreError as e:
                LOG.info("BotoCoreError: {}".format(e))
                pass

        timer = loopingcall.FixedIntervalLoopingCall(_wait_for_state)
        timer.start(interval=3).wait()

    def _wait_for_state(self, instance, ec2_node, desired_state, desired_power_state):
        """Wait for the state of the corrosponding ec2 instance to be in completely available state.
        :params:ec2_id: the instance's corrosponding ec2 id.
        :params:desired_state: the desired state of the instance to be in.
        """

        def _wait_for_power_state():
            """Called at an interval until the VM is running again."""

            state = ec2_node.state
            if state == desired_state:
                LOG.info("Instance has changed state to %s." % desired_state)
                raise loopingcall.LoopingCallDone()

        def _wait_for_status_check():
            """Power state of a machine might be ON, but status check is the one which gives the real"""

            if ec2_node.system_status.status == 'ok':
                LOG.info("Instance status check is %s / %s" %
                         (ec2_node.system_status.status, ec2_node.instance_status.status))
                raise loopingcall.LoopingCallDone()

        # waiting for the power state to change
        timer = loopingcall.FixedIntervalLoopingCall(_wait_for_power_state)
        timer.start(interval=1).wait()

        # waiting for the status of the machine to be in running
        if desired_state == 'running':
            timer = loopingcall.FixedIntervalLoopingCall(_wait_for_status_check)
            timer.start(interval=0.5).wait()
