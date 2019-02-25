config t
hostname ${local_hostname}
line vty 0 4
exec-timeout 0 0
configure terminal
crypto ikev2 profile default
match identity remote fqdn domain cisco.com
identity local fqdn ${local_hostname}.cisco.com
authentication remote pre-share key ${remote_pre_share_key}
authentication local pre-share key ${local_pre_share_key}
interface Tunnel0
ip address ${tunnel_ip_local_site} 255.255.255.252
tunnel source GigabitEthernet1
tunnel destination ${public_subnet_public_ip_remote_site}
tunnel protection ipsec profile default
crypto ikev2 dpd 10 2 on-demand
int gi2
no shut
ip route 0.0.0.0 0.0.0.0 ${public_gateway_ip}
ip route ${public_subnet_private_ip_cidr_remote_site} ${public_subnet_private_ip_cidr_remote_site_network_mask} ${tunnel_ip_remote_site}
