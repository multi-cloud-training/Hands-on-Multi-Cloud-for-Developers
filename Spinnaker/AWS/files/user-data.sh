#!/bin/sh
sudo apt update
sudo apt -yq install awscli
sudo apt -yq install unzip
sudo apt -yq install jq

curl -O https://raw.githubusercontent.com/spinnaker/halyard/master/install/debian/InstallHalyard.sh
sudo bash InstallHalyard.sh


