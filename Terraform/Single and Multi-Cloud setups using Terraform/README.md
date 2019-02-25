# Terraform Examples

A collection of example Terraform configurations to reference and run. Included are a single cloud example written for Amazon AWS, one for Google Cloud Platform, as well as one (working) example using both AWS and Google Cloud Platform.

More information about Terraform at [terraform.io](https://www.terraform.io/)

[Companion Presentation](https://docs.google.com/presentation/d/1W9FiugHDGD9NvGynLYQA8Qb6Ag3OBe92BTSwG0wk4Jc/edit?usp=sharing)

## Quickstart
- [Install terraform](https://www.terraform.io/intro/getting-started/install.html)
- Navigate to the desired example directory
- For examples that have AWS resources like `single-environment/aws` and `multi-environment`
  - Create a file called `terraform.tfvars` in the same directory as `main.tf` with the following credential information:
  
  ```
  # terraform.tfvars
  aws_access_key = <Your AWS Access Key>
  aws_secret_key = <Your AWS Secret Key>
  private_key_path = <Relative path to your generated EC2 ssh key>
  ```

- For examples that have GCP resources like `single-environment/gcp` and `multi-environment`
  - Set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to the json file containing your application credentials.
  
  ```bash
  export GOOGLE_APPLICATION_CREDENTIALS=<path to your *.json credentials file>
  ```

  - For the `multi-environment` examples, make sure to place your application credentials
   json file in the same directory, as you will need to package them in the archive being
   uploaded to AWS lambda.

- For all examples, after appropriate credentials have been set up:
  - `terraform init` to initialize Terraform's working files
  - `terraform plan` to review what changes will be made before they are applied
  - `terraform apply` to apply your changes
    - NOTE: in the case of a failure, Terraform will `NOT` rollback your changes.
    - use `destroy` to clean up any changes
  - `terraform destroy` to teardown all infrastructure created in the configuration
  
### Included Directories
  - `single-environment`: 
    - `aws`: Spins up a t2-micro instance on AWS EC2 and installs an nginx web server on it.
    - `gcp`: Spins up an f1-micro instance on GCE, provisions and attaches a pd-standard disk to it, and puts a basic network firewall in front.
  - `multi-environment`:
    - `go-function`: Creates a Google Pubsub topic on GCP and an AWS lambda function written in Go (`main.go`). When the function is invoked, it publishes a text payload to the created pubsub topic.
    - `node-function`: Creates a Google Pubsub topic on GCP and an AWS lambda function written in Node (`index.js`). When the function is invoked, it publishes a text payload to the created pubsub topic. (CURRENTLY NOT WORKING. Having issues compiling  Google Node libraries for Amazon Linux).



