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
from abc import ABCMeta, abstractmethod


class BaseProvider:
    __metaclass__ = ABCMeta

    def __init__(self):
        pass

    @abstractmethod
    def list_nodes(self):
        """Return all VM known to the virtualization layer, as a list"""
        raise NotImplementedError()

    @abstractmethod
    def list_sizes(self):
        """Return all sizes from provider"""
        raise NotImplementedError()

    @abstractmethod
    def create_node(self, instance, image_meta, *args, **kwargs):
        raise NotImplementedError()

    @abstractmethod
    def reboot(self, instance, *args, **kwargs):
        raise NotImplementedError()

    @abstractmethod
    def destroy(self, instance, *args, **kwargs):
        raise NotImplementedError()

    @abstractmethod
    def get_info(self, instance):
        """Get instance info from provider

        Must return dict:
            {
                state:          the running state, one of the power_state codes
                max_mem_kb:     (int) the maximum memory in KBytes allowed
                mem_kb:         (int) the memory in KBytes used by the instance
                num_cpu:        (int) the number of virtual CPUs for the instance
                cpu_time_ns:    (int) the CPU time used in nanoseconds
                id:             a unique ID for the instance
            }

        :param instance: Openstack node instance
        :return: Info dict
        """
        raise NotImplementedError()

    @abstractmethod
    def list_instances(self):
        """Return the names of all the instances known to the virtualization layer, as a list"""
        raise NotImplementedError()

    @abstractmethod
    def list_instance_uuids(self):
        """Return the UUIDS of all the instances known to the virtualization layer, as a list"""
        raise NotImplementedError()

    @abstractmethod
    def power_off(self, instance, timeout=0, retry_interval=0):
        """Power off the specified instance.

        :param instance: nova.objects.instance.Instance
        :param timeout: time to wait for GuestOS to shutdown
        :param retry_interval: How often to signal guest while
                               waiting for it to shutdown
        """
        raise NotImplementedError()

    @abstractmethod
    def power_on(self, context, instance, network_info, block_device_info=None):
        """Issues a provider specific commend to start provider instance

        :param instance: Local instance
        """
        raise NotImplementedError()

    def attach_volume(self, context, connection_info, instance, mountpoint,
                      disk_bus=None, device_type=None, encryption=None):
        """Attach the disk to the instance at mountpoint using info."""
        raise NotImplementedError()

    def detach_volume(self, connection_info, instance, mountpoint,
                      encryption=None):
        """Detach the disk attached to the instance."""
        raise NotImplementedError()

    def snapshot(self, context, instance, image_id, update_task_state):
        """Snapshots the specified instance.

        :param context: security context
        :param instance: nova.objects.instance.Instance
        :param image_id: Reference to a pre-created image that will hold the snapshot.
        """
        raise NotImplementedError()

    def finish_migration(self, context, migration, instance, disk_info, network_info, image_meta, resize_instance,
                         block_device_info=None, power_on=True):
        """Completes a resize.

        :param context: the context for the migration/resize
        :param migration: the migrate/resize information
        :param instance: nova.objects.instance.Instance being migrated/resized
        :param disk_info: the newly transferred disk information
        :param network_info:
           :py:meth:`~nova.network.manager.NetworkManager.get_instance_nw_info`
        :param image_meta: image object returned by nova.image.glance that
                           defines the image from which this instance
                           was created
        :param resize_instance: True if the instance is being resized,
                                False otherwise
        :param block_device_info: instance volume block device info
        :param power_on: True if the instance should be powered on, False
                         otherwise
        """
        raise NotImplementedError()

    def confirm_migration(self, migration, instance, network_info):
        """Confirms a resize, destroying the source VM.

        :param instance: nova.objects.instance.Instance
        """
        raise NotImplementedError()
