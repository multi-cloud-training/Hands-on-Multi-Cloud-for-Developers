data "azurerm_resource_group" "rg" {
  name = "${var.rg_name}"
}

data "azurerm_virtual_network" "current" {
  name                = "${var.vnet_name}"
  resource_group_name = "${data.azurerm_resource_group.rg.name}"
}

resource "azurerm_subnet" "public" {
  name                 = "cisco-csr-subnet-public"
  virtual_network_name = "${data.azurerm_virtual_network.current.name}"
  resource_group_name  = "${data.azurerm_resource_group.rg.name}"
  address_prefix       = "${local.public_azure_csr_subnet_cidr_block}"
  route_table_id       = "${azurerm_route_table.private.id}"
}

resource "azurerm_subnet" "private" {
  name                 = "cisco-csr-subnet-private"
  virtual_network_name = "${data.azurerm_virtual_network.current.name}"
  resource_group_name  = "${data.azurerm_resource_group.rg.name}"
  address_prefix       = "${local.private_azure_csr_subnet_cidr_block}"
}

# Public IP addresses
locals {
  public_azure_csr_subnet_cidr_block = "${join(".", list(element(split(".", data.azurerm_virtual_network.current.address_spaces[0]),0), element(split(".", data.azurerm_virtual_network.current.address_spaces[0]),1), var.public_subnet_subnet_suffix_cidrblock))}"
  public_azure_csr_private_ip = "${join(".", list(element(split(".", data.azurerm_virtual_network.current.address_spaces[0]),0), element(split(".", data.azurerm_virtual_network.current.address_spaces[0]),1), var.public_subnet_private_ip_address_suffix))}"
  private_azure_csr_subnet_cidr_block = "${join(".", list(element(split(".", data.azurerm_virtual_network.current.address_spaces[0]),0), element(split(".", data.azurerm_virtual_network.current.address_spaces[0]),1), var.private_subnet_subnet_suffix_cidrblock))}"
  private_azure_csr_private_ip = "${join(".", list(element(split(".", data.azurerm_virtual_network.current.address_spaces[0]),0), element(split(".", data.azurerm_virtual_network.current.address_spaces[0]),1), var.private_subnet_private_ip_address_suffix))}"
}

resource "azurerm_route_table" "private" {
    name = "private_cisco_vpn_route_table"
    location = "${var.azure_region}"
    resource_group_name = "${data.azurerm_resource_group.rg.name}"

    route {
        name = "CiscoRouter"
        address_prefix = "${coalesce(var.destination_cidr, data.template_file.azure-terraform-dcos-default-cidr.rendered)}"
        next_hop_type = "VirtualAppliance"
        next_hop_in_ip_address = "${local.private_azure_csr_private_ip}"
    }
}

resource "azurerm_public_ip" "cisco" {
  name                         = "cisco-pip"
  location                     = "${var.azure_region}"
  resource_group_name          = "${data.azurerm_resource_group.rg.name}"
  public_ip_address_allocation = "static"
}

# Agent Security Groups for NICs
resource "azurerm_network_security_group" "cisco_security_group" {
  name                         = "cisco-csr-security-group"
  location = "${var.azure_region}"
  resource_group_name = "${data.azurerm_resource_group.rg.name}"
}

resource "azurerm_network_security_rule" "cisco_sshRule" {
    name                        = "sshRule"
    priority                    = 100
    direction                   = "Inbound"
    access                      = "Allow"
    protocol                    = "Tcp"
    source_port_range           = "*"
    destination_port_range      = "22"
    source_address_prefix       = "*"
    destination_address_prefix  = "*"
    resource_group_name         = "${data.azurerm_resource_group.rg.name}"
    network_security_group_name = "${azurerm_network_security_group.cisco_security_group.name}"
}

resource "azurerm_network_security_rule" "cisco_udp500" {
    name                        = "cisco_udp_500"
    priority                    = 110
    direction                   = "Inbound"
    access                      = "Allow"
    protocol                    = "Udp"
    source_port_range           = "*"
    destination_port_range      = "500"
    source_address_prefix       = "Internet"
    destination_address_prefix  = "*"
    resource_group_name         = "${data.azurerm_resource_group.rg.name}"
    network_security_group_name = "${azurerm_network_security_group.cisco_security_group.name}"
}

resource "azurerm_network_security_rule" "cisco_udp4500" {
    name                        = "cisco_udp_4500"
    priority                    = 120
    direction                   = "Inbound"
    access                      = "Allow"
    protocol                    = "Udp"
    source_port_range           = "*"
    destination_port_range      = "4500"
    source_address_prefix       = "Internet"
    destination_address_prefix  = "*"
    resource_group_name         = "${data.azurerm_resource_group.rg.name}"
    network_security_group_name = "${azurerm_network_security_group.cisco_security_group.name}"
}

resource "azurerm_network_security_rule" "cisco_esp" {
    name                        = "cisco_esp"
    priority                    = 130
    direction                   = "Inbound"
    access                      = "Allow"
    protocol                    = "*"
    source_port_range           = "*"
    destination_port_range      = "*"
    source_address_prefix       = "10.0.0.0/8"
    destination_address_prefix  = "*"
    resource_group_name         = "${data.azurerm_resource_group.rg.name}"
    network_security_group_name = "${azurerm_network_security_group.cisco_security_group.name}"
}

resource "azurerm_network_security_rule" "cisco_everythingElseOutBound" {
    name                        = "allOtherTrafficOutboundRule"
    priority                    = 170
    direction                   = "Outbound"
    access                      = "Allow"
    protocol                    = "Tcp"
    source_port_range           = "*"
    destination_port_range      = "*"
    source_address_prefix       = "*"
    destination_address_prefix  = "*"
    resource_group_name         = "${data.azurerm_resource_group.rg.name}"
    network_security_group_name = "${azurerm_network_security_group.cisco_security_group.name}"
}
# End of Agent NIC Security Group

# Agent NICs with Security Group
resource "azurerm_network_interface" "cisco_nic_0" {
  name                      = "cisco-nic-0"
  location                  = "${var.azure_region}"
  resource_group_name       = "${data.azurerm_resource_group.rg.name}"
  network_security_group_id = "${azurerm_network_security_group.cisco_security_group.id}"
  enable_ip_forwarding      = "true"

  ip_configuration {
   primary                                 = "true"
   name                                    = "cisco_ipConfig-0"
   subnet_id                               = "${azurerm_subnet.public.id}"
   private_ip_address_allocation           = "static"
   private_ip_address                      = "${local.public_azure_csr_private_ip}"
   public_ip_address_id                    = "${azurerm_public_ip.cisco.id}"
  }
}

# Agent NICs with Security Group
resource "azurerm_network_interface" "cisco_nic_1" {
  name                      = "cisco-nic-1"
  location                  = "${var.azure_region}"
  resource_group_name       = "${data.azurerm_resource_group.rg.name}"
  enable_ip_forwarding      = "true"

  ip_configuration {
   name                                    = "cisco_ipConfig-1"
   subnet_id                               = "${azurerm_subnet.private.id}"
   private_ip_address_allocation           = "static"
   private_ip_address                      = "${local.private_azure_csr_private_ip}"
  }
}

# Agent VM Coniguration
resource "azurerm_virtual_machine" "cisco" {
    name                             = "cisco-csr"
    location                         = "${var.azure_region}"
    resource_group_name              = "${data.azurerm_resource_group.rg.name}"
    primary_network_interface_id     = "${azurerm_network_interface.cisco_nic_0.id}"
    network_interface_ids            = ["${azurerm_network_interface.cisco_nic_0.id}", "${azurerm_network_interface.cisco_nic_1.id}"]
    vm_size = "Standard_D2_v2"
    delete_os_disk_on_termination    = true
    delete_data_disks_on_termination = true

    plan {
        name = "16_6"
        product = "cisco-csr-1000v"
        publisher = "cisco"
    }
    vm_size = "Standard_D2_v2"
    storage_image_reference {
        publisher = "cisco"
        offer = "cisco-csr-1000v"
        sku = "16_6"
        version = "latest"
    }


  storage_os_disk {
    name              = "cisco_disk-os"
    caching           = "ReadWrite"
    create_option     = "FromImage"
    managed_disk_type = "Standard_LRS"
  }

    delete_os_disk_on_termination = true
    os_profile {
        computer_name = "${var.remote_hostname}"
        admin_username = "${var.cisco_user}"
        admin_password = "${var.cisco_password}"
        #custom_data = "enable-scp-server true"
    }
    os_profile_linux_config {
        disable_password_authentication = false
    }
}

data "template_file" "azure_ssh_template" {
   template = "${file("${path.module}/ssh-deploy-script.tpl")}"

   vars {
    cisco_commands = "${module.azure_csr_userdata.ssh_emulator}"
    cisco_hostname = "${azurerm_public_ip.cisco.ip_address}"
    cisco_password = "${var.cisco_password}"
    cisco_user    = "${var.cisco_user}"
   }
}

output "cisco" {
  value = ["${azurerm_public_ip.cisco.*.ip_address}"]
}

module "azure_csr_userdata" {
  source = "../cisco-config-generator"
  public_subnet_private_ip_local_site  = "${local.public_azure_csr_private_ip}"
  public_subnet_private_ip_network_mask = "${cidrnetmask(local.public_azure_csr_subnet_cidr_block)}"
  private_subnet_private_ip_local_site  = "${local.private_azure_csr_private_ip}"
  private_subnet_private_ip_network_mask = "${cidrnetmask(local.private_azure_csr_subnet_cidr_block)}"
  public_subnet_private_ip_cidr_remote_site_network_mask = "${cidrnetmask(data.template_file.azure-terraform-dcos-default-cidr.rendered)}"
  public_subnet_private_ip_cidr_remote_site  = "${element(split("/", data.template_file.azure-terraform-dcos-default-cidr.rendered), 0)}"
  public_subnet_public_ip_remote_site  = "${coalesce(var.public_subnet_public_ip_remote_site, aws_eip.csr.public_ip)}"
  tunnel_ip_local_site   = "${var.tunnel_ip_remote_site}"
  tunnel_ip_remote_site  = "${var.tunnel_ip_local_site}"
  local_hostname         = "${var.remote_hostname}"
}

resource "null_resource" "azure_ssh_deploy" {
  triggers {
    cisco_ids = "${azurerm_virtual_machine.cisco.id}"
    instruction = "${data.template_file.azure_ssh_template.rendered}"
  }
  connection {
    host = "${var.azure_docker_utility_node}"
    user = "${var.azure_docker_utility_node_username}"
  }

  provisioner "file" {
    content     = "${data.template_file.azure_ssh_template.rendered}"
    destination = "azure-cisco-config.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "sudo chmod +x azure-cisco-config.sh",
      "sudo ./azure-cisco-config.sh"
    ]
  }
}

output "azure_private_ip_address" {
  value = "${azurerm_network_interface.cisco_nic_0.private_ip_address}"
}

output "azure_ssh_user" {
  value = "${var.cisco_user}"
}

data "template_file" "azure-terraform-dcos-default-cidr" {
  template = "$${cloud == "aws" ? "10.0.0.0/16" : cloud == "gcp" ? "10.64.0.0/16" : "undefined"}"

  vars {
    cloud = "${var.local_terraform_dcos_destination_provider}"
  }
}
