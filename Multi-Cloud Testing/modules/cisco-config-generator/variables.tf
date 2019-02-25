variable "public_gateway_ip" { default = ""}
variable "public_subnet_private_ip_local_site" {}
variable "public_subnet_public_ip_remote_site" {}
variable "public_subnet_private_ip_network_mask" {}
variable "private_subnet_private_ip_network_mask" {}
variable "private_subnet_private_ip_local_site" {}
variable "public_subnet_private_ip_cidr_remote_site" {}
variable "public_subnet_private_ip_cidr_remote_site_network_mask" {}

variable "remote_pre_share_key" {
  default = "cisco123"
}

variable "local_pre_share_key" {
  default = "cisco123"
}

variable "tunnel_ip_local_site" {
  default = "172.16.0.1"
}

variable "tunnel_ip_remote_site" {
  default = "172.16.0.2"
}
variable "local_hostname" {
  default = "CSR1"
}
