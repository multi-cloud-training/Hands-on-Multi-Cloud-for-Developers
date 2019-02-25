$action = $args[0] # preview, reset, swap
$resourceGroup = '<resource-group name here>'
$webapp = '<webapp name here>'
$slot = 'staging'

az webapp deployment slot swap --name $webapp --resource-group $resourceGroup --slot $slot --action $action

# example 
# .\swap-slots.ps1 "preview"
# .\swap-slots.ps1 "swap"
# .\swap-slots.ps1 "reset"