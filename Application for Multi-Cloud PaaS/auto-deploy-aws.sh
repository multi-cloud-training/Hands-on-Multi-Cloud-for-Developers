######################################################
#This is the script to automatically deploy the sample 
#application to your AWS environment.
#
#created by T.Yamada
#last update 27/8/2018
######################################################

#!/bin/bash


CUR=`pwd`
subDomainName=$*
appname=${subDomainName}-app
region="us-west-2"

function cli_check(){
  if [ "$?" -ne 0 ];then
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

#initialize EB CLI project
#procedure
#(1) change directory to modules
#(2) initialize EB CLI project.
#@params:
# $1:application name $2:region
function initialize(){
  echo "****initialization start!****"
  cd $CUR/modules
  eb init $1 --platform "arn:aws:elasticbeanstalk:$2::platform/Tomcat 8.5 with Java 8 running on 64bit Amazon Linux/3.0.3" --region $2
  cli_check
  echo "****initialization done!****"
}

#deploying the sample application to your aws environment
#procedure
#(1) deploy the application
#@params:
# $1:subdomain name $2:region
function deploy(){
  echo "****deploying sample application start!****"
  eb create $1 --cname $1 --elb-type "application" --region $2
  cli_check
  echo "****deploying sample application done!****"
}

#show deployed information
function show_results(){
  echo -e "\n[Deployed information]"
  echo "Auto-deployment to your aws environment is done!"
  echo You can access http://${1}.${2}.elasticbeanstalk.com
}


args_check ${subDomainName}
initialize ${appname} ${region}
deploy ${subDomainName} ${region}
show_results ${subDomainName,,} ${region}

