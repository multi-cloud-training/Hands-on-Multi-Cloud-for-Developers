# Specify the provider and access details
provider "aws" {
  profile = "${var.aws_profile}"
  region = "${var.aws_region}"
}

data "aws_availability_zones" "available" {}

data "aws_ami_ids" "cisco_csr" {
  # Retrieves the AMI within the region that the VPC is created
  # Cost: It takes roughly ~50 seconds to perform this query of the ami
  # Owner: Cisco 
  owners = ["679593333241"]

  filter {
    name   = "name"
    values = ["cisco-CSR-.16.06.01*"]
  }
  filter {
    name   = "description"
    values = ["cisco-CSR-*"]
  }
  filter {
    name   = "is-public"
    values = ["true"]
  }
}

data "aws_vpc" "current" {
  id = "${var.vpc_id}"
}

locals {
  public_aws_csr_subnet_cidr_block = "${join(".", list(element(split(".", data.aws_vpc.current.cidr_block),0), element(split(".", data.aws_vpc.current.cidr_block),1), var.public_subnet_subnet_suffix_cidrblock))}"
  public_aws_csr_private_ip = "${join(".", list(element(split(".", data.aws_vpc.current.cidr_block),0), element(split(".", data.aws_vpc.current.cidr_block),1), var.public_subnet_private_ip_address_suffix))}"
  private_aws_csr_subnet_cidr_block = "${join(".", list(element(split(".", data.aws_vpc.current.cidr_block),0), element(split(".", data.aws_vpc.current.cidr_block),1), var.private_subnet_subnet_suffix_cidrblock))}"
  private_aws_csr_private_ip = "${join(".", list(element(split(".", data.aws_vpc.current.cidr_block),0), element(split(".", data.aws_vpc.current.cidr_block),1), var.private_subnet_private_ip_address_suffix))}"
  public_aws_csr_gateway_ip = "${cidrhost(local.public_aws_csr_subnet_cidr_block, 1)}"
}

resource "aws_subnet" "public_reserved_vpn" {
  vpc_id     = "${data.aws_vpc.current.id}"
  cidr_block = "${local.public_aws_csr_subnet_cidr_block}"
  availability_zone = "${data.aws_availability_zones.available.names[0]}"
}

resource "aws_subnet" "private_reserved_vpn" {
  vpc_id     = "${data.aws_vpc.current.id}"
  cidr_block = "${local.private_aws_csr_subnet_cidr_block}"
  availability_zone = "${data.aws_availability_zones.available.names[0]}"
}

data "aws_route_table" "current" {
  vpc_id    = "${var.vpc_id}"
}

resource "aws_route" "route" {
  route_table_id            = "${data.aws_route_table.current.id}"
  destination_cidr_block    = "${coalesce(var.destination_cidr, data.template_file.aws-terraform-dcos-default-cidr.rendered)}"
  network_interface_id      = "${aws_instance.cisco.primary_network_interface_id}"
}

resource "aws_route_table_association" "a" {
  subnet_id      = "${aws_subnet.public_reserved_vpn.id}"
  route_table_id = "${data.aws_route_table.current.id}"
}

resource "aws_eip" "csr" {
  vpc = true
}

resource "aws_eip_association" "csr" {
  allocation_id = "${aws_eip.csr.id}"
  network_interface_id  = "${aws_instance.cisco.primary_network_interface_id}"
}

resource "aws_network_interface" "csr" {
  subnet_id       = "${aws_subnet.private_reserved_vpn.id}"
  private_ips      = ["${local.private_aws_csr_private_ip}"]
  security_groups = ["${aws_security_group.sg_g1_csr1000v.id}"]
  source_dest_check = "false"

  attachment {
    instance     = "${aws_instance.cisco.id}"
    device_index = 1
  }
}

resource "aws_instance" "cisco" {
  ami                         = "${data.aws_ami_ids.cisco_csr.ids[0]}"
  instance_type               = "${var.aws_instance_type}"
  subnet_id                   = "${aws_subnet.public_reserved_vpn.id}"
  private_ip                  = "${local.public_aws_csr_private_ip}"
  associate_public_ip_address = true
  source_dest_check           = "false"
  key_name                    = "${var.ssh_key_name}"
  vpc_security_group_ids      = ["${aws_security_group.sg_g1_csr1000v.id}"]
  user_data                   = <<CONFIG
ios-config-1="username ${var.cisco_user} privilege 15 password ${var.cisco_password}"
ios-config-2="ip domain lookup"
ios-config-3="ip domain name cisco.com"
CONFIG

  tags {
    Name = "Cisco CSR VPN Router"
    owner = "${var.owner}"
    expiration = "${var.expiration}"
  }
}

data "template_file" "aws_ssh_template" {
   template = "${file("${path.module}/ssh-deploy-script.tpl")}"

   vars {
    cisco_commands = "${module.aws_csr_userdata.userdata_ssh_emulator}"
    cisco_hostname = "${local.public_aws_csr_private_ip}"
    cisco_password = "${var.cisco_password}"
    cisco_user    = "${var.cisco_user}"
   }
}

resource "null_resource" "aws_ssh_deploy" {
  triggers {
    cisco_ids = "${aws_instance.cisco.id}"
    instruction = "${data.template_file.aws_ssh_template.rendered}"
  }
  connection {
    host = "${var.aws_docker_utility_node}"
    user = "${var.aws_docker_utility_node_username}"
  }

  provisioner "file" {
    content     = "${data.template_file.aws_ssh_template.rendered}"
    destination = "aws-cisco-config.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "sudo chmod +x aws-cisco-config.sh",
      "sudo ./aws-cisco-config.sh"
    ]
  }
}

module "aws_csr_userdata" {
  source = "../cisco-config-generator"
  public_subnet_private_ip_local_site  = "${local.public_aws_csr_private_ip}"
  public_subnet_private_ip_network_mask = "${cidrnetmask(local.public_aws_csr_subnet_cidr_block)}"
  private_subnet_private_ip_local_site  = "${local.private_aws_csr_private_ip}"
  private_subnet_private_ip_network_mask = "${cidrnetmask(local.private_aws_csr_subnet_cidr_block)}"
  public_subnet_private_ip_cidr_remote_site_network_mask = "${cidrnetmask(data.template_file.aws-terraform-dcos-default-cidr.rendered)}"
  public_subnet_private_ip_cidr_remote_site  = "${element(split("/", data.template_file.aws-terraform-dcos-default-cidr.rendered),0)}"
  public_subnet_public_ip_remote_site  = "${coalesce(var.public_subnet_public_ip_remote_site, azurerm_public_ip.cisco.ip_address)}"
  public_gateway_ip = "${local.public_aws_csr_gateway_ip}"
  tunnel_ip_local_site   = "${var.tunnel_ip_local_site}"
  tunnel_ip_remote_site  = "${var.tunnel_ip_remote_site}"
  local_hostname         = "${var.local_hostname}"
}

data "template_file" "aws-terraform-dcos-default-cidr" {
  template = "$${cloud == "azure" ? "10.32.0.0/16" : cloud == "gcp" ? "10.64.0.0/16" : "undefined"}"

  vars {
    cloud = "${var.remote_terraform_dcos_destination_provider}"
  }
}
