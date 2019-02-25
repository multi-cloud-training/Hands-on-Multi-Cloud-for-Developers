function Log-Message{
    param($Message)
    
    #create log file
    $dirLog = $env:APPDATA + "\ConnectAzureVPN"
    $dir = Get-Item -Path $dirLog
    if(-not $dir)
    {
        New-Item -ItemType directory -Path $dirLog
    }
    
    $today = Get-Date
    $logFile = $dirLog + "\" + $today.Day + ".log"

    $today.ToString() + " : " + $Message >> $logFile
}


while($true)
{
    #Sleep 15 second
    Start-Sleep 15



    #check DNS server in enviroment
    $dnsServer = $env:Azure_DNS_VPN
    
    if(-not $dnsServer)
    {
        Log-Message -Message "Can't find Azure_DNS_VPN in enviroment variables."
        continue
    }

    #check subnets information in enviroment, like 10.0.1.0#10.0.2.0
    $subnetStr = $env:Azure_Subnet_VPN
    if(-not $subnetStr)
    {
        Log-Message -Message "Can't find Azure_Subnet_VPN in enviroment variables. The Azure-Subnet_VPN should be set like 10.0.1.0#10.0.2.0"
        continue
    }
    $subnets = $subnetStr.Split("#")

    #check Point-To-Site ip address range, like 10.0.0.
    $ipRange = $env:Azure_P2S_IP_Range_VPN
    if(-not $ipRange)
    {
        Log-Message -Message "Can't find Azure_P2S_IP_Range_VPN in enviroment variables. The Azure-Subnet_VPN should be set like 10.0.0."
        continue
    }


    #get Azure VPN gateway
    $vpnParentDir = $env:APPDATA + "\Microsoft\Network\Connections\Cm"
    $itemsInDir = Get-ChildItem -Path $vpnParentDir | ?{ $_.PSIsContainer } | Select-Object Name
    if(-not $itemsInDir)
    {
        Log-Message -Message "Can't find Azure Point to Site VPN installed on this computer, please double check."
        continue
    }

    if($itemsInDir -isnot [System.Array])
    {
        $vpnGateway = $itemsInDir.Name
    }
    else
    {
        Log-Message -Message "Multiple directory found under $vpnParentDir, we select the first one as vpn gateway."
        $vpnGateway = $itemsInDir[0].Name
    }

    
    #check whether DNS server can be contacted    
    $result = gwmi -query "SELECT * FROM Win32_PingStatus WHERE Address = '$dnsServer'" 
    if ($result.StatusCode -eq 0) { 
        Log-Message -Message "$dnsServer is up."
    } 
    else{ 
        Log-Message -Message "$dnsServer is down."


        #disconnect at first, then connect
        Log-Message -Message "Disconnecting..."
        rasdial $vpnGateway /DISCONNECT 
        Log-Message -Message "Connecting..."
        rasdial $vpnGateway /PHONEBOOK:$vpnParentDir\$vpnGateway\$vpnGateway.pbk 


        # Adds IP routes to Azure VPN through the Point-To-Site VPN


        # Find the current new DHCP assigned IP address from Azure
        $azureIpAddress = ipconfig | findstr $ipRange

        # If Azure hasn't given us one yet, exit and let u know
        if (!$azureIpAddress){
            Log-Message -Message "You do not currently have an IP address in your Azure subnet."
            continue
        }

        $azureIpAddress = $azureIpAddress.Split(": ")
        $azureIpAddress = $azureIpAddress[$azureIpAddress.Length-1]
        $azureIpAddress = $azureIpAddress.Trim()

        # Delete any previous configured routes for these ip ranges
        foreach($subnet in $subnets) {
            $routeExists = route print | findstr $subnet
            if($routeExists) {
                Log-Message -Message "Deleting route to Azure: $subnet"
                route delete $subnet
            }
        }

        # Add our new routes to Azure Virtual Network
        foreach($subnet in $subnets) {
            Log-Message -Message "Adding route to Azure: $subnet"
            Log-Message -Message "route add $ip MASK 255.255.255.0 $azureIpAddress"
            route add $subnet MASK 255.255.255.0 $azureIpAddress
        } 

        # Set DNS for the current vm
        $adapterStr = netsh interface ipv4 show interfaces | findstr $vpnGateway
        if($adapterStr)
        {
            $adapterName = $adapterStr.Substring($adapterStr.Indexof($vpnGateway))
            Log-Message -Message "Config DNS server as $dnsServer on adapter $vpnGateway"
            netsh interface ip set dns $adapterName static $dnsServer
            ipconfig /registerdns
        }
        
    }


 }
