resource "random_id" "shared_secret" {
  byte_length = 8
}

module "gcp" {
  source = "./gcp"

  namespace = "${var.namespace}"

  nomad_servers = "${var.nomad_servers}"
  nomad_agents  = "${var.nomad_agents}"

  consul_enabled        = true
  consul_version        = "${var.consul_version}"
  consul_join_tag_key   = "${var.consul_join_tag_key}"
  consul_join_tag_value = "${var.consul_join_tag_value}"
  consul_wan            = "consul.demo.gs"

  nomad_enabled = true
  nomad_version = "${var.nomad_version}"

  hashiui_enabled = false
  hashiui_version = "${var.hashiui_version}"
  gcp_region      = "${var.gcp_region}"
}

module "aws" {
  source = "./aws"

  namespace = "${var.namespace}"

  nomad_servers = "${var.nomad_servers}"
  nomad_agents  = "${var.nomad_agents}"

  consul_enabled        = true
  consul_version        = "${var.consul_version}"
  consul_join_tag_key   = "${var.consul_join_tag_key}"
  consul_join_tag_value = "${var.consul_join_tag_value}"

  nomad_enabled = true
  nomad_version = "${var.nomad_version}"

  hashiui_enabled = false
  hashiui_version = "${var.hashiui_version}"

  public_key_path = "~/.ssh/id_rsa_usbc.pub"
}

module "vpn" {
  source = "./vpn"

  aws_cidr = "${module.aws.cidr}"
  gcp_cidr = "${module.gcp.cidr}"

  aws_region = "${var.aws_region}"
  gcp_region = "${var.gcp_region}"

  shared_secret = "${random_id.shared_secret.hex}"

  aws_vpc            = "${module.aws.vpc_id}"
  aws_sg             = "${module.aws.security_group}"
  aws_route_table_id = "${module.aws.route_table_id}"
  aws_vpn_gateway    = "${module.aws.vpn_gateway}"
}

module "loadbalancer" {
  source                = "./loadbalancer"
  aws_lb                = "${module.aws.alb_dns}"
  gcp_lb                = "${module.gcp.alb_dns}"
  alb_internal_dns_name = "${module.aws.alb_internal_dns}"
  alb_internal_zone_id  = "${module.aws.alb_internal_zone_id}"
  route53_zone          = "${var.route53_zone}"
}

module "datadog" {
  source = "./datadog"
}
