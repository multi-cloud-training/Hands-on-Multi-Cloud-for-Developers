data "azurerm_resource_group" "rg" {
  name = "${var.rg_name}"
}

data "azurerm_virtual_network" "current" {
  name                = "${var.vnet_name}"
  resource_group_name = "${data.azurerm_resource_group.rg.name}"
}

data "azurerm_subnet" "public" {
  name                 = "${data.azurerm_virtual_network.current.subnets[0]}"
  virtual_network_name = "${data.azurerm_virtual_network.current.name}"
  resource_group_name  = "${data.azurerm_resource_group.rg.name}"
}

resource "azurerm_public_ip" "cisco" {
  name                         = "cisco-pip"
  location                     = "${data.azurerm_resource_group.rg.location}"
  resource_group_name          = "${data.azurerm_resource_group.rg.name}"
  public_ip_address_allocation = "Dynamic"
  idle_timeout_in_minutes      = 30
}

resource "azurerm_route_table" "RTPrivate" {
    name = "cisco_vpn_route_table"
    location = "${var.azure_region}"
    resource_group_name = "${data.azurerm_resource_group.rg.name}"

    route {
        name = "CiscoRouter"
        address_prefix = "${coalesce(var.destination_cidr, data.template_file.terraform-dcos-default-cidr.rendered)}"
        next_hop_type = "VirtualAppliance"
        next_hop_in_ip_address = "${azurerm_network_interface.cisco_nic.private_ip_address}"
    }
}

resource "azurerm_network_interface" "cisco_nic" {
    name = "cisco_nic"
    location = "${var.azure_region}"
    resource_group_name = "${data.azurerm_resource_group.rg.name}"
    enable_ip_forwarding = true

    ip_configuration {
        name = "cisco_nic"
        subnet_id = "${data.azurerm_subnet.public.id}"
        private_ip_address_allocation = "dynamic"
        public_ip_address_id          = "${azurerm_public_ip.cisco.id}"
    }
}

data "template_file" "terraform-dcos-default-cidr" {
  template = "$${cloud == "aws" ? "10.0.0.0/16" : cloud == "gcp" ? "10.64.0.0/16" : "undefined"}"

  vars {
    cloud = "${var.terraform_dcos_destination_provider}"
  }
}

data "azurerm_platform_image" "cisco_csr_image" {
  location  = "${var.azure_region}"
  publisher = "cisco"
  offer     = "cisco-csr-1000v"
  sku       = "16_6"
}

#resource "azurerm_availability_set" "cisco_aset" {
#  TODO (mbernadin): Optional HA
#  name                = "cisco_availibility_set"
#  location            = "${data.azurerm_resource_group.rg.location}"
#  resource_group_name = "${data.azurerm_resource_group.rg.name}"
#}

resource "azurerm_virtual_machine" "cisco" {
    name = "csr1000v"
    location = "${var.azure_region}"
    resource_group_name = "${data.azurerm_resource_group.rg.name}"
    plan {
        name = "csr-azure-byol"
        product = "cisco-csr-1000v"
        publisher = "cisco"
    }
    vm_size = "Standard_D2_v2"
    storage_image_reference {
        publisher = "cisco"
        offer = "cisco-csr-1000v"
        sku = "csr-azure-byol"
        version = "latest"
    }

  storage_os_disk {
    name              = "cisco-os-disk"
    caching           = "ReadWrite"
    create_option     = "FromImage"
    managed_disk_type = "Standard_LRS"
  }

    delete_os_disk_on_termination = true
    os_profile {
        computer_name = "csr1000v"
        admin_username = "${var.cisco_user}"
        admin_password = "${var.cisco_password}"
    }
    os_profile_linux_config {
        disable_password_authentication = false
    }

#  os_profile_linux_config {
#    disable_password_authentication = true
#    ssh_keys {
#        path     = "/home/${var.cisco_user}/.ssh/authorized_keys"
#        key_data = "${var.ssh_pub_key}"
#    }
#  }
    network_interface_ids = ["${azurerm_network_interface.cisco_nic.id}"]
    primary_network_interface_id = "${azurerm_network_interface.cisco_nic.id}"
}

data "azurerm_public_ip" "cisco" {
  name                = "${azurerm_public_ip.cisco.name}"
  resource_group_name = "${data.azurerm_resource_group.rg.name}"
  depends_on          = ["azurerm_virtual_machine.cisco"]
}
