variable "managed_account_role" {
  type = "string" 
}

variable "master_account_root" {
  type = "string"
}
variable "region" {
  type = "string"
}

provider "aws" {
  region = "${var.region}"
  assume_role {
    role_arn     = "${var.managed_account_role}"
    session_name = "SpinnakerCreation"
  }
}

data "aws_iam_policy_document" "spinnaker_iam_policy" {
  statement {
    actions = [
      "iam:*",
      "ec2:*",
      "s3:*",
      "sts:PassRole",
      "sts:AssumeRole",
    ]

    effect   = "Allow"
    resources = ["*"]
  }
}

data "aws_iam_policy_document" "allow_spinnaker_account_to_access" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "AWS"
      identifiers = ["${var.master_account_root}"]
    }
  }
}


resource "aws_iam_role" "spinnaker_access_role" {
  name               = "SpinnakerCrossAccountRole"
  path               = "/spinnaker"
  description        = "Role allowing spinnaker to do operations on systems"
  assume_role_policy = "${data.aws_iam_policy_document.allow_spinnaker_account_to_access.json}"
}


resource "aws_iam_policy" "spinnaker_access_policy" {
  name        = "SpinnakerAccessPolicy"
  policy      = "${data.aws_iam_policy_document.spinnaker_iam_policy.json}"
  path        = "/spinnaker"
  description = "Policy allowing Spinnaker to do actions in THIS account"
}

resource "aws_iam_role_policy_attachment" "spinnaker_access_role_attachment" {
  role       = "${aws_iam_role.spinnaker_access_role.name}"
  policy_arn = "${aws_iam_policy.spinnaker_access_policy.arn}"
}




