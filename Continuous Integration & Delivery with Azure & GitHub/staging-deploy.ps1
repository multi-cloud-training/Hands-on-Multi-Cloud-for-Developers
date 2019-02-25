$branch=$args[0]
$repo = '<your repo url here>'
$resourceGroup = '<resource-group name here>'

az webapp deployment source delete --name ms-azure-github --resource-group ms-azure-githubRG --slot staging
az webapp deployment source config --name ms-azure-github --repo-url $repo --resource-group $resourceGroup --branch $branch --slot stagingwer

# example 
# .\staging-deploy.ps1 "release/1.0.2"