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
"""
Kozinaki hybrid cloud Nova compute driver
"""

from oslo_config import cfg
from nova import network, conductor, exception
from nova.compute import power_state, arch, hv_type, vm_mode
from nova.api.openstack.common import logging
from nova.virt import driver
from nova.virt.hardware import InstanceInfo
from oslo_service import loopingcall

from .exceptions import KozinakiException
from .providers import get_provider_by_name

LOG = logging.getLogger(__name__)
_LOG = "\n\n### {} ###\n\n"


CONF = cfg.CONF

# In OpenStack Newton netconf moved to nova.conf
try:
    CONF.import_opt('my_ip', 'nova.netconf')
except ImportError as e:
    CONF.import_opt('my_ip', 'nova.conf.netconf')

# Mapping of libcloud instance power states to the OpenStack power states
# Each libcloud driver has provider specific power states in NODE_STATE_MAP within each driver
# which map to generic libclou power states defined in libcloud.compute.types.NodeState
# OpenStack power states are defined in nova/compute/power_state.py STATE_MAP variable

provider_to_local_nodestates = {
    # libcloud             OpenStack
    0: power_state.RUNNING,  # 0: RUNNING       :  2: RUNNING: running
    1: power_state.NOSTATE,  # 1: REBOOTING     :  1: NOSTATE: pending
    2: power_state.SHUTDOWN,  # 2: TERMINATED    :  4: SHUTDOWN: shutdown
    3: power_state.NOSTATE,  # 3: PENDING       :  1: NOSTATE: pending
    4: power_state.NOSTATE,  # 4: UNKNOWN       :  1: NOSTATE: pending
    5: power_state.SHUTDOWN  # 5: SHUTDOWN      :  4: SHUTDOWN: shutdown
}

AZURE_POWER_STATE = {
    "Starting": power_state.NOSTATE,
    "Started": power_state.RUNNING,
    "Stopping": power_state.NOSTATE,
    "Stopped": power_state.SHUTDOWN,
}


class KozinakiDriver(driver.ComputeDriver):

    def __init__(self, virtapi, read_only=False):
        super(KozinakiDriver, self).__init__(virtapi)
        self.instances = {}
        self._mounts = {}
        self._version = '0.1'
        self._interfaces = {}
        self.conductor_api = conductor.API()
        self.network_api = network.API()
        self.supported_hv_specs = ['kozinaki']

    def init_host(self, host):
        """Initialize anything that is necessary for the driver to function,
        including catching up with currently running VM's on the given host.
        """

        return

    def _configured_providers(self):
        providers_sections = [section[9:] for section in
                              CONF.list_all_sections() if section.startswith('kozinaki_')]
        return providers_sections

    def cleanup_host(self, host):
        """Clean up anything that is necessary for the driver gracefully stop,
        including ending remote sessions. This is optional.
        """
        pass

    def get_info(self, instance):
        """Get the current status of an instance

        :param instance: local instance nova.objects.instance.Instance object

        Returns a InstanceInfo object
            state:          the running state, one of the power_state codes
            max_mem_kb:     (int) the maximum memory in KBytes allowed
            mem_kb:         (int) the memory in KBytes used by the instance
            num_cpu:        (int) the number of virtual CPUs for the instance
            cpu_time_ns:    (int) the CPU time used in nanoseconds
            id:             a unique ID for the instance
        """
        provider = self._get_instance_provider(instance)
        provider_info = provider.get_info(instance)

        instance_info = InstanceInfo(**provider_info)

        return instance_info

    def get_num_instances(self):
        """Return the total number of virtual machines.

        Return the number of virtual machines that the hypervisor knows
        about.

        .. note::

            This implementation works for all drivers, but it is
            not particularly efficient. Maintainers of the virt drivers are
            encouraged to override this method with something more
            efficient.
        """
        return len(self.list_instances())

    def instance_exists(self, instance):
        """Checks existence of an instance on the host.

        :param instance: The instance to lookup

        Returns True if an instance with the supplied ID exists on
        the host, False otherwise.

        .. note::

            This implementation works for all drivers, but it is
            not particularly efficient. Maintainers of the virt drivers are
            encouraged to override this method with something more
            efficient.
        """
        try:
            return instance.uuid in self.list_instance_uuids()
        except NotImplementedError:
            return instance.name in self.list_instances()

    def estimate_instance_overhead(self, instance_info):
        """Estimate the virtualization overhead required to build an instance
        of the given flavor.

        Defaults to zero, drivers should override if per-instance overhead
        calculations are desired.

        :param instance_info: Instance/flavor to calculate overhead for.
        :returns: Dict of estimated overhead values.
        """
        return {'memory_mb': 0, 'disk_gb': 0}

    def list_instances(self):
        """Return the names of all the instances known to the virtualization layer, as a list"""
        all_nodes_names = []
        for provider_name in self._configured_providers():
            provider = get_provider_by_name(provider_name)
            all_nodes_names.extend(provider.list_instances())
        return all_nodes_names

    def list_instance_uuids(self):
        """Return the UUIDS of all the instances known to the virtualization layer, as a list"""
        all_nodes_uuid = []
        for provider_name in self._configured_providers():
            provider = get_provider_by_name(provider_name)
            all_nodes_uuid.extend(provider.list_instances())
        return all_nodes_uuid

    def rebuild(self, context, instance, image_meta, injected_files,
                admin_password, bdms, detach_block_devices,
                attach_block_devices, network_info=None,
                recreate=False, block_device_info=None,
                preserve_ephemeral=False):
        """Destroy and re-make this instance.

        A 'rebuild' effectively purges all existing data from the system and
        remakes the VM with given 'metadata' and 'personalities'.

        This base class method shuts down the VM, detaches all block devices,
        then spins up the new VM afterwards. It may be overridden by
        hypervisors that need to - e.g. for optimisations, or when the 'VM'
        is actually proxied and needs to be held across the shutdown + spin
        up steps.

        :param context: security context
        :param instance: nova.objects.instance.Instance
                         This function should use the data there to guide
                         the creation of the new instance.
        :param nova.objects.ImageMeta image_meta:
            The metadata of the image of the instance.
        :param injected_files: User files to inject into instance.
        :param admin_password: Administrator password to set in instance.
        :param bdms: block-device-mappings to use for rebuild
        :param detach_block_devices: function to detach block devices. See
            nova.compute.manager.ComputeManager:_rebuild_default_impl for
            usage.
        :param attach_block_devices: function to attach block devices. See
            nova.compute.manager.ComputeManager:_rebuild_default_impl for
            usage.
        :param network_info: instance network information
        :param recreate: True if the instance is being recreated on a new
            hypervisor - all the cleanup of old state is skipped.
        :param block_device_info: Information about block devices to be
                                  attached to the instance.
        :param preserve_ephemeral: True if the default ephemeral storage
                                   partition must be preserved on rebuild
        """
        raise NotImplementedError()

    def spawn(self, context, instance, image_meta, injected_files, admin_password,
              network_info=None, block_device_info=None):
        """Create a new instance/VM/domain on the virtualization platform.

        Once this successfully completes, the instance should be
        running (power_state.RUNNING).

        If this fails, any partial instance should be completely
        cleaned up, and the virtualization platform should be in the state
        that it was before this call began.

        :param context: security context
        :param instance: nova.objects.instance.Instance
                         This function should use the data there to guide
                         the creation of the new instance.
        :param image_meta: image object returned by nova.image.glance that
                           defines the image from which to boot this instance
        :param injected_files: User files to inject into instance.
        :param admin_password: Administrator password to set in instance.
        :param network_info:
           :py:meth:`~nova.network.manager.NetworkManager.get_instance_nw_info`
        :param block_device_info: Information about block devices to be
                                  attached to the instance.
        """
        provider_name = getattr(image_meta.properties, 'img_owner_id', None)
        provider = get_provider_by_name(provider_name)
        provider.create_node(instance, image_meta, injected_files, admin_password, network_info, block_device_info)
        instance['metadata'].update({'cloud_service_name': provider_name})

        def _wait_for_boot():
            """Called at an interval until the VM is running."""
            node = self.get_info(instance)

            if node:
                LOG.info("Power state: {}".format(node.state), instance=instance)

                if node.state == power_state.RUNNING:
                    LOG.info("Instance spawned successfully.", instance=instance)
                    raise loopingcall.LoopingCallDone()

        timer = loopingcall.FixedIntervalLoopingCall(_wait_for_boot)
        timer.start(interval=3).wait()

    def destroy(self, context, instance, network_info, block_device_info=None, destroy_disks=True, migrate_data=None):
        """Destroy the specified instance from the Hypervisor.

        If the instance is not found (for example if networking failed), this
        function should still succeed.  It's probably a good idea to log a
        warning in that case.

        :param context: security context
        :param instance: Instance object as returned by DB layer.
        :param network_info: instance network information
        :param block_device_info: Information about block devices that should
                                  be detached from the instance.
        :param destroy_disks: Indicates if disks should be destroyed
        :param migrate_data: implementation specific params
        """

        provider = self._get_instance_provider(instance)
        provider.destroy(instance, context, network_info, block_device_info=None, destroy_disks=True, migrate_data=None)

    def cleanup(self, context, instance, network_info, block_device_info=None,
                destroy_disks=True, migrate_data=None, destroy_vifs=True):
        """Cleanup the instance resources .

        Instance should have been destroyed from the Hypervisor before calling
        this method.

        :param context: security context
        :param instance: Instance object as returned by DB layer.
        :param network_info: instance network information
        :param block_device_info: Information about block devices that should
                                  be detached from the instance.
        :param destroy_disks: Indicates if disks should be destroyed
        :param migrate_data: implementation specific params
        """
        raise NotImplementedError()

    def reboot(self, context, instance, network_info, reboot_type,
               block_device_info=None, bad_volumes_callback=None):
        """Reboot the specified instance.

        After this is called successfully, the instance's state
        goes back to power_state.RUNNING. The virtualization
        platform should ensure that the reboot action has completed
        successfully even in cases in which the underlying domain/vm
        is paused or halted/stopped.

        :param instance: nova.objects.instance.Instance
        :param network_info: instance network information
        :param reboot_type: Either a HARD or SOFT reboot
        :param block_device_info: Info pertaining to attached volumes
        :param bad_volumes_callback: Function to handle any bad volumes
            encountered
        """

        provider = self._get_instance_provider(instance)
        provider.reboot(instance)

    def get_console_pool_info(self, console_type):

        raise NotImplementedError()

    def get_console_output(self, context, instance):
        """Get console output for an instance

        :param context: security context
        :param instance: nova.objects.instance.Instance
        """
        raise NotImplementedError()

    def get_vnc_console(self, context, instance):
        """Get connection info for a vnc console.

        :param context: security context
        :param instance: nova.objects.instance.Instance

        :returns an instance of console.type.ConsoleVNC
        """
        raise NotImplementedError()

    def get_spice_console(self, context, instance):
        """Get connection info for a spice console.

        :param context: security context
        :param instance: nova.objects.instance.Instance

        :returns an instance of console.type.ConsoleSpice
        """
        raise NotImplementedError()

    def get_rdp_console(self, context, instance):
        """Get connection info for a rdp console.

        :param context: security context
        :param instance: nova.objects.instance.Instance

        :returns an instance of console.type.ConsoleRDP
        """
        raise NotImplementedError()

    def get_serial_console(self, context, instance):
        """Get connection info for a serial console.

        :param context: security context
        :param instance: nova.objects.instance.Instance

        :returns an instance of console.type.ConsoleSerial
        """
        raise NotImplementedError()

    def get_mks_console(self, context, instance):
        """Get connection info for a MKS console.

        :param context: security context
        :param instance: nova.objects.instance.Instance

        :returns an instance of console.type.ConsoleMKS
        """
        raise NotImplementedError()

    def get_diagnostics(self, instance):
        """Return diagnostics data about the given instance.

        :param nova.objects.instance.Instance instance:
            The instance to which the diagnostic data should be returned.

        :return: Has a big overlap to the return value of the newer interface
            :func:`get_instance_diagnostics`
        :rtype: dict
        """
        # TODO: static data, must get this from instance
        return {'cpu0_time': 17300000000,
                'memory': 524288,
                'vda_errors': -1,
                'vda_read': 262144,
                'vda_read_req': 112,
                'vda_write': 5778432,
                'vda_write_req': 488,
                'vnet1_rx': 2070139,
                'vnet1_rx_drop': 0,
                'vnet1_rx_errors': 0,
                'vnet1_rx_packets': 26701,
                'vnet1_tx': 140208,
                'vnet1_tx_drop': 0,
                'vnet1_tx_errors': 0,
                'vnet1_tx_packets': 662,
                }

    def get_instance_diagnostics(self, instance):
        """Return diagnostics data about the given instance.

        :param nova.objects.instance.Instance instance:
            The instance to which the diagnostic data should be returned.

        :return: Has a big overlap to the return value of the older interface
            :func:`get_diagnostics`
        :rtype: nova.virt.diagnostics.Diagnostics
        """
        raise NotImplementedError()

    def get_all_bw_counters(self, instances):
        """Return bandwidth usage counters for each interface on eachrunning VM.

        :param instances: nova.objects.instance.InstanceList
        """
        raise NotImplementedError()

    def get_all_volume_usage(self, context, compute_host_bdms):
        """Return usage info for volumes attached to vms on a given host"""
        raise NotImplementedError()

    def get_host_ip_addr(self):
        """Retrieves the IP address of this local nova compute instance"""
        return CONF.my_ip

    def attach_volume(self, context, connection_info, instance, mountpoint,
                      disk_bus=None, device_type=None, encryption=None):
        """Attach the disk to the instance at mountpoint using info."""
        instance_name = instance['name']
        if instance_name not in self._mounts:
            self._mounts[instance_name] = {}
        self._mounts[instance_name][mountpoint] = connection_info
        return True

    def detach_volume(self, connection_info, instance, mountpoint,
                      encryption=None):
        """Detach the disk attached to the instance."""
        try:
            del self._mounts[instance['name']][mountpoint]
        except KeyError:
            pass
        return True

    def swap_volume(self, old_connection_info, new_connection_info,
                    instance, mountpoint, resize_to):
        """Replace the disk attached to the instance."""
        instance_name = instance['name']
        if instance_name not in self._mounts:
            self._mounts[instance_name] = {}
        self._mounts[instance_name][mountpoint] = new_connection_info
        return True

    def attach_interface(self, instance, image_meta, vif):
        if vif['id'] in self._interfaces:
            raise exception.InterfaceAttachFailed('duplicate')
        self._interfaces[vif['id']] = vif

    def detach_interface(self, instance, vif):
        try:
            del self._interfaces[vif['id']]
        except KeyError:
            raise exception.InterfaceDetachFailed('not attached')

    def migrate_disk_and_power_off(self, context, instance, dest, flavor, network_info, block_device_info=None,
                                   timeout=0, retry_interval=0):
        """Transfers the disk of a running instance in multiple phases, turning
        off the instance before the end.

        :param nova.objects.instance.Instance instance:
            The instance whose disk should be migrated.
        :param str dest:
            The IP address of the destination host.
        :param nova.objects.flavor.Flavor flavor:
            The flavor of the instance whose disk get migrated.
        :param nova.network.model.NetworkInfo network_info:
            The network information of the given `instance`.
        :param dict block_device_info:
            Information about the block devices.
        :param int timeout:
            The time in seconds to wait for the guest OS to shutdown.
        :param int retry_interval:
            How often to signal guest while waiting for it to shutdown.

        :return: A list of disk information dicts in JSON format.
        :rtype: str
        """
        raise NotImplementedError()

    def snapshot(self, context, instance, image_id, update_task_state):
        """Snapshots the specified instance.

        :param context: security context
        :param instance: nova.objects.instance.Instance
        :param image_id: Reference to a pre-created image that will
                         hold the snapshot.
        """

        provider = self._get_instance_provider(instance)
        provider.snapshot(context, instance, image_id, update_task_state)

    def post_interrupted_snapshot_cleanup(self, context, instance):
        """Cleans up any resources left after an interrupted snapshot.

        :param context: security context
        :param instance: nova.objects.instance.Instance
        """
        pass

    def finish_migration(self, context, migration, instance, disk_info,
                         network_info, image_meta, resize_instance,
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

        provider = self._get_instance_provider(instance)
        provider.finish_migration(context, migration, instance, disk_info, network_info, image_meta, resize_instance,
                                  block_device_info, power_on)

    def confirm_migration(self, migration, instance, network_info):
        """Confirms a resize, destroying the source VM.

        :param instance: nova.objects.instance.Instance
        """
        provider = self._get_instance_provider(instance)
        provider.confirm_migration(migration, instance, network_info)

    def finish_revert_migration(self, context, instance, network_info,
                                block_device_info=None, power_on=True):
        """Finish reverting a resize/migration.

        :param context: the context for the finish_revert_migration
        :param instance: nova.objects.instance.Instance being migrated/resized
        :param network_info: instance network information
        :param block_device_info: instance volume block device info
        :param power_on: True if the instance should be powered on, False
                         otherwise
        """
        raise NotImplementedError()

    def pause(self, instance):
        """Pause the given instance.

        A paused instance doesn't use CPU cycles of the host anymore. The
        state of the VM could be stored in the memory or storage space of the
        host, depending on the underlying hypervisor technology.
        A "stronger" version of `pause` is :func:'suspend'.
        The counter action for `pause` is :func:`unpause`.

        :param nova.objects.instance.Instance instance:
            The instance which should be paused.

        :return: None
        """

        raise NotImplementedError()

    def unpause(self, instance):
        """Unpause the given paused instance.

        The paused instance gets unpaused and will use CPU cycles of the
        host again. The counter action for 'unpause' is :func:`pause`.
        Depending on the underlying hypervisor technology, the guest has the
        same state as before the 'pause'.

        :param nova.objects.instance.Instance instance:
            The instance which should be unpaused.

        :return: None
        """

        raise NotImplementedError()

    def suspend(self, context, instance):
        """Suspend the specified instance.

        A suspended instance doesn't use CPU cycles or memory of the host
        anymore. The state of the instance could be persisted on the host
        and allocate storage space this way. A "softer" way of `suspend`
        is :func:`pause`. The counter action for `suspend` is :func:`resume`.

        :param nova.context.RequestContext context:
            The context for the suspend.
        :param nova.objects.instance.Instance instance:
            The instance to suspend.

        :return: None
        """
        raise NotImplementedError()

    def resume(self, context, instance, network_info, block_device_info=None):
        """resume the specified suspended instance.

        The suspended instance gets resumed and will use CPU cycles and memory
        of the host again. The counter action for 'resume' is :func:`suspend`.
        Depending on the underlying hypervisor technology, the guest has the
        same state as before the 'suspend'.

        :param nova.context.RequestContext context:
            The context for the resume.
        :param nova.objects.instance.Instance instance:
            The suspended instance to resume.
        :param nova.network.model.NetworkInfo network_info:
            Necessary network information for the resume.
        :param dict block_device_info:
            Instance volume block device info.

        :return: None
        """
        raise NotImplementedError()

    def resume_state_on_host_boot(self, context, instance, network_info,
                                  block_device_info=None):
        """resume guest state when a host is booted.

        :param instance: nova.objects.instance.Instance
        """
        raise NotImplementedError()

    def rescue(self, context, instance, network_info, image_meta,
               rescue_password):
        """Rescue the specified instance.

        :param nova.context.RequestContext context:
            The context for the rescue.
        :param nova.objects.instance.Instance instance:
            The instance being rescued.
        :param nova.network.model.NetworkInfo network_info:
            Necessary network information for the resume.
        :param nova.objects.ImageMeta image_meta:
            The metadata of the image of the instance.
        :param rescue_password: new root password to set for rescue.
        """
        raise NotImplementedError()

    def set_bootable(self, instance, is_bootable):
        """Set the ability to power on/off an instance.

        :param instance: nova.objects.instance.Instance
        """
        raise NotImplementedError()

    def unrescue(self, instance, network_info):
        """Unrescue the specified instance.

        :param instance: nova.objects.instance.Instance
        """

        raise NotImplementedError()

    def power_off(self, instance, timeout=0, retry_interval=0):
        """Power off the specified instance.

        :param instance: nova.objects.instance.Instance
        :param timeout: time to wait for GuestOS to shutdown
        :param retry_interval: How often to signal guest while
                               waiting for it to shutdown
        """
        provider = self._get_instance_provider(instance)
        provider.power_off(instance, timeout, retry_interval)

    def power_on(self, context, instance, network_info, block_device_info=None):
        """
        Issues a provider specific commend to start provider instance
        :param instance: Local instance
        """
        provider = self._get_instance_provider(instance)
        provider.power_on(context, instance, network_info, block_device_info)

    def trigger_crash_dump(self, instance):
        """Trigger crash dump mechanism on the given instance.

        Stalling instances can be triggered to dump the crash data. How the
        guest OS reacts in details, depends on the configuration of it.

        :param nova.objects.instance.Instance instance:
            The instance where the crash dump should be triggered.

        :return: None
        """
        raise NotImplementedError()

    def soft_delete(self, instance):
        """Soft delete the specified instance.

        A soft-deleted instance doesn't allocate any resources anymore, but is
        still available as a database entry. The counter action :func:`restore`
        uses the database entry to create a new instance based on that.

        :param nova.objects.instance.Instance instance:
            The instance to soft-delete.

        :return: None
        """
        raise NotImplementedError()

    def restore(self, instance):
        """Restore the specified soft-deleted instance.

        The restored instance will be automatically booted. The counter action
        for `restore` is :func:`soft_delete`.

        :param nova.objects.instance.Instance instance:
            The soft-deleted instance which should be restored from the
            soft-deleted data.

        :return: None
        """
        raise NotImplementedError()

    # TODO: since we have infinite resources we need to set them as either maximum
    # available by data type or figure out what infinity means for them

    def get_available_resource(self, nodename):
        """Retrieve resource information.

        This method is called when nova-compute launches, and
        as part of a periodic task that records the results in the DB.

        :param nodename:
            node which the caller want to get resources from
            a driver that manages only one node can safely ignore this
        :returns: Dictionary describing resources
        """

        dic = {'vcpus': 999999,
               'memory_mb': 999999,
               'local_gb': 999999,
               'vcpus_used': 0,
               'memory_mb_used': 0,
               'local_gb_used': 0,
               'numa_topology': None,
               'hypervisor_type': 'kozinaki',
               'hypervisor_version': 1.0,
               'hypervisor_hostname': nodename,
               'cpu_info': '?',

               'supported_instances': [
                   (arch.I686, hv_type.FAKE, vm_mode.HVM),
                   (arch.X86_64, hv_type.FAKE, vm_mode.HVM)
               ]
               }
        return dic

    def pre_live_migration(self, context, instance, block_device_info, network_info, disk_info, migrate_data=None):
        """Prepare an instance for live migration

        :param context: security context
        :param instance: nova.objects.instance.Instance object
        :param block_device_info: instance block device information
        :param network_info: instance network information
        :param disk_info: instance disk information
        :param migrate_data: a LiveMigrateData object
        """
        raise NotImplementedError()

    def live_migration(self, context, instance, dest,
                       post_method, recover_method, block_migration=False,
                       migrate_data=None):
        """Live migration of an instance to another host.

        :param context: security context
        :param instance:
            nova.db.sqlalchemy.models.Instance object
            instance object that is migrated.
        :param dest: destination host
        :param post_method:
            post operation method.
            expected nova.compute.manager._post_live_migration.
        :param recover_method:
            recovery method when any exception occurs.
            expected nova.compute.manager._rollback_live_migration.
        :param block_migration: if true, migrate VM disk.
        :param migrate_data: a LiveMigrateData object

        """
        raise NotImplementedError()

    def live_migration_force_complete(self, instance):
        """Force live migration to complete

        :param instance: Instance being live migrated

        """
        raise NotImplementedError()

    def live_migration_abort(self, instance):
        """Abort an in-progress live migration.

        :param instance: instance that is live migrating

        """
        raise NotImplementedError()

    def rollback_live_migration_at_destination(self, context, instance,
                                               network_info,
                                               block_device_info,
                                               destroy_disks=True,
                                               migrate_data=None):
        """Clean up destination node after a failed live migration.

        :param context: security context
        :param instance: instance object that was being migrated
        :param network_info: instance network information
        :param block_device_info: instance block device information
        :param destroy_disks:
            if true, destroy disks at destination during cleanup
        :param migrate_data: a LiveMigrateData object

        """
        raise NotImplementedError()

    def post_live_migration(self, context, instance, block_device_info,
                            migrate_data=None):
        """Post operation of live migration at source host.

        :param context: security context
        :instance: instance object that was migrated
        :block_device_info: instance block device information
        :param migrate_data: a LiveMigrateData object
        """
        pass

    def post_live_migration_at_source(self, context, instance, network_info):
        """Unplug VIFs from networks at source.

        :param context: security context
        :param instance: instance object reference
        :param network_info: instance network information
        """
        raise NotImplementedError("Hypervisor driver does not support post_live_migration_at_source method")

    def post_live_migration_at_destination(self, context, instance,
                                           network_info,
                                           block_migration=False,
                                           block_device_info=None):
        """Post operation of live migration at destination host.

        :param context: security context
        :param instance: instance object that is migrated
        :param network_info: instance network information
        :param block_migration: if true, post operation of block_migration.
        """
        raise NotImplementedError()

    def check_instance_shared_storage_local(self, context, instance):
        """Check if instance files located on shared storage.

        This runs check on the destination host, and then calls
        back to the source host to check the results.

        :param context: security context
        :param instance: nova.objects.instance.Instance object
        """
        raise NotImplementedError()

    def check_instance_shared_storage_remote(self, context, data):
        """Check if instance files located on shared storage.

        :param context: security context
        :param data: result of check_instance_shared_storage_local
        """
        raise NotImplementedError()

    def check_instance_shared_storage_cleanup(self, context, data):
        """Do cleanup on host after check_instance_shared_storage calls

        :param context: security context
        :param data: result of check_instance_shared_storage_local
        """
        pass

    def check_can_live_migrate_destination(self, context, instance,
                                           src_compute_info, dst_compute_info,
                                           block_migration=False,
                                           disk_over_commit=False):
        """Check if it is possible to execute live migration.

        This runs checks on the destination host, and then calls
        back to the source host to check the results.

        :param context: security context
        :param instance: nova.db.sqlalchemy.models.Instance
        :param src_compute_info: Info about the sending machine
        :param dst_compute_info: Info about the receiving machine
        :param block_migration: if true, prepare for block migration
        :param disk_over_commit: if true, allow disk over commit
        :returns: a LiveMigrateData object (hypervisor-dependent)
        """
        raise NotImplementedError()

    def cleanup_live_migration_destination_check(self, context,
                                                 dest_check_data):
        """Do required cleanup on dest host after check_can_live_migrate calls

        :param context: security context
        :param dest_check_data: result of check_can_live_migrate_destination
        """
        raise NotImplementedError()

    def check_can_live_migrate_source(self, context, instance,
                                      dest_check_data, block_device_info=None):
        """Check if it is possible to execute live migration.

        This checks if the live migration can succeed, based on the
        results from check_can_live_migrate_destination.

        :param context: security context
        :param instance: nova.db.sqlalchemy.models.Instance
        :param dest_check_data: result of check_can_live_migrate_destination
        :param block_device_info: result of _get_instance_block_device_info
        :returns: a LiveMigrateData object
        """
        raise NotImplementedError()

    def get_instance_disk_info(self, instance,
                               block_device_info=None):
        """Retrieve information about actual disk sizes of an instance.

        :param instance: nova.objects.Instance
        :param block_device_info:
            Optional; Can be used to filter out devices which are
            actually volumes.
        :return:
            json strings with below format::

                "[{'path':'disk',
                   'type':'raw',
                   'virt_disk_size':'10737418240',
                   'backing_file':'backing_file',
                   'disk_size':'83886080'
                   'over_committed_disk_size':'10737418240'},
                   ...]"
        """
        raise NotImplementedError()

    def refresh_security_group_rules(self, security_group_id):
        """This method is called after a change to security groups.

        All security groups and their associated rules live in the datastore,
        and calling this method should apply the updated rules to instances
        running the specified security group.

        An error should be raised if the operation cannot complete.

        """

        raise NotImplementedError()

    def refresh_instance_security_rules(self, instance):
        """Refresh security group rules

        Gets called when an instance gets added to or removed from
        the security group the instance is a member of or if the
        group gains or loses a rule.
        """
        raise NotImplementedError()

    def reset_network(self, instance):
        """reset networking for specified instance."""

        pass

    def ensure_filtering_rules_for_instance(self, instance, network_info):
        """Setting up filtering rules and waiting for its completion.

        To migrate an instance, filtering rules to hypervisors
        and firewalls are inevitable on destination host.
        ( Waiting only for filtering rules to hypervisor,
        since filtering rules to firewall rules can be set faster).

        Concretely, the below method must be called.
        - setup_basic_filtering (for nova-basic, etc.)
        - prepare_instance_filter(for nova-instance-instance-xxx, etc.)

        to_xml may have to be called since it defines PROJNET, PROJMASK.
        but libvirt migrates those value through migrateToURI(),
        so , no need to be called.

        Don't use thread for this method since migration should
        not be started when setting-up filtering rules operations
        are not completed.

        :param instance: nova.objects.instance.Instance object

        """

        raise NotImplementedError()

    def filter_defer_apply_on(self):
        """Defer application of IPTables rules."""
        pass

    def filter_defer_apply_off(self):
        """Turn off deferral of IPTables rules and apply the rules now."""
        pass

    def unfilter_instance(self, instance, network_info):
        """Stop filtering instance."""

        raise NotImplementedError()

    def set_admin_password(self, instance, new_pass):
        """Set the root password on the specified instance.

        :param instance: nova.objects.instance.Instance
        :param new_pass: the new password
        """
        raise NotImplementedError()

    def inject_file(self, instance, b64_path, b64_contents):
        """Writes a file on the specified instance.

        The first parameter is an instance of nova.compute.service.Instance,
        and so the instance is being specified as instance.name. The second
        parameter is the base64-encoded path to which the file is to be
        written on the instance; the third is the contents of the file, also
        base64-encoded.

        NOTE(russellb) This method is deprecated and will be removed once it
        can be removed from nova.compute.manager.
        """

        raise NotImplementedError()

    def change_instance_metadata(self, context, instance, diff):
        """Applies a diff to the instance metadata.

        This is an optional driver method which is used to publish
        changes to the instance's metadata to the hypervisor.  If the
        hypervisor has no means of publishing the instance metadata to
        the instance, then this method should not be implemented.

        :param context: security context
        :param instance: nova.objects.instance.Instance
        """
        pass

    def inject_network_info(self, instance, nw_info):
        """inject network info for specified instance."""

        pass

    def poll_rebooting_instances(self, timeout, instances):
        """Perform a reboot on all given 'instances'.

        Reboots the given `instances` which are longer in the rebooting state
        than `timeout` seconds.

        :param int timeout:
            The timeout (in seconds) for considering rebooting instances
            to be stuck.
        :param list instances:
            A list of nova.objects.instance.Instance objects that have been
            in rebooting state longer than the configured timeout.

        :return: None
        """

        raise NotImplementedError()

    def host_power_action(self, action):
        """Reboots, shuts down or powers up the host.

        :param str action:
            The action the host should perform. The valid actions are:
            ""startup", "shutdown" and "reboot".

        :return: The result of the power action
        :rtype: : str
        """

        raise NotImplementedError()

    def host_maintenance_mode(self, host, mode):
        """Start/Stop host maintenance window. On start, it triggers
        guest VMs evacuation.
        """
        if not mode:
            return 'off_maintenance'
        return 'on_maintenance'

    def set_host_enabled(self, enabled):
        """Sets the specified host's ability to accept new instances."""
        if enabled:
            return 'enabled'
        return 'disabled'

    def get_host_uptime(self):
        """Returns the result of calling the Linux command `uptime` on this
        host.

        :return: A text which contains the uptime of this host since the
                 last boot.
        :rtype: str
        """

        raise NotImplementedError()

    def plug_vifs(self, instance, network_info):
        """Plug virtual interfaces (VIFs) into the given `instance` at
        instance boot time.

        The counter action is :func:`unplug_vifs`.

        :param nova.objects.instance.Instance instance:
            The instance which gets VIFs plugged.
        :param nova.network.model.NetworkInfo network_info:
            The object which contains information about the VIFs to plug.

        :return: None
        """

        raise NotImplementedError()

    def unplug_vifs(self, instance, network_info):
        """Unplug virtual interfaces (VIFs) from networks.

        The counter action is :func:`plug_vifs`.

        :param nova.objects.instance.Instance instance:
            The instance which gets VIFs unplugged.
        :param nova.network.model.NetworkInfo network_info:
            The object which contains information about the VIFs to unplug.

        :return: None
        """
        raise NotImplementedError()

    def get_host_cpu_stats(self):
        """Get the currently known host CPU stats.

        :returns: a dict containing the CPU stat info, eg:

            | {'kernel': kern,
            |  'idle': idle,
            |  'user': user,
            |  'iowait': wait,
            |   'frequency': freq},

                  where kern and user indicate the cumulative CPU time
                  (nanoseconds) spent by kernel and user processes
                  respectively, idle indicates the cumulative idle CPU time
                  (nanoseconds), wait indicates the cumulative I/O wait CPU
                  time (nanoseconds), since the host is booting up; freq
                  indicates the current CPU frequency (MHz). All values are
                  long integers.

        """
        raise NotImplementedError()

    def block_stats(self, instance_name, disk_id):
        """Return performance counters associated with the given disk_id on the
        given instance.  These are returned as [rd_req, rd_bytes, wr_req,
        wr_bytes, errs], where rd indicates read, wr indicates write, req is
        the total number of I/O requests made, bytes is the total number of
        bytes transferred, and errs is the number of requests held up due to a
        full pipeline.

        All counters are long integers.

        This method is optional.  On some platforms (e.g. XenAPI) performance
        statistics can be retrieved directly in aggregate form, without Nova
        having to do the aggregation.  On those platforms, this method is
        unused.

        Note that this function takes an instance ID.
        """
        return [0, 0, 0, 0, None]

    def deallocate_networks_on_reschedule(self, instance):
        """Does the driver want networks deallocated on reschedule?"""
        return False

    def macs_for_instance(self, instance):
        """What MAC addresses must this instance have?

        Some hypervisors (such as bare metal) cannot do freeform virtualization
        of MAC addresses. This method allows drivers to return a set of MAC
        addresses that the instance is to have. allocate_for_instance will take
        this into consideration when provisioning networking for the instance.

        Mapping of MAC addresses to actual networks (or permitting them to be
        freeform) is up to the network implementation layer. For instance,
        with openflow switches, fixed MAC addresses can still be virtualized
        onto any L2 domain, with arbitrary VLANs etc, but regular switches
        require pre-configured MAC->network mappings that will match the
        actual configuration.

        Most hypervisors can use the default implementation which returns None.
        Hypervisors with MAC limits should return a set of MAC addresses, which
        will be supplied to the allocate_for_instance call by the compute
        manager, and it is up to that call to ensure that all assigned network
        details are compatible with the set of MAC addresses.

        This is called during spawn_instance by the compute manager.

        :return: None, or a set of MAC ids (e.g. set(['12:34:56:78:90:ab'])).
            None means 'no constraints', a set means 'these and only these
            MAC addresses'.
        """
        return None

    def dhcp_options_for_instance(self, instance):
        """Get DHCP options for this instance.

        Some hypervisors (such as bare metal) require that instances boot from
        the network, and manage their own TFTP service. This requires passing
        the appropriate options out to the DHCP service. Most hypervisors can
        use the default implementation which returns None.

        This is called during spawn_instance by the compute manager.

        Note that the format of the return value is specific to the Neutron
        client API.

        :return: None, or a set of DHCP options, eg:

             |    [{'opt_name': 'bootfile-name',
             |      'opt_value': '/tftpboot/path/to/config'},
             |     {'opt_name': 'server-ip-address',
             |      'opt_value': '1.2.3.4'},
             |     {'opt_name': 'tftp-server',
             |      'opt_value': '1.2.3.4'}
             |    ]

        """
        return None

    def manage_image_cache(self, context, all_instances):
        """Manage the driver's local image cache.

        Some drivers chose to cache images for instances on disk. This method
        is an opportunity to do management of that cache which isn't directly
        related to other calls into the driver. The prime example is to clean
        the cache and remove images which are no longer of interest.

        :param all_instances: nova.objects.instance.InstanceList
        """
        pass

    def add_to_aggregate(self, context, aggregate, host, **kwargs):
        """Add a compute host to an aggregate.

        The counter action to this is :func:`remove_from_aggregate`

        :param nova.context.RequestContext context:
            The security context.
        :param nova.objects.aggregate.Aggregate aggregate:
            The aggregate which should add the given `host`
        :param str host:
            The name of the host to add to the given `aggregate`.
        :param dict kwargs:
            A free-form thingy...

        :return: None
        """
        # NOTE(jogo) Currently only used for XenAPI-Pool
        raise NotImplementedError()

    def remove_from_aggregate(self, context, aggregate, host, **kwargs):
        """Remove a compute host from an aggregate.

        The counter action to this is :func:`add_to_aggregate`

        :param nova.context.RequestContext context:
            The security context.
        :param nova.objects.aggregate.Aggregate aggregate:
            The aggregate which should remove the given `host`
        :param str host:
            The name of the host to remove from the given `aggregate`.
        :param dict kwargs:
            A free-form thingy...

        :return: None
        """
        raise NotImplementedError()

    def undo_aggregate_operation(self, context, op, aggregate, host, set_error=True):
        """Undo for Resource Pools."""
        raise NotImplementedError()

    def get_volume_connector(self, instance):
        """Get connector information for the instance for attaching to volumes.

        Connector information is a dictionary representing the ip of the
        machine that will be making the connection, the name of the iscsi
        initiator and the hostname of the machine as follows::

            {
                'ip': ip,
                'initiator': initiator,
                'host': hostname
            }

        """
        return {'ip': '127.0.0.1', 'initiator': 'fake', 'host': 'fakehost'}

    def get_available_nodes(self, refresh=False):
        """Returns nodenames of all nodes managed by the compute service.

        This method is for multi compute-nodes support. If a driver supports
        multi compute-nodes, this method returns a list of nodenames managed
        by the service. Otherwise, this method should return
        [hypervisor_hostname].
        """
        return ['azure-cloud']

    def node_is_available(self, nodename):
        """Return whether this compute service manages a particular node."""
        if nodename in self.get_available_nodes():
            return True
        # Refresh and check again.
        return nodename in self.get_available_nodes(refresh=True)

    def get_per_instance_usage(self):
        """Get information about instance resource usage.

        :returns: dict of  nova uuid => dict of usage info
        """
        return {}

    def instance_on_disk(self, instance):
        """Checks access of instance files on the host.

        :param instance: nova.objects.instance.Instance to lookup

        Returns True if files of an instance with the supplied ID accessible on
        the host, False otherwise.

        .. note::
            Used in rebuild for HA implementation and required for validation
            of access to instance shared disk files
        """
        return False

    def register_event_listener(self, callback):
        """Register a callback to receive events.

        Register a callback to receive asynchronous event
        notifications from hypervisors. The callback will
        be invoked with a single parameter, which will be
        an instance of the nova.virt.event.Event class.
        """

        self._compute_event_callback = callback

    def delete_instance_files(self, instance):
        """Delete any lingering instance files for an instance.

        :param instance: nova.objects.instance.Instance
        :returns: True if the instance was deleted from disk, False otherwise.
        """
        return True

    @property
    def need_legacy_block_device_info(self):
        """Tell the caller if the driver requires legacy block device info.

        Tell the caller whether we expect the legacy format of block
        device info to be passed in to methods that expect it.
        """
        return True

    def volume_snapshot_create(self, context, instance, volume_id,
                               create_info):
        """Snapshots volumes attached to a specified instance.

        The counter action to this is :func:`volume_snapshot_delete`

        :param nova.context.RequestContext context:
            The security context.
        :param nova.objects.instance.Instance  instance:
            The instance that has the volume attached
        :param uuid volume_id:
            Volume to be snapshotted
        :param create_info: The data needed for nova to be able to attach
               to the volume.  This is the same data format returned by
               Cinder's initialize_connection() API call.  In the case of
               doing a snapshot, it is the image file Cinder expects to be
               used as the active disk after the snapshot operation has
               completed.  There may be other data included as well that is
               needed for creating the snapshot.
        """
        raise NotImplementedError()

    def volume_snapshot_delete(self, context, instance, volume_id,
                               snapshot_id, delete_info):
        """Deletes a snapshot of a volume attached to a specified instance.

        The counter action to this is :func:`volume_snapshot_create`

        :param nova.context.RequestContext context:
            The security context.
        :param nova.objects.instance.Instance instance:
            The instance that has the volume attached.
        :param uuid volume_id:
            Attached volume associated with the snapshot
        :param uuid snapshot_id:
            The snapshot to delete.
        :param dict delete_info:
            Volume backend technology specific data needed to be able to
            complete the snapshot.  For example, in the case of qcow2 backed
            snapshots, this would include the file being merged, and the file
            being merged into (if appropriate).

        :return: None
        """
        raise NotImplementedError()

    def default_root_device_name(self, instance, image_meta, root_bdm):
        """Provide a default root device name for the driver.

        :param nova.objects.instance.Instance instance:
            The instance to get the root device for.
        :param nova.objects.ImageMeta image_meta:
            The metadata of the image of the instance.
        :param nova.objects.BlockDeviceMapping root_bdm:
            The description of the root device.
        """
        raise NotImplementedError()

    def default_device_names_for_instance(self, instance, root_device_name,
                                          *block_device_lists):
        """Default the missing device names in the block device mapping."""
        raise NotImplementedError()

    def get_device_name_for_instance(self, instance,
                                     bdms, block_device_obj):
        """Get the next device name based on the block device mapping.

        :param instance: nova.objects.instance.Instance that volume is
                         requesting a device name
        :param bdms: a nova.objects.BlockDeviceMappingList for the instance
        :param block_device_obj: A nova.objects.BlockDeviceMapping instance
                                 with all info about the requested block
                                 device. device_name does not need to be set,
                                 and should be decided by the driver
                                 implementation if not set.

        :returns: The chosen device name.
        """
        raise NotImplementedError()

    def is_supported_fs_format(self, fs_type):
        """Check whether the file format is supported by this driver

        :param fs_type: the file system type to be checked,
                        the validate values are defined at disk API module.
        """
        return False

    def quiesce(self, context, instance, image_meta):
        """Quiesce the specified instance to prepare for snapshots.

        If the specified instance doesn't support quiescing,
        InstanceQuiesceNotSupported is raised. When it fails to quiesce by
        other errors (e.g. agent timeout), NovaException is raised.

        :param context:  request context
        :param instance: nova.objects.instance.Instance to be quiesced
        :param nova.objects.ImageMeta image_meta:
            The metadata of the image of the instance.
        """
        raise NotImplementedError()

    def unquiesce(self, context, instance, image_meta):
        """Unquiesce the specified instance after snapshots.

        If the specified instance doesn't support quiescing,
        InstanceQuiesceNotSupported is raised. When it fails to quiesce by
        other errors (e.g. agent timeout), NovaException is raised.

        :param context:  request context
        :param instance: nova.objects.instance.Instance to be unquiesced
        :param nova.objects.ImageMeta image_meta:
            The metadata of the image of the instance.
        """
        raise NotImplementedError()

    def network_binding_host_id(self, context, instance):
        """Get host ID to associate with network ports.

        :param context:  request context
        :param instance: nova.objects.instance.Instance that the network
                         ports will be associated with
        :returns: a string representing the host ID
        """
        return instance.get('host')

    def _get_instance_provider(self, instance):
        provider_name = instance.system_metadata.get('image_img_owner_id')
        if provider_name:
            return get_provider_by_name(provider_name)
        else:
            raise KozinakiException('Missing "img_owner_id" property in image. This is should be provider name. '
                                    '(for example img_owner_id="AZURE")')
