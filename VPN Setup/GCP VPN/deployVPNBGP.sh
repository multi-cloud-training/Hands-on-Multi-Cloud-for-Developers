#! /bin/bash
####
####
####  1 - Replace the $values with the desired values and save
####  2 - Execute chmod 777 deployVPN.sh
####  3 - Execute ./deployVPN.sh
#### 
####

# [START start gcloud]
gcloud config init \
# [START create_publicipaddress]
gcloud compute addresses create $vpnpublicipnamename\
 --region $region
# [END create_publicipaddress]

# [START create_gateway_vpn_gateway]
gcloud compute target-vpn-gateways create $gatewayvpnname \
 --network=$network \
 --region $region
# [END create_gateway_vpn_gateway]

# [START create_forwardingruleesp]
cloud compute forwarding-rules create $forwardingruleespvpnname \
 --target-vpn-gateway=$gatewayvpnname \
 --target-vpn-gateway-region $region \
 --region $region \
 --ip-protocol=ESP \
 --address=$vpnpublicipname
# [END create_forwardingruleesp]

# [START create_forwardingruleudp500]
gcloud compute forwarding-rules create $forwardingruleudp500vpnname \
 --target-vpn-gateway=$gatewayvpnname \
 --target-vpn-gateway-region $region \
 --region $region \
 --ip-protocol=UDP \
 --address=$vpnpublicipname \
 --ports=500
# [END create_forwardingruleudp500]

# [START create_forwardingruleudp4500]
gcloud compute forwarding-rules create $forwardingruleudp4500vpnname \
--target-vpn-gateway=$gatewayvpnname \
--target-vpn-gateway-region $region \
--region $region \
--ip-protocol=UDP \
--address=$vpnpublicipname \
--ports=4500
# [END create_forwardingruleudp4500]

# [START create_tunnelvpn]
gcloud compute vpn-tunnels create $tunnelsvpnname \
 --peer-address=$customergatewayaddress \
 --shared-secret=$sharedkey \
 --target-vpn-gateway=$gatewayvpnname \
 --local-traffic-selector=$vpcprivaterange \
 --network=$network \
 --remote-traffic-selector=$customeriprange \
 --region $region
# [END create_tunnelvpn]

# [START create_router]
gcloud compute routers create $routername \
--asn=$ASN \
--network=NETWORK \
 --region $region
# [END create_router]

# [START create_router_interface]
gcloud compute routers add-interface $interfacerouter \
--interface-name=$interfacename \
--vpn-tunnel=$tunnelsvpnname \
--vpn-tunnel-region=$region
# [END create_router_interface]


# [START create_peer_bgp]
gcloud compute routers add-bgp-peer $peerbgpname \
--interface=$interfacename \
--peer-asn=$peerasn \
--peer-name=$peername \
--advertised-route-priority=$routepriority \
--peer-ip-address=$peripaddress \
--region=$REGION
# [END create_peer_bgp]

# [START create_firewallallowicmp]
gcloud compute firewall-rules create $allowicmpvpnname \
 --allow=icmp \
 --source-ranges=$customeriprange
# [END create_firewallallowicmp]