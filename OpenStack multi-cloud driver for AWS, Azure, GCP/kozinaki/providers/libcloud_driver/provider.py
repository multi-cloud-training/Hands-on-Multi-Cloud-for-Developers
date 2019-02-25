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

import logging
import inspect

import libcloud
from libcloud.compute.types import Provider
from libcloud.compute.base import NodeAuthPassword
from libcloud.compute.providers import get_driver as get_libcloud_driver
from oslo_config import cfg
from nova.compute import power_state

from ..common import BaseProvider
from .extended_drivers import get_extended_driver

LOG = logging.getLogger(__name__)

# Disable SSL check
libcloud.security.VERIFY_SSL_CERT = False

POWER_STATE_MAP = {
    'running': power_state.RUNNING,
    'starting': power_state.NOSTATE,
    'rebooting': power_state.NOSTATE,
    'terminated': power_state.NOSTATE,
    'pending': power_state.NOSTATE,
    'unknown': power_state.NOSTATE,
    'stopping': power_state.NOSTATE,
    'stopped': power_state.SHUTDOWN,
    'suspended': power_state.SUSPENDED,
    'error': power_state.CRASHED,
    'paused': power_state.PAUSED,
    'reconfiguring': power_state.NOSTATE,
    'migrating': power_state.NOSTATE,
}


class LibCloudProvider(BaseProvider):
    def __init__(self, name):
        super(LibCloudProvider, self).__init__()
        self.name = name
        self.config_name = 'kozinaki_' + self.name
        self.provider_name = self.name[3:]
        self.driver = self.get_driver()
        self._mounts = {}

    def get_driver(self):
        config = self.load_config()

        provider_cls = get_extended_driver(
            driver_cls=get_libcloud_driver(getattr(Provider, self.provider_name)),
            nova_config=config
        )

        provider_cls_info = inspect.getargspec(provider_cls.__init__)

        driver = provider_cls(
            **{arg: value for arg, value in config.items() if arg in provider_cls_info.args and value is not None})
        return driver

    def load_config(self):
        """Load config options from nova config file or command line (for example: /etc/nova/nova.conf)

        Sample settings in nova config:
            [kozinaki_EC2]
            user=AKIAJR7NAEIZPWSTFBEQ
            key=zv9zSem8OE+k/axFkPCgZ3z3tLrhvFBaIIa0Ik0j
        """

        provider_cls = get_libcloud_driver(getattr(Provider, self.provider_name))
        provider_cls_info = inspect.getargspec(provider_cls.__init__)

        provider_opts = [cfg.StrOpt(arg) for arg in provider_cls_info.args]
        provider_opts.append(cfg.StrOpt('location'))
        provider_opts.append(cfg.StrOpt('root_password'))
        provider_opts.append(cfg.StrOpt('project_id'))

        cfg.CONF.register_opts(provider_opts, self.config_name)
        return cfg.CONF[self.config_name]

    def create_node(self, instance, image_meta, *args, **kwargs):
        config = self.load_config()

        # Get info
        image_id = getattr(image_meta.properties, 'os_distro')
        flavor_name = instance.flavor['name']

        node_config = {'name': instance.uuid}

        # Find image
        for image in self.driver.list_images():
            if image.id == image_id:
                node_config['image'] = image
                break
        else:
            Exception('Image with id "{}" not found'.format(image_id))

        # Find size
        for size in self.driver.list_sizes():
            if size.id == flavor_name:
                node_config['size'] = size
                break
        else:
            Exception('Flavor with id "{}" not found'.format(flavor_name))

        # Find location
        for location in self.driver.list_locations():
            if location.id == config['location']:
                node_config['location'] = location
                break
        else:
            Exception('Location with id "{}" not found'.format(config['location']))

        # Root password
        try:
            if config.get('root_password'):
                node_config['auth'] = NodeAuthPassword(config.get('root_password'))
        except cfg.NoSuchOptError:
            pass

        instance = self.driver.create_node(**node_config)
        return instance

    def list_nodes(self):
        return self.driver.list_nodes()

    def destroy(self, instance, *args, **kwargs):
        node = self._get_node_by_uuid(instance.uuid)
        if node:
            self.driver.destroy_node(node)

    def list_instances(self):
        return self.list_nodes()

    def list_sizes(self):
        return self.driver.list_images()

    def power_on(self, context, instance, network_info, block_device_info=None):
        node = self._get_node_by_uuid(instance.uuid)
        if node:
            self.driver.ex_power_on_node(node)

    def list_instance_uuids(self):
        return [node.id for node in self.list_nodes()]

    def power_off(self, instance, timeout=0, retry_interval=0):
        node = self._get_node_by_uuid(instance.uuid)
        if node:
            self.driver.ex_shutdown_node(node)

    def get_info(self, instance):
        node = self._get_node_by_uuid(instance.uuid)

        if node:
            node_power_state = POWER_STATE_MAP[node.state]
            node_id = node.id
        else:
            node_power_state = power_state.NOSTATE
            node_id = 0

        node_info = {
            'state': node_power_state,
            'max_mem_kb': 0,  # '(int) the maximum memory in KBytes allowed',
            'mem_kb': 0,  # '(int) the memory in KBytes used by the instance',
            'num_cpu': 0,  # '(int) the number of virtual CPUs for the instance',
            'cpu_time_ns': 0,  # '(int) the CPU time used in nanoseconds',
            'id': node_id
        }

        return node_info

    def reboot(self, instance, *args, **kwargs):
        node = self._get_node_by_uuid(instance.uuid)
        if node:
            self.driver.reboot_node(node)

    def attach_volume(self, context, connection_info, instance, mountpoint,
                      disk_bus=None, device_type=None, encryption=None):
        """Attach the disk to the instance at mountpoint using info."""
        instance_name = instance['name']
        if instance_name not in self._mounts:
            self._mounts[instance_name] = {}
        self._mounts[instance_name][mountpoint] = connection_info

        volume_id = connection_info['data']['volume_id']

        volume = self._get_volume_by_uuid(volume_id)
        node = self._get_node_by_uuid(instance.uuid)
        if not all([volume, node]):
            return

        self.driver.attach_volume(node, volume)

    def detach_volume(self, connection_info, instance, mountpoint,
                      encryption=None):
        """Detach the disk attached to the instance."""
        try:
            del self._mounts[instance['name']][mountpoint]
        except KeyError:
            pass
        volume_id = connection_info['data']['volume_id']

        volume = self._get_volume_by_uuid(volume_id)
        if not volume:
            return

        self.driver.detach_volume(volume)

    def snapshot(self, context, instance, image_id, update_task_state):
        volume = self._get_volume_by_uuid(image_id)
        if not volume:
            return
        self.driver.create_volume_snapshot(volume, 'snapshot_1')

    def _get_node_by_uuid(self, uuid):
        nodes = self.list_nodes()
        for node in nodes:
            # Some providers limit node name. For example - Linode (32 symbols).
            #   node.name   ->  'cd87279c-308a-4a3d-94d9-0ed1912f'
            #   uuid        ->  'cd87279c-308a-4a3d-94d9-0ed1912f06a1'
            if node.name in uuid:
                return node

    def _get_volume_by_uuid(self, uuid):
        volumes = self.driver.list_volumes()
        for volume in volumes:
            if volume.id == uuid:
                return volume
