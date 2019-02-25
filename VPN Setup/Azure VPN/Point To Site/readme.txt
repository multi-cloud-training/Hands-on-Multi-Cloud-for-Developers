1) Install Azure Point to Site(P2S) VPN.
2) Register the enviroment variables in System Properties about your Azure VPN as below sample.
    	#The DNS of your Azure virtual network
	Azure_DNS_VPN = "10.0.1.4"
   	#The Subnet setting in your Azure virtual network, use # to unite multiple subnets like 10.0.1.0#10.0.2.0
	Azure_Subnet_VPN = "10.0.1.0"
	#The IPAddress range which Point to Site VPN will be assigned
	Azure_P2S_IP_Range_VPN = "10.0.0."
3) Use the credential which you install Azure P2S VPN to create a scheduled task run ConnectAzureVPNByScript.ps1. You may modify the user and password in DeployTask.bat and then run it.
4) The log file is stored at $env:appdata\ConnectAzureVPN by date.

Contribute to the script 
at
https://github.com/TomWu1/ConnectAzureVPN/blob/master/ConnectAzureVPNByScript.ps1

or
contact tombwu@gmail.com | tom.wu@software.dell.com
