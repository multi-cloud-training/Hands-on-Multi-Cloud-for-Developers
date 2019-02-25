resource "azurerm_network_security_group" "cisco_sg" {
    name = "${var.rg_name}"
    location = "${var.azure_region}"
    resource_group_name = "${data.azurerm_resource_group.rg.name}"
    security_rule {
        name = "AllowSSH"
        priority = 100
        direction = "Inbound"
        access = "Allow"
        protocol = "Tcp"
	source_port_range = "*"
        destination_port_range = "22"
        source_address_prefix = "Internet"
	destination_address_prefix = "*"
    }
    security_rule {
        name = "AllowUDP500"
        priority = 101
        direction = "Inbound"
        access = "Allow"
        protocol = "Udp"
	source_port_range = "*"
        destination_port_range = "500"
        source_address_prefix = "Internet"
	destination_address_prefix = "*"
    }
    security_rule {
        name = "AllowUDP4500"
        priority = 102
        direction = "Inbound"
        access = "Allow"
        protocol = "Udp"
	source_port_range = "*"
        destination_port_range = "4500"
        source_address_prefix = "Internet"
	destination_address_prefix = "*"
    }
    security_rule {
        name = "AllowESP"
        priority = 103
        direction = "Inbound"
        access = "Allow"
        protocol = "*"
	source_port_range = "*"
        destination_port_range = "*"
        source_address_prefix = "VirtualNetwork"
	destination_address_prefix = "*"
    }
}
