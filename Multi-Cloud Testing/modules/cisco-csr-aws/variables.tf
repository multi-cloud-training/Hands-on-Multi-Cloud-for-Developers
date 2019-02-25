variable "vpc_id" {
  description = "Existing VPC to install Cisco CSR on"
  default     = ""
}

variable "instance_type" {
  description = "Cisco CSR Instance type. Accepts c4.<types> only"
  default = "c4.large"
}

variable "aws_region" {
  default = ""
}

variable "aws_profile" {
  default = ""
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
  default = "azure"
}

variable "subnet_id" {
  description = "selected subnet chosen for Cisco CSR on an existing subnet"
  default     = ""
}

variable "ssh_key_name" {
  description = "AWS Key Pair name for ssh"
  default     = ""
}
