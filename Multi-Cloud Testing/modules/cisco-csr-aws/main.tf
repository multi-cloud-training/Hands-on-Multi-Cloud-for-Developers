# Specify the provider and access details
provider "aws" {
  profile = "${var.aws_profile}"
  region = "${var.aws_region}"
}

data "aws_ami_ids" "cisco_csr" {
  # Retrieves the AMI within the region that the VPC is created
  # Cost: It takes roughly ~50 seconds to perform this query of the ami
  # Owner: Cisco 
  owners = ["679593333241"]

  filter {
    name   = "name"
    values = ["cisco-ic_CSR_*-AMI-SEC-HVM-*"]
  }
  filter {
    name   = "description"
    values = ["cisco-ic_CSR_*-AMI-SEC-HVM"]
  }
  filter {
    name   = "is-public"
    values = ["true"]
  }
}

data "aws_vpc" "current" {
  id = "${var.vpc_id}"
}

data "aws_route_table" "current" {
  vpc_id    = "${var.vpc_id}"
  #subnet_id = "${var.subnet_id}"
}

resource "aws_route" "route" {
  route_table_id            = "${data.aws_route_table.current.id}"
  destination_cidr_block    = "${coalesce(var.destination_cidr, data.template_file.terraform-dcos-default-cidr.rendered)}"
  instance_id               = "${aws_instance.cisco.id}"
}

resource "aws_eip" "csr_public_ip" {
  # TODO(mbernadin) consideration HA
  # count = "${length(var.local_csr_public_ip) == "0" ? "1" : "0"}"
  vpc = true
  instance = "${aws_instance.cisco.id}"
}

resource "aws_instance" "cisco" {
  ami                         = "${data.aws_ami_ids.cisco_csr.ids[0]}"
  instance_type               = "${var.instance_type}"
  subnet_id                   = "${var.subnet_id}"
  associate_public_ip_address = true
  source_dest_check           = false
  key_name                    = "${var.ssh_key_name}"
  vpc_security_group_ids      = ["${aws_security_group.sg_g1_csr1000v.id}"]

  tags {
    Name = "Cisco CSR VPN Router"
  }
}

data "template_file" "terraform-dcos-default-cidr" {
  template = "$${cloud == "azure" ? "10.32.0.0/16" : cloud == "gcp" ? "10.64.0.0/16" : "undefined"}"

  vars {
    cloud = "${var.terraform_dcos_destination_provider}"
  }
}
