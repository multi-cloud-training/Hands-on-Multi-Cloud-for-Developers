
# Deploying Spinnaker with Terraform

Clone the repository:

```
git clone https://github.com/danisla/spinnaker-terraform.git
cd spinnaker-terraform/
```

Export the Google SDK environment variables for Terraform:

```
export GOOGLE_REGION=$(gcloud config get-value compute/region)
export GOOGLE_PROJECT=$(gcloud config get-value project)
```

Add default jenkins password to tfvars file:

```
echo "jenkins_password = \"$(openssl rand -base64 15)\"" >> terraform.tfvars
```

Create the remote backend on GCS and the `backend.tf` file:

```
export TF_BACKEND_BUCKET=${GOOGLE_PROJECT}-terraform
```

```
gsutil mb gs://${TF_BACKEND_BUCKET}
```

```
cat > backend.tf <<EOF
terraform {
  backend "gcs" {
    bucket = "${TF_BACKEND_BUCKET}"
    path   = "spinnaker/terraform.tfstate"
  }
}
EOF
```

Initialize and preview Terraform actions:

```
terraform init
terraform plan
```

Run Terraform:

```
terraform apply
```
