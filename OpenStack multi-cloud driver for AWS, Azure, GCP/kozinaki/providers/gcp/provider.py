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

import os
import logging

from haikunator import Haikunator
from oslo_config import cfg
from oslo_service import loopingcall
from nova.compute import power_state
from googleapiclient import discovery
from oauth2client.client import GoogleCredentials
from googleapiclient.errors import HttpError

from ..common import BaseProvider


LOG = logging.getLogger(__name__)

haikunator = Haikunator()


POWER_STATE_MAP = {
    0:  power_state.NOSTATE,
    'RUNNING': power_state.RUNNING,
    32: power_state.NOSTATE,
    'TERMINATED': power_state.SHUTDOWN,
    64: power_state.NOSTATE,
    80: power_state.SHUTDOWN,
}


IMAGE_MAP = {
    'centos': 'centos-cloud',
    'coreos': 'coreos-cloud',
    'debian': 'debian-cloud',
    'rhel': 'rhel-cloud',
    'sles': 'suse-cloud',
    'ubuntu': 'ubuntu-cloud',
    'windows': 'windows-cloud',
    'sql': 'windows-sql-cloud',
}


class GCPProvider(BaseProvider):

    def __init__(self):
        super(GCPProvider, self).__init__()
        self.name = 'GCP'
        self.config_name = 'kozinaki_' + self.name
        self.vm_prefix = 'kozinaki-'
        self.driver = self.get_driver()
        self._mounts = {}

    def get_driver(self):
        config = self.load_config()
        os.environ.update(
            {'GOOGLE_APPLICATION_CREDENTIALS': config['path_to_json_token']})
        credentials = GoogleCredentials.get_application_default()

        compute = discovery.build('compute', 'v1', credentials=credentials)
        return compute

    def load_config(self):
        """Load config options from nova config file or command line (for example: /etc/nova/nova.conf)

        Sample settings in nova config:
            [kozinaki_EC2]
            user=AKIAJR7NAEIZPWSTFBEQ
            key=zv9zSem8OE+k/axFkPCgZ3z3tLrhvFBaIIa0Ik0j
        """

        provider_opts = [
            cfg.StrOpt('path_to_json_token', help='Google API json token file', secret=True),
            cfg.StrOpt('project', help='Google project id'),
            cfg.StrOpt('zone', help='Google zone name'),
        ]

        cfg.CONF.register_opts(provider_opts, self.config_name)
        return cfg.CONF[self.config_name]

    def create_node(self, instance, image_meta, *args, **kwargs):
        config = self.load_config()

        # Get info
        image_family = getattr(image_meta.properties, 'os_distro')
        flavor_name = instance.flavor['name']

        # Get the latest image
        for family_startwith, project in IMAGE_MAP.items():
            if image_family.startswith(family_startwith):
                image_project = project
                break
        else:
            raise Exception('Project for image family "{}" not found'.format(image_family))

        image_response = self.driver.images().getFromFamily(project=image_project, family=image_family).execute()
        source_disk_image = image_response['selfLink']

        # Configure the machine
        machine_type = "zones/{zone}/machineTypes/{flavor}".format(zone=config['zone'], flavor=flavor_name)

        machine_config = {
            'name': self.vm_prefix + instance.uuid,
            'machineType': machine_type,

            # Specify the boot disk and the image to use as a source.
            'disks': [
                {
                    'boot': True,
                    'autoDelete': True,
                    'initializeParams': {
                        'sourceImage': source_disk_image,
                    }
                }
            ],

            # Specify a network interface with NAT to access the public
            # internet.
            'networkInterfaces': [{
                'network': 'global/networks/default',
                'accessConfigs': [
                    {'type': 'ONE_TO_ONE_NAT', 'name': 'External NAT'}
                ]
            }],

            # Allow the instance to access cloud storage and logging.
            'serviceAccounts': [{
                'email': 'default',
                'scopes': [
                    'https://www.googleapis.com/auth/devstorage.read_write',
                    'https://www.googleapis.com/auth/logging.write'
                ]
            }],
        }

        operation = self.driver.instances().insert(
            project=config['project'], zone=config['zone'], body=machine_config).execute()
        self.wait_for_operation(operation)

    def list_nodes(self):
        config = self.load_config()
        result = self.driver.instances().list(project=config['project'], zone=config['zone']).execute()
        return result['items']

    def destroy(self, instance, *args, **kwargs):
        config = self.load_config()

        gcp_instance = self._get_gcp_instance(instance, config['project'], config['zone'])
        if gcp_instance:
            operation = self.driver.instances().delete(
                project=config['project'], zone=config['zone'], instance=gcp_instance['name']).execute()
            self.wait_for_operation(operation)

    def list_instances(self):
        config = self.load_config()
        result = self.driver.instances().list(project=config['project'], zone=config['zone']).execute()
        return result['items']

    def list_sizes(self):
        config = self.load_config()
        result = self.driver.instances().list(project=config['project'], zone=config['zone']).execute()
        return result['items']

    def power_on(self, context, instance, network_info, block_device_info=None):
        config = self.load_config()
        operation = self.driver.instances().start(
            project=config['project'], zone=config['zone'], instance=self.vm_prefix + instance.uuid).execute()
        self.wait_for_operation(operation)

    def list_instance_uuids(self):
        return [node.id for node in self.list_nodes()]

    def power_off(self, instance, timeout=0, retry_interval=0):
        config = self.load_config()
        operation = self.driver.instances().stop(
            project=config['project'], zone=config['zone'], instance=self.vm_prefix + instance.uuid).execute()
        self.wait_for_operation(operation)

    def get_info(self, instance):
        config = self.load_config()
        instance = self.driver.instances().get(
            project=config['project'], zone=config['zone'], instance=self.vm_prefix + instance.uuid).execute()

        if instance:
            node_power_state = POWER_STATE_MAP.get(instance['status'], power_state.NOSTATE)
            node_id = instance['id']
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
        config = self.load_config()
        operation = self.driver.instances().reset(
            project=config['project'], zone=config['zone'], instance=self.vm_prefix + instance.uuid).execute()
        self.wait_for_operation(operation)

    def get_or_create_volume(self, project, zone, volume_name):
        try:
            response = self.driver.disks().get(project=project, zone=zone, disk=volume_name).execute()
            return response['selfLink']
        except HttpError as e:
            if e.resp.status != 404:
                raise e

        # Disk not found
        disk_body = {
            "name": volume_name,
            "description": "Created by kozinaki",
            "type": "projects/{project}/zones/{zone}/diskTypes/pd-standard".format(project=project, zone=zone),
            "sizeGb": "10"
        }

        operation = self.driver.disks().insert(project=project, zone=zone, body=disk_body).execute()
        self.wait_for_operation(operation)
        return operation['selfLink']

    def attach_volume(self, context, connection_info, instance, mountpoint,
                      disk_bus=None, device_type=None, encryption=None):
        """Attach the disk to the instance at mountpoint using info."""
        config = self.load_config()
        instance_name = self.vm_prefix + instance.uuid
        if instance_name not in self._mounts:
            self._mounts[instance_name] = {}
        self._mounts[instance_name][mountpoint] = connection_info

        volume_id = connection_info['data']['volume_id']

        volume_self_link = self.get_or_create_volume(
            project=config['project'], zone=config['zone'], volume_name=volume_id)

        body = {
            "kind": "compute#attachedDisk",
            "source": volume_self_link,
            "deviceName": volume_id,
            "boot": False,
            "autoDelete": False,
        }

        operation = self.driver.instances().attachDisk(
            project=config['project'], zone=config['zone'], instance=self.vm_prefix + instance.uuid, body=body).execute()

        self.wait_for_operation(operation)

    def detach_volume(self, connection_info, instance, mountpoint,
                      encryption=None):
        """Detach the disk attached to the instance."""
        config = self.load_config()

        try:
            del self._mounts[instance['name']][mountpoint]
        except KeyError:
            pass

        volume_id = connection_info['data']['volume_id']

        operation = self.driver.instances().detachDisk(
            project=config['project'], zone=config['zone'], instance=self.vm_prefix + instance.uuid, deviceName=volume_id).execute()

        self.wait_for_operation(operation)

    def snapshot(self, context, instance, image_id, update_task_state):
        config = self.load_config()

        body = {
            "sourceDisk": "projects/{project}/zones/{zone}/disks/{instance}".format(
                project=config['project'],
                zone=config['zone'],
                instance=self.vm_prefix + instance.uuid
            ),
            "name": "snapshot-{}".format(self.vm_prefix + instance.uuid)
        }

        operation = self.driver.disks().createSnapshot(
            project=config['project'], zone=config['zone'], disk=self.vm_prefix + instance.uuid, body=body).execute()

        self.wait_for_operation(operation)

    def finish_migration(self, context, migration, instance, disk_info, network_info, image_meta, resize_instance,
                         block_device_info=None, power_on=True):
        LOG.info("***** Calling FINISH MIGRATION *******************")
        config = self.load_config()

        flavor_name = migration['new_instance_type_name']

        body = {
            "machineType": "zones/{zone}/machineTypes/{flavor}".format(zone=config['zone'], flavor=flavor_name)
        }

        operation = self.driver.instances().setMachineType(
            project=config['project'], zone=config['zone'], instnace=self.vm_prefix + instance.uuid, body=body).execute()

        self.wait_for_operation(operation)

    def confirm_migration(self, migration, instance, network_info):
        LOG.info("***** Calling CONFIRM MIGRATION *******************")
        config = self.load_config()
        operation = self.driver.instances().start(
            project=config['project'], zone=config['zone'], instance=self.vm_prefix + instance.uuid).execute()
        self.wait_for_operation(operation)

    def _get_gcp_instance(self, nova_instance, project, zone):
        try:
            response = self.driver.instances().get(
                project=project, zone=zone, instance=self.vm_prefix + nova_instance.uuid).execute()
            return response
        except HttpError as e:
            if e.resp.status != 404:
                raise e
            return False

    def wait_for_operation(self, operation):
        config = self.load_config()
        LOG.info('Waiting for operation to finish...')

        def waiting():
            result = self.driver.zoneOperations().get(
                project=config['project'],
                zone=config['zone'],
                operation=operation['name']).execute()
            LOG.info('Operation state {}'.format(result['status']))
            if result['status'] == 'DONE':
                print("done.")
                if 'error' in result:
                    raise Exception(result['error'])
                raise loopingcall.LoopingCallDone()

        timer = loopingcall.FixedIntervalLoopingCall(waiting)
        timer.start(interval=1).wait()
