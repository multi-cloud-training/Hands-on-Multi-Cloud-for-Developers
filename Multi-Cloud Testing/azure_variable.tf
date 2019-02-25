variable "num_of_azure_public_agents" {
  default = "3"
}

variable "azure_public_agent_instance_type" {
  description = "Azure DC/OS Private Agent instance type"
  default = "Standard_D3_v2"
}

variable "num_of_azure_private_agents" {
  default = "3"
}

variable "azure_agent_instance_type" {
  description = "Azure DC/OS Private Agent instance type"
  default = "Standard_D3_v2"
}

variable "azure_bootstrap_instance_type" {
  description = "Azure DC/OS Bootstrap instance type"
  default = "Standard_D3_v2"
}

variable "azure_region" {
  description = "Azure region to launch servers."
  default     = "UK West"
}

variable "azure_admin_username" {
  description = "Username of the OS. (Defaults can be found here modules/dcos-tested-azure-oses/azure_template_file.tf)"
  default = ""
}

variable "ssh_pub_key" {
  description = "The Public SSH Key associated with your instances for login. Copy your own key from your machine when deploying to log into your instance."
  default = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDCJtEX2fuZ4EWXCL3M37Qbr0mj3saEdhOwnLGJk8hr5xFOa8DoTs5IofaHfeRoiOKwfg44PW4fpDIz/e7X/9tmKTuwOszuAE9QTWQijZesCanLSf5nwYCTMsNGlUfxhjpJhcgQIcZ6vcDbNeGIQTElgsBKXoIXDosP3qjdWuwEEIfaQJDo4Mv16P+SqzPJ1KIV16lfw2NW71y7JzNApPRWxlxkoTiydv1hs6Ye6b6MTLLeDIsyzPqNro5/LpQkT7hr37pG88xC22Cn2lA18hhusP0wP+6pZbnbveKLVFkSdVlZAKgsEZ0UyAXsKElWtTHN+SXuqXmldg8h7n6GF1/tmEz7n/2+SBH+nNBlQPM/VOxW7yDwCKWr87mFI009a6ge66U4q+lqrfKzNSIsoamuICYg8GtAGK3yuPQq+pwFluJRUEihZQDlJ7IvezAKThglyDgV31D9frCqJ4gMTfzSnZ2PW54vJjNyAHZQoCqp/Y0aIdjwpnHw6F+blPmgXzzsheMahME7iCMQP1F/ckgXfq1rtI0mT1QNZhUtfFf1qYguNT0EdCGy3G3oWnHiIqjcq/wfhCTpf22ph7h1Q+b1ygXXIGnQWfyY/vZTDdW2lbrX36X/fZA3M74SBmQFEMWrul4tX//YwGtpHSyN380fdRHyCPPo6+BSB7KHVwDevw== default@mesosphere.com"
}
