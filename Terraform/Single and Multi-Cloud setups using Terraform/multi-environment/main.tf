##################################################################################
# VARIABLES
##################################################################################

variable "aws_access_key" {}
variable "aws_secret_key" {}

#####################################################################
# Providers
#####################################################################
provider "google" {
  credentials = "${file("~/.config/gcloud/terraform-admin.json")}"
  project     = "terraform-demo-project"
  region      = "us-east1"
}

provider "aws" {
  access_key = "${var.aws_access_key}"
  secret_key = "${var.aws_secret_key}"
  region     = "us-east-1"
}

#####################################################################
# Resources
#####################################################################

resource "google_pubsub_topic" "terraform-demo" {
  name = "demo-topic"
}

resource "aws_lambda_function" "demo_lambda" {
  function_name    = "demo_lambda"
  handler          = "main"
  runtime          = "go1.x"
  filename         = "go-function/main.zip"
  source_code_hash = "${base64sha256(file("go-function/main.zip"))}"
  role             = "${aws_iam_role.lambda_exec_role.arn}"
}

resource "aws_iam_role" "lambda_exec_role" {
  name = "lambda_exec_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}
