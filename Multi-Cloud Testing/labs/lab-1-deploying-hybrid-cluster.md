# Multi-Cloud Open DC/OS on AWS with Terraform
# AWS and Azure

Requirements
------------

-	[Terraform](https://www.terraform.io/downloads.html) 0.11.x

## Deploying Multi-Cloud DCOS 

This repository is meant to get the bare minimum of running a multi-cloud DC/OS cluster.

This repo is configured to deploy a cluster across AWS and Azure using Cisco CSR 1000V for VPN connection in between.

## I. Download Prerequisites 

1. Accept the AWS Cisco CSR subscription from the Marketplace (If you are working from Mesosphere's SE/SA account please skip this step) 
Click the link below with the same AWS account you will use

https://aws.amazon.com/marketplace/pp?sku=9vr24qkp1sccxhwfjvp9y91p1

2.  Accept the Azure Cisco CSR subscription from the marketplace (If you are working from Mesosphere's SE/SA account please skip this step)

```bash
az vm image accept-terms --urn "cisco:cisco-csr-1000v:16_6:16.6.120170804"
```

3.  Retrieve Sales Mesosphere License Key via OneLogin here: https://mesosphere.onelogin.com/notes/56317. You can also use any valid DC/OS Ent. license key

4.  Retrieve Sales Mesosphere Private and Public Key via OneLogin here: https://mesosphere.onelogin.com/notes/41130. (This is the standard Mesosphere SSH key, You may have this preconfigured already)

5.  Retrieve Mesosphere MAWS Commandline tool for access to AWS: https://github.com/mesosphere/maws/releases

6.  Retrieve Azure CLI tool for access to Azure: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest

7. Retrieve the AWS CLI from here http://docs.aws.amazon.com/cli/latest/userguide/installing.html

8. Download and install terraform from here https://www.terraform.io/downloads.html . (Or just use brew install terraform)

## II. Download the Hybridcloud Demo Terraform Module

```bash
mkdir hybridcloud-demo && cd hybridcloud-demo
terraform init -from-module github.com/bernadinm/hybrid-cloud
cp desired_cluster_profile.tfvars.example desired_cluster_profile.tfvars
```

## III. Configure Prerequisites

### Configure Mesosphere MAWS 

```bash
# Download maws-darwin binary from https://github.com/mesosphere/maws/releases
chmod +x maws*
sudo mv ~/Downloads/maws* /usr/local/bin/maws
```
If you are using maws for the first time, you need to list the account you have access too and login to one of them

Use this command

````
maws ls
````
Your output should look something like this:

```
You will now be taken to your browser for authentication. URL https://aws.mesosphere.com/sso?maws=56014
2 available Accounts:
Team 03:
	110465657741_Mesosphere-PowerUser

Team 10:
	273854932432_Mesosphere-PowerUser
````
Now use maws to login to retrieve your temporary account credentials

```
eval $(maws login 110465657741_Mesosphere-PowerUser)
```

Your output should look something like this
```
retrieved credentials writing to profile 110465657741_Mesosphere-PowerUser
```

Note that you will have to refresh the credentials (use the maws login command) every 1 hour

### Configure SSH Private and Public Key for Terraform

Set your ssh agent locally to point to your pem key and public key

```bash
$ ssh-add /path/to/ssh_private_key.pem
```

AWS requires you to have an existing keypair set per region. Please set this keypair name so Terraform knows which one to use.

```bash
$ cat desired_cluster_profile.tfvars | grep ssh_key_name
ssh_key_name = "<AWS_KEYPAIR_NAME_BY_REGION>"
```

Azure requires you to provide the public SSH key directly. Please provide this public key so Terraform can pass this to Azure to use. 

```bash
$ cat desired_cluster_profile.tfvars | grep ssh_pub_key
ssh_pub_key = "<INSERT_SSH_PUB_KEY>"
```

*Note*: If any of these entries do not exist in the file, you can add them directly in your desired_cluster_profile.tfvars.

### Configure Mesosphere License Key in Terraform variables file

In the `desired_cluster_profile.tfvars` file in your hybridcloud-demo folder, Copy your license and place it in the `dcos_license_key_contents` variable

To double check 

```bash
$ cat desired_cluster_profile.tfvars | grep dcos_license_key_contents
dcos_license_key_contents = "<MY_LICENSE_KEY>"
```

### Configure your aws_profile in Terraform variables file

Copy you Mesosphere `maws` profile name and provide it to terraform. For the sales team, it is already known to be `110465657741_Mesosphere-PowerUser` so it will look like this below:

To double check 

```bash
$ cat desired_cluster_profile.tfvars | grep aws_profile
aws_profile = "110465657741_Mesosphere-PowerUser"
```

### Configure your Azure login for Terraform

```bash
$ az login
```
Output should be something like  
```
To sign in, use a web browser to open the page https://microsoft.com/devicelogin and enter the code AXUDMJZWB to authenticate.
```
Note that the code above refreshes every time.

Open the browser link above and enter the code, use your Azure credential to login Once you enter your credential, go back to the terminal. you should see something like 

```
[
  {
    "cloudName": "AzureCloud",
    "id": "6bfddfe6-078b-4a9d-86ff-52e86464efe0",
    "isDefault": true,
    "name": "Mesosphere Sales",
    "state": "Enabled",
    "tenantId": "a98e3fd4-b172-4731-9f98-7038f712e693",
    "user": {
      "name": "amr@azuremesosphere.onmicrosoft.com",
      "type": "user"
    }
  },

```


## IV. Deploy Your First Hybrid Cluster

You are now ready to deploy your hybrid cluster!!


```bash
$ terraform apply -var-file desired_cluster_profile.tfvars
```

Now terraform should start deploying your cluster. This will likely take 10~20 minutes depending on the size of the cluster and the speed of cloud provider region 


Here is an output of a successful deployment:

```
Apply complete! Resources: 114 added, 0 changed, 0 destroyed.

Outputs:

AWS Cisco CSR VPN Router Public IP Address = 18.206.133.140
Azure Cisco CSR VPN Router Public IP Address = 40.118.230.34
Bootstrap Host Public IP = mbernadin-tf80cc-bootstrap.westus.cloudapp.azure.com
Bootstrap Public IP Address = 34.229.179.46
Master ELB Public IP = mbernadin-tf80cc-pub-mas-elb-1841202780.us-east-1.elb.amazonaws.com
Master Public IPs = [
    52.204.155.230
]
Private Agent Public IPs = [
    mbernadin-tf80cc-agent-1.westus.cloudapp.azure.com
]
Public Agent ELB Address = mbernadin-tf80cc-pub-agt-elb-1182904022.us-east-1.elb.amazonaws.com
Public Agent ELB Public IP = public-agent-mbernadin-tf80cc.westus.cloudapp.azure.com
Public Agent Public IPs = [
    mbernadin-tf80cc-public-agent-1.westus.cloudapp.azure.com
]
ssh_user = core
```

### Destroy Cluster

For the purpose of this lab we will be keeping our cluster up and running, but if you needed to destroy your cluster for any reason now, here is the command: 

```bash
terraform destroy -var-file desired_cluster_profile.tfvars
```

Note: No major enhancements should be expected with this repo. It is meant for demo and testing purposes only.

### Navigation

1. LAB1 - Deploying AWS Using Terraform (current)
2. [LAB2 - Bursting from AWS to Azure](./lab-2-bursting-from-aws-to-azure.md)
3. [LAB3 - Deploying and Migrating Stateless App from AWS to Azure](./lab-3-deploying-and-migrating-stateless-app.md)
4. [LAB4 - Deploying Cassandra Multi DataCenter](./lab-4-deploying-cassandra-multi-dc-cluster.md)

[Return to Main Page](../README.md)
