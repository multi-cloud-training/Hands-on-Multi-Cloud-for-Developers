provider "aws" {
  region = "us-east-1"
  assume_role {
    role_arn     = "arn:aws::${var.masterAccountId}:role/${var.masterAccountRole}"
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

data "aws_iam_policy_document" "ec2_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "spinnaker_policy" {
  name        = "SpinnakerServerPolicy"
  policy      = "${data.aws_iam_policy_document.spinnaker_iam_policy.json}"
  path        = "/spinnaker"
  description = "Policy allowing Spinnaker to do actions in various other accounts"
}

resource "aws_iam_role" "spinnaker_role" {
  name               = "SpinnakerServerRole"
  path               = "/spinnaker"
  description        = "Role allowing spinnaker to do operations on systems"
  assume_role_policy = "${data.aws_iam_policy_document.ec2_assume_role_policy.json}"
}


resource "aws_iam_role_policy_attachment" "spinnaker_server_role" {
  role       = "${aws_iam_role.spinnaker_role.name}"
  policy_arn = "${aws_iam_policy.spinnaker_policy.arn}"
}

resource "aws_iam_instance_profile" "profile_for_role" {
  role = "${aws_iam_role.spinnaker_role.arn}"
  name      = "SpinnakerInstanceProfile"
}

###############################################################
## NORMALLY would suggest NOT creating these here but in a base account configuration.  VPC's tend to be a more complicated discussion
## point particularly if you need to peer resources deal with IPAM and allocation and similar concepts.  BUT for the purposes of a test...
###############################################################
module "vpc" {
  source = "terraform-aws-modules/vpc/aws"

  name = "ManagementVpc"
  cidr = "10.0.0.0/22"

  azs             = ["us-east-1a", "us-east-1b"]
  public_subnets  = ["10.0.253.0/24", "10.0.254.0/24"]

  enable_nat_gateway = true
  enable_vpn_gateway = false

  public_subnet_tags = {
    name               = "ManagementVpc.external.us-east-1"
    immutable_metadata = "{'purpose':'external'}"
  }
}

resource "aws_key_pair" "deployer" {
  key_name   = "spinnaker-deploy-key"
  public_key = "${file(pathexpand("~/.ssh/id_rsa.pub"))}"
}

data "aws_ami" "ubuntu" {
  most_recent = true

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-xenial-16.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  owners = ["099720109477"] # Canonical
}
data "http" "icanhazip" {
   url = "http://icanhazip.com"
}
resource "aws_security_group" "allow_ssh_web_to_spinnaker" {
  ingress {
    self = true
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["${chomp(data.http.icanhazip.body)}/32"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  vpc_id = "${module.vpc.vpc_id}"
}

resource "aws_instance" "halyard_and_spinnaker_server" {
  ami                    = "${data.aws_ami.ubuntu.id}"
  instance_type          = "m5.xlarge"
  key_name               = "${aws_key_pair.deployer.id}"
  vpc_security_group_ids = ["${aws_security_group.allow_ssh_web_to_spinnaker.id}"]
  iam_instance_profile   = "${aws_iam_instance_profile.profile_for_role.arn}"
  subnet_id = "${module.vpc.public_subnets[0]}"
  user_data = "${file("${path.module}/files/user-data.sh")}"
}

data "aws_region" "current" {}

module "sub_account" {
  source = "modules/managed_account"
  region = "${data.aws_region.current.name}"
  ## Defines the role used to create resources in remote account and grant MASTER account access to assume a spinnaker role in that account
  managed_account_role = "${var.subAccountRole[0]}"
  master_account_root = "arn:aws::${var.masterAccountId}:root"
}

