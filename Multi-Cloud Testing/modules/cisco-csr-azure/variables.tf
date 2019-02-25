variable "rg_name" {
  description = "Name of the resource group this VPN router will be launched into."
  default     = ""
}

variable "cisco_user" {
  description = "Default Linux User for login"
  default     = ""
}

variable "cisco_password" {
  description = "Default linux Password for login"
  default     = ""
}

variable "vnet_name" {
  description = "Existing VNet Name to install Cisco CSR on"
  default     = ""
}

variable "subnet_name" {
  description = "selected subnet chosen for Cisco CSR on an existing subnet"
  default     = ""
}

variable "instance_disk_size" {
 description = "Default size of the root disk (GB)"
 default = "32"
}

variable "instance_type" {
  description = "Cisco CSR Instance type. Accepts c4.<types> only"
  default = "Standard_D2_v2"
}

variable "azure_region" {
  description = "Azure region to launch servers."
  default     = "West US"
}

variable "destination_cidr" {
  description = "The CIDR block to route traffic too for the other Cisco CSR Router"
  default = ""
}

variable "destination_csr_public_ip" {
  description = "Public IP Address for destination Cisco CSR"
  default     = ""
}

variable "local_csr_public_ip" {
  description = "Public IP address of the current Cisco CSR. If none is provided, an EIP will be created and used."
  default     = ""
}

variable "terraform_dcos_destination_provider" {
  description = "The CIDR block to route traffic too for the other Cisco CSR Router"
  default = "aws"
}

variable "ssh_pub_key" {
  description = "The Public SSH Key associated with your instances for login. Copy your own key from your machine when deploying to log into your instance."
  default = ""
}
