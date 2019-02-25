#Spinnaker setup in AWS


Creates a spinnaker environment including a single debian based spinnaker release that can deploy to a sub account(s).  The sub accounts will be setup with an IAM role granting the "Master" account permissions to assume that role.  Spinnaker/Halyard instance will be built in a new VPC in the "master" account and granted access to assume any role.  This will allow it to assume the "sub account" roles. 

** WARNING ** This creates a brand new VPC for your application.  This may or may not be a good idea depending upon your environment.  This is for demo purposes ONLY.  FURTHER the instance that is spun up will be PUBLICLY accessible, though restricted to your IP address.

## SETUP:
1.  In variables.tf set defaults for the various accounts.  
2.  Make sure you run terraform plan as a user who can assume the "Master" role, and that "Master" role can access roles in your "Sub" accounts.
3.  terraform apply
4.  ssh ubuntu@<ip> (see output of the terraform apply)

## Setup chaos monkey
ON the server:

sudo apt install golang-go
go get github.com/netflix/chaosmonkey/cmd/chaosmonkey
#### Create /etc/chaosmonkey/chaosmonkey.taml
#### https://netflix.github.io/chaosmonkey/How-to-deploy/
chaosmonkey migrate

