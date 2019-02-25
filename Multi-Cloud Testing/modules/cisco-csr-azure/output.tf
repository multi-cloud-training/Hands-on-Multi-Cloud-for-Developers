output "destination_cidr" {
  value = "${data.template_file.terraform-dcos-default-cidr.rendered}"
}

output "public_ip_address" {
  value = "${data.azurerm_public_ip.cisco.ip_address}"
}

output "private_ip_address" {
  value = "${azurerm_network_interface.cisco_nic.private_ip_address}"
}

output "password" {
  value = "${var.cisco_password}"
}

output "ssh_user" {
  value = "${var.cisco_user}"
}
