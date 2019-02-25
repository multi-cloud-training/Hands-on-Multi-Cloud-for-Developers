######################################################
#This is the script to automatically deploy the sample 
#application to your Predix environment.
#
#created by T.Yamada
#last update 27/8/2018
######################################################

#!/bin/bash


CUR=`pwd`
appname=$*

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

#creating the manifest file
#procedure
#(1) remove manifest file
#(2) create the new manifest file according to the argument.
function create_manifest(){
  echo "****creating manifest file start!****"
  rm -rf  $CUR/manifest.yml
  echo "---" >> $CUR/manifest.yml
  echo "applications:" >> $CUR/manifest.yml
  echo "  - name: " $1 >> $CUR/manifest.yml
  echo "    memory: 256MB" >> $CUR/manifest.yml
  echo "    buildpack: staticfile_buildpack" >> $CUR/manifest.yml
  echo "****creating manifest file done!****"
}

#pushing sample application to your predix environment
#procedure
#(1) change direcory to modules
#(2) push using the manifest file
function push(){
  echo "****pushing sample application start!****"
  cd $CUR/modules
  cf push -f $CUR/manifest.yml
  cli_check
  echo "****pushing sample application done!****"
}

#show deployed information
function show_results(){
  echo -e "\n[Deployed information]"
  echo "Auto-deployment to your predix environment is done!"
  echo "You can access https://"$1".run.aws-usw02-pr.ice.predix.io"
}


args_check ${appname}
create_manifest ${appname}
push
show_results ${appname,,}

