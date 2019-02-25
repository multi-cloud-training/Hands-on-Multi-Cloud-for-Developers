######################################################
#This is the script to automatically deploy the sample 
#application to your Azure environment.
#
#created by T.Yamada
#last update 26/9/2018
######################################################

#!/bin/bash


CUR=`pwd`
appname=$*
resourcegroup=PPM_tom_autoscript
serviceplan=freeplan

function cli_check(){
  if [ "$?" -eq 1 ];then
    echo "Message: Some error occurs."
    echo "Abnormal end!"
    exit
  fi
}

# check if the argument is suitable or not 
function args_check(){
  if [ "$#" -ne 1 ];then
    echo "Usage:"
    echo "   $0 [sub-domain name]"
    exit
  fi
}

#creating webapps service
#procedure
#(1) remove the resource group if exists.
#(2) create the new resource group.
#(3) create the new service plan.
#(4) create the webapp service.
#@params:
# $1:resource group name, $2:service plan name, $3:application name
function create_webapps(){
  echo "****creating webapps start!****"
#  az group delete --name $1 
  az group create --location eastus --name $1
  az appservice plan create --name $2 --resource-group $1 --sku FREE
  az webapp create --name $3 --resource-group $1 --plan $2
  echo "****creating webapps done!****"
}

#deploying the sample application to your azure environment
#procedure
#(1) change direcory to modules
#(2) zip the modules
#(3) deploy the application
#@params:
# $1:resource group name, $2:application name
function deploy(){
  echo "****deploying sample application start!****"
  cd $CUR/modules
  zip -r modules.zip *
  az webapp deployment source config-zip --resource-group $1 --name $2 --src modules.zip
  cli_check
  echo "****deploying sample application done!****"
}

#show deployed information
function show_results(){
  echo -e "\n[Deployed information]"
  echo "Auto-deployment to your azure environment is done!"
  echo "You can access https://"$1".azurewebsites.net"
}


args_check ${appname}
create_webapps ${resourcegroup} ${serviceplan} ${appname}
deploy ${resourcegroup} ${appname}
show_results ${appname,,}

