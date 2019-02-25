# Multi-cloud VPN with AWS and GCP

Terraform templates to establish VPN connection between AWS and GCP.

## Setup

Export your AWS credentials:

```
export AWS_ACCESS_KEY_ID=YOUR_AWS_KEY_ID
export AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_KEY
```

Export your google credentials per [terraform docs](https://www.terraform.io/docs/providers/google/index.html#authentication-json-file):

```
export GOOGLE_PROJECT=$(gcloud config get-value project)
export GOOGLE_CREDENTIALS=$(cat ~/.config/gcloud/${USER}-*.json)
```

## Run Terraform

There are a few parameters in the AWS Customer Configuration that cannot be extracted from the terraform attributes, a makefile automates the extraction of these fields and mananges a terraform.tfvars file.

First, add your `EC2_SSH_PUB_KEY` to the `terraform.tfvars` file so that you can ssh into the EC2 instance later:

```
echo "EC2_SSH_PUB_KEY = \"$(cat ~/.ssh/google_compute_engine.pub)\"" >> terraform.tfvars
```

Now, run the make target to provision the infrastructure:

```
make
```

## SSH Into the Instances

The init scripts for the EC2 and GCE instsances automatically install and run iperf3 in server mode listening on port 80.

Run `terraform output` to see the IP adddresses used below.

Follow the steps below to ssh into either of the instances:

For GCP:

```
gcloud compute ssh --zone us-central1-a us-central1-iperf
```

For EC2:

```
ssh -i ~/.ssh/google_compute_engine ubuntu@ec2_instance_public_ip
```

> Replace `ec2_instance_public_ip` with public IP of your ec2 instance.

## Run iperf3

```
export TARGET=IP_OF_OTHER_INSTANCE
sudo iperf3 -c $TARGET -i 1 -t 60 -V -p 80
```

> Replace IP_OF_OTHER_INSTANCE with the internal IP of the instance you are targeting.

## References

- [Using Cloud VPN With Amazon Web Services Guide](https://cloud.google.com/files/CloudVPNGuide-UsingCloudVPNwithAmazonWebServices.pdf)
