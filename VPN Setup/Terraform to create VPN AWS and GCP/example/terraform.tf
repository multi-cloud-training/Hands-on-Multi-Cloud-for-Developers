# Create a VPC to launch our instances into
resource "aws_vpc" "default" {
  cidr_block           = "10.0.0.0/16"
  cidr_block           = "${var.vpc_cidr_block}"
  enable_dns_hostnames = true

  tags {
    "Name" = "${var.namespace}"
  }
}

# Create an internet gateway to give our subnet access to the outside world
resource "aws_internet_gateway" "default" {
  vpc_id = "${aws_vpc.default.id}"

  tags {
    "Name" = "${var.namespace}"
  }
}

# Grant the VPC internet access on its main route table
resource "aws_route" "internet_access" {
  route_table_id         = "${aws_vpc.default.main_route_table_id}"
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = "${aws_internet_gateway.default.id}"
}

# Create a subnet to launch our instances into
resource "aws_subnet" "default" {
  count                   = "${length(var.private_subnets)}"
  vpc_id                  = "${aws_vpc.default.id}"
  availability_zone       = "${var.azs[count.index]}"
  cidr_block              = "${var.private_subnets[count.index]}"
  map_public_ip_on_launch = true

  tags {
    "Name" = "${var.namespace}"
  }
}

module "vpn" {
  source = "nicholasjackson/aws/"

  aws_cidr = "10.0.0.0/16"
  gcp_cidr = "10.128.0.0/20"

  aws_region = "eu-west-1"
  gcp_region = "us-east-1"

  aws_vpc            = "${aws_vpc.default.id}"
  aws_sg             = "${aws_security_group.allow_nomad.id}"
  aws_route_table_id = "${aws_vpc.default.main_route_table_id}"
}
