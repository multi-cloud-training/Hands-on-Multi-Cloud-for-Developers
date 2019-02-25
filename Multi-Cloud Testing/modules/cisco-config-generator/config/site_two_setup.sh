conf t
hostname ${hostname_site_two}
end
config t
line vty 0 4
exec-timeout 0 0
end
enable
configure terminal
crypto ikev2 profile default
authentication remote pre-share key ${local_pre_share_key} 
authentication local pre-share key ${remote_pre_share_key}
end
interface Tunnel0
ip address ${tunnel_ip_site_two} 255.255.255.252
tunnel source GigabitEthernet1
tunnel destination ${public_ip_site_two}
tunnel protection ipsec profile default
crypto ikev2 dpd 10 2 on-demand
int gi2
no shut
end
ip route ${public_ip_site_two} 255.255.0.0 ${tunnel_ip_site_two}
