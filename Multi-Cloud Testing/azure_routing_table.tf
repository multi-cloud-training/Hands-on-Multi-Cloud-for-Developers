resource "azurerm_route_table" "private" {
# TODO(mbernadin): current data azurerm_subnet does not support associating
# existing resources with routing tables. Creating this one to make hybrid cloud
# work explicitly 
    name = "dcos_cisco_vpn_route_table"
    location = "${var.azure_region}"
    resource_group_name = "${azurerm_resource_group.dcos.name}"

    route {
        name = "CiscoRouter"
        address_prefix = "${aws_vpc.default.cidr_block}"
        next_hop_type = "VirtualAppliance"
        next_hop_in_ip_address = "${module.aws_azure_cisco_vpn_connecter.private_azure_csr_private_ip}"
    }
}
