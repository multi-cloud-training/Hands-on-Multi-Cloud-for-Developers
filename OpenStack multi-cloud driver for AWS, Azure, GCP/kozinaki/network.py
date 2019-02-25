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
from oslo_config import cfg
from oslo_utils import uuidutils
from oslo_log import log as logging
from nova.network import api as network_api
from nova.objects import fixed_ip as fixed_ip_obj
from nova.network.manager import RPCAllocateFixedIP, NetworkManager


LOG = logging.getLogger(__name__)


network_opts = [
    cfg.StrOpt('flat_network_bridge',
               help='Bridge for simple network instances'),
    cfg.StrOpt('flat_network_dns',
               default='8.8.4.4',
               help='DNS server for simple network'),
    cfg.BoolOpt('flat_injected',
                default=False,
                help='Whether to attempt to inject network setup into guest'),
    cfg.StrOpt('flat_interface',
               help='FlatDhcp will bridge into this interface if set'),
    cfg.IntOpt('vlan_start',
               default=100,
               help='First VLAN for private networks'),
    cfg.StrOpt('vlan_interface',
               help='VLANs will bridge into this interface if set'),
    cfg.IntOpt('num_networks',
               default=1,
               help='Number of networks to support'),
    cfg.StrOpt('vpn_ip',
               default='$my_ip',
               help='Public IP for the cloudpipe VPN servers'),
    cfg.IntOpt('vpn_start',
               default=1000,
               help='First Vpn port for private networks'),
    cfg.IntOpt('network_size',
               default=256,
               help='Number of addresses in each private subnet'),
    cfg.StrOpt('fixed_range_v6',
               default='fd00::/48',
               help='Fixed IPv6 address block'),
    cfg.StrOpt('gateway',
               help='Default IPv4 gateway'),
    cfg.StrOpt('gateway_v6',
               help='Default IPv6 gateway'),
    cfg.IntOpt('cnt_vpn_clients',
               default=0,
               help='Number of addresses reserved for vpn clients'),
    cfg.IntOpt('fixed_ip_disassociate_timeout',
               default=600,
               help='Seconds after which a deallocated IP is disassociated'),
    cfg.IntOpt('create_unique_mac_address_attempts',
               default=5,
               help='Number of attempts to create unique mac address'),
    cfg.BoolOpt('fake_call',
                default=False,
                help='If True, skip using the queue and make local calls'),
    cfg.BoolOpt('teardown_unused_network_gateway',
                default=False,
                help='If True, unused gateway devices (VLAN and bridge) are '
                     'deleted in VLAN network mode with multi hosted '
                     'networks'),
    cfg.BoolOpt('force_dhcp_release',
                default=True,
                help='If True, send a dhcp release on instance termination'),
    cfg.BoolOpt('share_dhcp_address',
                default=False,
                help='If True in multi_host mode, all compute hosts share '
                     'the same dhcp address. The same IP address used for '
                     'DHCP will be added on each nova-network node which '
                     'is only visible to the vms on the same host.'),
    cfg.BoolOpt('update_dns_entries',
                default=False,
                help='If True, when a DNS entry must be updated, it sends a '
                     'fanout cast to all network hosts to update their DNS '
                     'entries in multi host mode'),
    cfg.IntOpt("dns_update_periodic_interval",
               default=-1,
               help='Number of seconds to wait between runs of updates to DNS '
                    'entries.'),
    cfg.StrOpt('dhcp_domain',
               default='novalocal',
               help='Domain to use for building the hostnames'),
    cfg.StrOpt('l3_lib',
               default='nova.network.l3.LinuxNetL3',
               help="Indicates underlying L3 management library"),
    ]

CONF = cfg.CONF
for option in network_opts:
    try:
        CONF.register_opt(option)
    except cfg.DuplicateOptError as e:
        LOG.debug(e)

# CONF.register_opts(network_opts)
CONF.import_opt('use_ipv6', 'nova.netconf')
CONF.import_opt('my_ip', 'nova.netconf')
CONF.import_opt('network_topic', 'nova.network.rpcapi')
CONF.import_opt('fake_network', 'nova.network.linux_net')


class FlatManager(RPCAllocateFixedIP, NetworkManager):
    """Basic network where no vlans are used.

    FlatManager does not do any bridge or vlan creation.  The user is
    responsible for setting up whatever bridges are specified when creating
    networks through nova-manage. This bridge needs to be created on all
    compute hosts.

    The idea is to create a single network for the host with a command like:
    nova-manage network create 192.168.0.0/24 1 256. Creating multiple
    networks for for one manager is currently not supported, but could be
    added by modifying allocate_fixed_ip and get_network to get the network
    with new logic. Arbitrary lists of addresses in a single network can
    be accomplished with manual db editing.

    If flat_injected is True, the compute host will attempt to inject network
    config into the guest.  It attempts to modify /etc/network/interfaces and
    currently only works on debian based systems. To support a wider range of
    OSes, some other method may need to be devised to let the guest know which
    ip it should be using so that it can configure itself. Perhaps an attached
    disk or serial device with configuration info.

    Metadata forwarding must be handled by the gateway, and since nova does
    not do any setup in this mode, it must be done manually.  Requests to
    169.254.169.254 port 80 will need to be forwarded to the api server.

    """

    timeout_fixed_ips = False

    required_create_args = ['bridge']

#     def _allocate_mac_addresses(self, context, instance_uuid, networks, macs):s):
#         """ We are allocating instance during instance spawn, so we don't
#             need this part. """
#         pass

#     def _allocate_fixed_ips(self, context, instance_id, host, networks,
#                             **kwargs):
#         """We are allocating IPs during instance spawn."""
#         requested_networks = kwargs.get('requested_networks')
#         for network in networks:
#             address = None
#             if requested_networks is not None:
#                 for address in (fixed_ip for (uuid, fixed_ip) in
#                                 requested_networks if network['uuid'] == uuid):
#                     break
#
#             self.allocate_fixed_ip(context, instance_id,
#                                    network, address=address)

#     def _get_networks_for_instance(self, context, instance_id, project_id,
#                                    requested_networks=None):
#         LOG.debug("###%s \n\n"  % requested_networks)
#         if requested_networks is not None and len(requested_networks) != 0:
#             return NetworkManager._get_networks_for_instance(self, context, instance_id, project_id,
#                                    requested_networks=requested_networks)
#         else:
#             return []

    def allocate_for_instance(self, context, **kwargs):
        import pdb; pdb.set_trace()
        requested_networks = kwargs.get('requested_networks')
        if requested_networks is not None and len(requested_networks) != 0:
            return NetworkManager.allocate_for_instance(self, context, **kwargs)
        else:
            instance_uuid = kwargs['instance_id']
            if not uuidutils.is_uuid_like(instance_uuid):
                instance_uuid = kwargs.get('instance_uuid')
            host = kwargs['host']
            rxtx_factor = kwargs['rxtx_factor']

            return self.get_instance_nw_info(context, instance_uuid, rxtx_factor,
                                             host)

    def deallocate_fixed_ip(self, context, address, host=None, teardown=True, instance=None):
        """Returns a fixed ip to the pool."""
        import pdb; pdb.set_trace()
        # super(FlatManager, self).deallocate_fixed_ip(context, address, host, teardown, instance=instance)
        fixed_ip_obj.FixedIP.disassociate_by_address(context, address)

    def _setup_network_on_host(self, context, network):
        """Setup Network on this host."""
        import pdb; pdb.set_trace()
        # NOTE(tr3buchet): this does not need to happen on every ip
        # allocation, this functionality makes more sense in create_network
        # but we'd have to move the flat_injected flag to compute
        network.injected = CONF.flat_injected
        network.save()

    def _teardown_network_on_host(self, context, network):
        """Tear down network on this host."""
        import pdb; pdb.set_trace()
        pass

    # NOTE(justinsb): The floating ip functions are stub-implemented.
    # We were throwing an exception, but this was messing up horizon.
    # Timing makes it difficult to implement floating ips here, in Essex.

    def get_floating_ip(self, context, id):
        """Returns a floating IP as a dict."""
        # NOTE(vish): This is no longer used but can't be removed until
        #             we major version the network_rpcapi to 2.0.
        import pdb; pdb.set_trace()
        return None

    def get_floating_pools(self, context):
        """Returns list of floating pools."""
        # NOTE(maurosr) This method should be removed in future, replaced by
        # get_floating_ip_pools. See bug #1091668
        import pdb; pdb.set_trace()
        return {}

    def get_floating_ip_pools(self, context):
        """Returns list of floating ip pools."""
        # NOTE(vish): This is no longer used but can't be removed until
        #             we major version the network_rpcapi to 2.0.
        import pdb; pdb.set_trace()
        return {}

    def get_floating_ip_by_address(self, context, address):
        """Returns a floating IP as a dict."""
        # NOTE(vish): This is no longer used but can't be removed until
        #             we major version the network_rpcapi to 2.0.
        import pdb; pdb.set_trace()
        return None

    def get_floating_ips_by_project(self, context):
        """Returns the floating IPs allocated to a project."""
        # NOTE(vish): This is no longer used but can't be removed until
        #             we major version the network_rpcapi to 2.0.
        import pdb; pdb.set_trace()
        return []

    def get_floating_ips_by_fixed_address(self, context, fixed_address):
        """Returns the floating IPs associated with a fixed_address."""
        # NOTE(vish): This is no longer used but can't be removed until
        #             we major version the network_rpcapi to 2.0.
        import pdb; pdb.set_trace()
        return []

    @network_api.wrap_check_policy
    def allocate_floating_ip(self, context, project_id, pool):
        """Gets a floating ip from the pool."""
        import pdb; pdb.set_trace()
        return None

    def deallocate_floating_ip(self, context, address, affect_auto_assigned):
        """Returns a floating ip to the pool."""
        import pdb; pdb.set_trace()
        return None

    def associate_floating_ip(self, context, floating_address, fixed_address, affect_auto_assigned=False):
        """Associates a floating ip with a fixed ip.

        Makes sure everything makes sense then calls _associate_floating_ip,
        rpc'ing to correct host if i'm not it.
        """
        import pdb; pdb.set_trace()
        return None

    def disassociate_floating_ip(self, context, address, affect_auto_assigned=False):
        """Disassociates a floating ip from its fixed ip.

        Makes sure everything makes sense then calls _disassociate_floating_ip,
        rpc'ing to correct host if i'm not it.
        """
        import pdb; pdb.set_trace()
        return None

    def migrate_instance_start(self, context, instance_uuid,
                               floating_addresses,
                               rxtx_factor=None, project_id=None,
                               source=None, dest=None):
        import pdb; pdb.set_trace()
        pass

    def migrate_instance_finish(self, context, instance_uuid,
                                floating_addresses, host=None,
                                rxtx_factor=None, project_id=None,
                                source=None, dest=None):
        import pdb; pdb.set_trace()
        pass

    def update_dns(self, context, network_ids):
        """Called when fixed IP is allocated or deallocated."""
        import pdb; pdb.set_trace()
        pass


class FakeManager(RPCAllocateFixedIP, NetworkManager):

    def allocate_fixed_ip(self, context, instance_id, network, **kwargs):
        import pdb; pdb.set_trace()
        return '0.0.0.0'

    def allocate_for_instance(self, context, **kwargs):
        import pdb; pdb.set_trace()
        pass

    def init_host(self):
        """Do any initialization that needs to be run if this is a
        standalone service.
        """
        import pdb; pdb.set_trace()
        pass

    def _setup_network_on_host(self, context, network):
        import pdb; pdb.set_trace()
        pass

    def _teardown_network_on_host(self, context, network):
        import pdb; pdb.set_trace()
        pass

    def _get_network_dict(self, network):
        import pdb; pdb.set_trace()
        return {}
