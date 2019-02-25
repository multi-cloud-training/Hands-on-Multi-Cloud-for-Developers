#!/bin/bash
MONGO_POD_NAME=`kubectl get pods | grep db-deploy | awk '{print $1}'`
echo $MONGO_POD_NAME
kubectl version
kubectl exec \-it $MONGO_POD_NAME bash './import.sh'