# Multi-Cloud Demo

This configuration provisions infrastructure on Amazon Web Services and Google Cloud Platform to demonstrate the cross-provider provisioning functionality available in Terraform.

For AWS: aws.tf uses modules from the [Terraform Module Registry][terraform_registry_aws] to provision a VPC, the necessary networking components and an auto scaling group across multiple AZs. The associated launch configuration launches three instances of the latest Amazon Linux AMI then installs httpd and a custom landing page via a user data script.

For Google: google.tf uses locally defined resources to provision a Managed Instance Group in the default VPC and network, spanning multiple zones. The group configuration launches three VMs running CentOS 7 then installs httpd and a custom landing page via a startup script.

> The regions chosen for this demo are hardcoded to AWS eu-west-1 and GCP us-west1

## Estimated Time to Complete

20 minutes.

## Prerequisites

### AWS

* An AWS Access Key and AWS Secret Access Key should be configured on the host running this Terraform configuration. Environment variables are one way to achieve this, eg:

    ```sh
    export AWS_ACCESS_KEY_ID=XXXXXXXXXXXXXXXXXXXX
    export AWS_SECRET_ACCESS_KEY=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ```

> See the  ['Configuring the AWS CLI'][aws_cli_config] documentation for guidance

### GCP

* Google credentials should be present on the host running this Terraform configuration. Environment variables are one way to acieve this, eg:

    ```sh
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/google_credentials.json
    ```

> See the ['Getting Started with Authentication'][getting_started_with_gcp] GCP documentation for guidance

## Steps

1. Initialise Terraform to download the required dependencies:

    `terraform init`

1. Execute a plan of the Terraform configuration:

    `terraform plan -out=1.tfplan`

1. Execute an apply of the Terraform configuration:

    `terraform apply 1.tfplan`

### Notes

To destroy the resources provisioned in this example run:

```sh
terraform plan -out=d.tfplan -destroy
terraform apply d.tfplan
```

[terraform_registry_aws]: https://registry.terraform.io/browse?provider=aws
[aws_cli_config]: http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html
[getting_started_with_gcp]: https://cloud.google.com/docs/authentication/getting-started
