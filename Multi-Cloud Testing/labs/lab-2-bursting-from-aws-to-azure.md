## Bursting into Azure from AWS

In the previous lab we've configured a cluster with small # of DC/OS instances. In this lab we will increase the # of nodes on one of the cloud providers (Azure) to simulate bursting and expanding capacity to the cloud 

### Adding or Removing Remote Nodes or Default Region Nodes

Change the number of remote nodes in the desired cluster profile.

1. Open your `desired_cluster_profile.tfvars`
2. Change the following variables

 * aws_group_{n}_private_agent_az from 1 to 3 (per zone)
  
 * number of aws_group_{n}_public_agent_az from 1 to 3
  
 * num_of_azure_private_agents from 1 to 5

At the end of your change it should look like this:

```bash 
dcos_version = "1.12.1"
num_of_masters = "1"
aws_region = "us-east-1"
aws_master_instance_type = "m4.xlarge"
aws_agent_instance_type = "m4.xlarge"
aws_public_agent_instance_type = "m4.xlarge"
aws_private_agent_instance_type = "m4.xlarge"
aws_bootstrap_instance_type = "m4.xlarge"
# ---- Private Agents Zone / Instance
aws_group_1_private_agent_az = "a"
aws_group_2_private_agent_az = "b"
aws_group_3_private_agent_az = "c"
num_of_private_agent_group_1 = "3"
num_of_private_agent_group_2 = "3"
num_of_private_agent_group_3 = "3"
# ---- Public Agents Zone / Instance
aws_group_1_public_agent_az = "a"
aws_group_2_public_agent_az = "b"
aws_group_3_public_agent_az = "c"
num_of_public_agent_group_1 = "0"
num_of_public_agent_group_2 = "0"
num_of_public_agent_group_3 = "1"
# ----- Remote Region Below
azure_region = "UK South"
num_of_azure_private_agents = "5"
num_of_azure_public_agents  = "1" 
azure_public_agent_instance_type = "Standard_D3_v2"
azure_agent_instance_type = "Standard_D3_v2"
azure_bootstrap_instance_type = "Standard_D3_v2"
# ----- DCOS Config Below
dcos_cluster_name = "Hybrid-Cloud"
aws_profile = "110465657741_Mesosphere-PowerUser"
dcos_license_key_contents = "<INSERT_LICENSE_HERE>"
ssh_pub_key = "<INSERT_SSH_PUB_KEY>"
```

3. Save the file and now you can burst out by performing the `terraform apply <args>` below:

```bash
terraform apply -var-file desired_cluster_profile.tfvars
```

### Navigation

1. [LAB1 - Deploying AWS Using Terraform](./lab-1-deploying-hybrid-cluster.md)
2. LAB2 - Bursting from AWS to Azure (current)
3. [LAB3 - Deploying and Migrating Stateless App from AWS to Azure](./lab-3-deploying-and-migrating-stateless-app.md)
4. [LAB4 - Deploying Cassandra Multi DataCenter](./lab-4-deploying-cassandra-multi-dc-cluster.md)

[Return to Main Page](../README.md)
