#parameters
[CmdletBinding()]
Param(
#VNets File Path
  [Parameter(Mandatory=$True,Position=1)]
   [string]$VNetCSVPath,
#Shared Key
   [Parameter(Mandatory=$True)]
   [string]$SharedKey
)


function Create-LocalNetworkSiteNode{
    param($Name,$AddressPrefix,$VPNGatewayAddress,$XmlObj)

    $ret = $XmlObj.CreateElement("LocalNetworkSite",$XmlObj.DocumentElement.NamespaceURI)
    $ret.SetAttribute("name",$Name);
    $nodeAddressSpace = $XmlObj.CreateElement("AddressSpace",$XmlObj.DocumentElement.NamespaceURI)
    $nodeAddressPrefix = $XmlObj.CreateElement("AddressPrefix",$XmlObj.DocumentElement.NamespaceURI)
    $nodeGateway = $XmlObj.CreateElement("VPNGatewayAddress",$XmlObj.DocumentElement.NamespaceURI)
    
    $nodeAddressPrefix.InnerText = $AddressPrefix
    $nodeGateway.InnerText = $VPNGatewayAddress
    $nodeAddressSpace.AppendChild($nodeAddressPrefix)
    $ret.AppendChild($nodeAddressSpace)
    $ret.AppendChild($nodeGateway)

    if($null -ne $XmlObj.NetworkConfiguration.VirtualNetworkConfiguration.LocalNetworkSites)
    {
        $XmlObj.NetworkConfiguration.VirtualNetworkConfiguration.LocalNetworkSites.AppendChild($ret)
    }
    else
    {
        $localSitesNode = $XmlObj.CreateElement("LocalNetworkSites",$XmlObj.DocumentElement.NamespaceURI)
        $localSitesNode.AppendChild($ret)
        $XmlObj.NetworkConfiguration.VirtualNetworkConfiguration.InsertBefore($localSitesNode,$XmlObj.NetworkConfiguration.VirtualNetworkConfiguration.VirtualNetworkSites)
    }
}


function Create-GatewayNodes{
    param($GatewayAddressPrefix,$VNetName,$XmlObj)

    $nodeSubnet = $XmlObj.CreateElement("Subnet",$XmlObj.DocumentElement.NamespaceURI)
    $nodeSubnet.SetAttribute("name","GatewaySubnet");
    $nodeAddressPrefix = $XmlObj.CreateElement("AddressPrefix",$XmlObj.DocumentElement.NamespaceURI)
    $nodeAddressPrefix.InnerText = $GatewayAddressPrefix
    $nodeSubnet.AppendChild($nodeAddressPrefix)
    
    $baseNode = $XmlObj.NetworkConfiguration.VirtualNetworkConfiguration.VirtualNetworkSites.VirtualNetworkSite | where {$_.name -eq $VNetName}
    if($baseNode)
    {
        $gatewaySubnetNode = $baseNode.Subnets.Subnet | where {$_.name -eq "GatewaySubnet"}
        if($gatewaySubnetNode)
        {
            Write-Host "Gateway subnet already get configured on $VNetName."
        }
        else
        {
            $baseNode.Subnets.AppendChild($nodeSubnet)
        }
    }
    else
    {
        Write-Host "Config of $VNetName can't be found"
    }
    
}

function Create-ConnectionsToLocalNetworkNode{
    param($LocalNetworkName,$VNetName,$XmlObj)


    $baseNode = $XmlObj.NetworkConfiguration.VirtualNetworkConfiguration.VirtualNetworkSites.VirtualNetworkSite | where {$_.name -eq $VNetName}
    if(-not $baseNode)
    {
        Write-Host "Config of $VNetName can't be found"
    }
    else
    {
        $nodeLocalNetworkSiteRef = $XmlObj.CreateElement("LocalNetworkSiteRef",$XmlObj.DocumentElement.NamespaceURI)
        $nodeLocalNetworkSiteRef.SetAttribute("name",$LocalNetworkName);
        $connectionNode = $XmlObj.CreateElement("Connection",$XmlObj.DocumentElement.NamespaceURI)
        $connectionNode.SetAttribute("type","IPsec")
        $nodeLocalNetworkSiteRef.AppendChild($connectionNode) 

        if($null -eq $XmlObj.NetworkConfiguration.VirtualNetworkConfiguration.VirtualNetworkSites.VirtualNetworkSite.Gateway)
        {
            $ret = $XmlObj.CreateElement("Gateway",$XmlObj.DocumentElement.NamespaceURI)
            $nodeConnectionsToLocalNetwork = $XmlObj.CreateElement("ConnectionsToLocalNetwork",$XmlObj.DocumentElement.NamespaceURI)
            $nodeConnectionsToLocalNetwork.AppendChild($nodeLocalNetworkSiteRef)
            $ret.AppendChild($nodeConnectionsToLocalNetwork)
        
            $baseNode.AppendChild($ret)
        }
        $baseNode.Gateway.ConnectionsToLocalNetwork.AppendChild($nodeLocalNetworkSiteRef)
    }

}



$SourceVNets = Import-Csv -Path $VNetCSVPath

#hashtable - subscription:vnet object in csv
$VNets = @{}

#hashtable - subscription: vnet configuration xml
$VNetConfigsWithGateway = @{}

#hashtable - subscription: vnet configuration xml
$VNetConfigsWithoutGateway = @{}

#hashtable - subscription: vnet gateway ip address
$VNetGateways = @{}

#Build hashtables
foreach ($VNet in $SourceVNets)
{
    $VNetName = $VNet.VNetName
    $Subscription = $VNet.Subscription

    if($VNets[$Subscription])
    {
        Write-Host "Found subscription '$Subscription' have more than 1 VNet config in csv, exit"
        exit 0
    }

    $VNets[$Subscription] = $VNet

    if(-not (Get-AzureSubscription -SubscriptionName $VNet.Subscription))
    {
        Write-Host "Can't find the subscription '$Subscription', exit"
        exit 0
    }

    Select-AzureSubscription -SubscriptionName $Subscription
    $VNetSite = $null
    $VNetSite = Get-AzureVNetSite -VNetName $VNetName
    if(-not $VNetSite)
    {
        Write-Host "Can't find VNet site $VNetName under subscription '$Subscription', exit"
        exit 0
    }


    $VNetGateway = Get-AzureVNetGateway -VNetName $VNetName
    if($VNetGateway -and $VNetGateway.VIPAddress)
    {
        $config = Get-AzureVNetConfig 
        $VNetConfigsWithGateway[$Subscription] = $config.XMLConfiguration
        $VNetGateways[$Subscription] = $VNetGateway.VIPAddress
    }
    else
    {
        Write-Host "Can't find the gateway of '$Subscription - $VNetName' ,  will create a new gateway after configuration!"
        $config = Get-AzureVNetConfig 
        $VNetConfigsWithoutGateway[$Subscription] = $config.XMLConfiguration
        $VNetGateways[$Subscription] = "1.0.0.0"
    }


}

#Create gateway for those VNets which don't have one.
foreach($Subscription in $VNetConfigsWithoutGateway.Keys)
{

    Select-AzureSubscription -SubscriptionName $Subscription
    $xmlDoc = [xml]$VNetConfigsWithoutGateway[$Subscription]
    Create-LocalNetworkSiteNode -Name "fakeLocal" -AddressPrefix "192.168.255.0/24" -VPNGatewayAddress "1.0.0.0" -XmlObj $xmlDoc
    Create-GatewayNodes -GatewayAddressPrefix $VNets[$Subscription].GatewaySubnet -VNetName $VNets[$Subscription].VNetName -XmlObj $xmlDoc
    Create-ConnectionsToLocalNetworkNode -LocalNetworkName "fakeLocal" -VNetName $VNets[$Subscription].VNetName -XmlObj $xmlDoc
    $configPath = $Subscription + "_NetConfig.xml"
    $xmlDoc.Save($configPath)

    Set-AzureVNetConfig -ConfigurationPath $configPath
    foreach($i in 0..2)
    {
        Try
        {
            New-AzureVNetGateway -VNetName $VNets[$Subscription].VNetName -GatewayType DynamicRouting
            break;
        }
        Catch [System.Exception]
        {
            Write-Host "Caught a system exception during creating Gateway for $Subscription"
        }
    }


    $config = Get-AzureVNetConfig 
    $VNetConfigsWithGateway[$Subscription] = $config.XMLConfiguration
    $VNetGateways[$Subscription] = (Get-AzureVNetGateway -VNetName $VNets[$Subscription].VNetName).VIPAddress
}


#config all the subscriptions
foreach($Subscription in $VNetConfigsWithGateway.Keys)
{
    Select-AzureSubscription -SubscriptionName $Subscription
    $xmlDoc = [xml]$VNetConfigsWithGateway[$Subscription]

    $xmlDoc.NetworkConfiguration.VirtualNetworkConfiguration.RemoveChild($xmlDoc.NetworkConfiguration.VirtualNetworkConfiguration.LocalNetworkSites)


    $vnetNode = $xmlDoc.NetworkConfiguration.VirtualNetworkConfiguration.VirtualNetworkSites.VirtualNetworkSite | where {$_.name -eq $VNets[$Subscription].VNetName}
    if(-not $vnetNode)
    {
        Write-Host "Can't find VNetSite in config of $Subscription"
        continue;
    }

    $vnetNode.RemoveChild($vnetNode.Gateway)


    $localNetNames = @()
    foreach($VNet in $VNets.Values)
    {

        $localNetworkName = $VNets[$Subscription].VNetName + "_" + $VNet.VNetName
        if($VNet.Subscription -eq $Subscription)
        {
            continue;
        }
        else
        {
            Create-LocalNetworkSiteNode -Name $localNetworkName -AddressPrefix $VNet.AddressPrefix -VPNGatewayAddress $VNetGateways[$VNet.Subscription] -XmlObj $xmlDoc
            Create-ConnectionsToLocalNetworkNode -LocalNetworkName $localNetworkName -VNetName $VNets[$Subscription].VNetName -XmlObj $xmlDoc
            $localNetNames += $localNetworkName
        }
    }


    $configPath = $Subscription + "_NetConfig.xml"
    $xmlDoc.Save($configPath)

    Set-AzureVNetConfig -ConfigurationPath $configPath

    foreach($localName in $localNetNames)
    {
        Set-AzureVNetGatewayKey -VNetName $VNets[$Subscription].VNetName -LocalNetworkSiteName $localName -SharedKey $SharedKey
    }

    Get-AzureVNetConnection -VNetName $VNets[$Subscription].VNetName

}





