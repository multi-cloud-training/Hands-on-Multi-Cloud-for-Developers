#! /bin/bash
####
####
####  1 - Replace the $values with the desired values and save
####  2 - Execute chmod 777 deployVPN.sh
####  3 - Execute ./deployCloudVPN.sh
#### 
####


# [START start gcloud]
gcloud init --skip-diagnostics \
# [END start_gcloud]

# generates random number
vpnrandom=$RANDOM

echo
echo Welcome to Cloud VPN Deployer
echo

echo "Insert Region: " 
read region

echo "Insert Network: " 
read network

echo "Insert Supplier Name: " 
read supplier

# [START create_gateway_vpn_gateway]
gcloud compute target-vpn-gateways create gatewayvpn-$supplier-$vpnrandom \
 --network=$network \
 --region $region
# [END create_gateway_vpn_gateway]

# [START create_publicipaddress]
gcloud compute addresses create vpnpublicip-$supplier-$vpnrandom \
 --region $region
# [END create_publicipaddress]

# [START create_forwardingruleesp]
gcloud compute forwarding-rules create fwdespvpn-$supplier-$vpnrandom \
 --target-vpn-gateway=gatewayvpn-$supplier-$vpnrandom \
 --target-vpn-gateway-region $region \
 --region $region \
 --ip-protocol=ESP \
 --address=vpnpublicip-$supplier-$vpnrandom
# [END create_forwardingruleesp]

# [START create_forwardingruleudp500]
gcloud compute forwarding-rules create fwd500vpn-$supplier-$vpnrandom \
 --target-vpn-gateway=gatewayvpn-$supplier-$vpnrandom \
 --target-vpn-gateway-region $region \
 --region $region \
 --ip-protocol=UDP \
 --address=vpnpublicip-$supplier-$vpnrandom \
 --ports=500
# [END create_forwardingruleudp500]

# [START create_forwardingruleudp4500]
gcloud compute forwarding-rules create fwd4500vpn-$supplier-$vpnrandom \
--target-vpn-gateway=gatewayvpn-$supplier-$vpnrandom \
--target-vpn-gateway-region $region \
--region $region \
--ip-protocol=UDP \
--address=vpnpublicip-$supplier-$vpnrandom \
--ports=4500
# [END create_forwardingruleudp4500]

echo "Insert the number of VPN tunnels: " 
read vpntunnelnumber
echo Loading...

for ((c=1;c<=$vpntunnelnumber;c++))
do  

vpntunnelrandom=$RANDOM
echo "Insert Supplier Gateway address: " 
read customergatewayaddress

echo "Insert Shared Key: "
read sharedkey

echo "Insert GCP Private IP network: "
read vpcprivaterange

echo "Insert Supplier Internal IP: " 
read customeriprange

echo "Insert IKE Version (1 or 2): "
read ikeversion

# [START create_tunnelvpn]
gcloud compute vpn-tunnels create tunnelsvpn-$supplier-$vpnrandom-$vpntunnelrandom \
 --peer-address=$customergatewayaddress \
 --shared-secret=$sharedkey \
 --target-vpn-gateway=gatewayvpn-$supplier-$vpnrandom \
 --local-traffic-selector=$vpcprivaterange \
 --ike-version $ikeversion \
 --region $region \
 --remote-traffic-selector=$customeriprange \
 --region $region
# [END create_tunnelvpn]

# [START create_firewallallowicmp]
gcloud compute firewall-rules create allowicmpvpn-$supplier-$vpnrandom-$vpntunnelrandom \
 --allow=icmp \
 --source-ranges=$customeriprange
# [END create_firewallallowicmp]
done