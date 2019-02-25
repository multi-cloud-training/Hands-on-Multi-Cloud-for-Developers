
variable gcp_instance_type {
  default = "n1-highmem-8"
}

variable gcp_network_cidr {
  default = "10.128.0.0/9"
}

variable gcp_subnet1_cidr {
  default = "10.128.0.0/20"
}

variable TUN1_VPN_GW_ASN {
  description = "Tunnel 1 - Virtual Private Gateway ASN, from the AWS VPN Customer Gateway Configuration"
  default = "7224"
}

variable TUN1_VPN_GW_INSIDE_IP {
  description = "Tunnel 1 - Virtual Private Gateway from Inside IP Address, from AWS VPN Customer Gateway Configuration"
  default = "169.254.0.1"
}

variable TUN1_CUSTOMER_GW_INSIDE_IP {
  description = "Tunnel 1 - Customer Gateway from Inside IP Address, from AWS VPN Customer Gateway Configuration"
  default = "169.254.0.2"
}

variable TUN1_CUSTOMER_GW_INSIDE_NETWORK_CIDR {
  description = "Tunnel 1 - Customer Gateway from Inside IP Address CIDR block, from AWS VPN Customer Gateway Configuration"
  default = "30"
}

variable TUN2_VPN_GW_ASN {
  description = "Tunnel 2 - Virtual Private Gateway ASN, from the AWS VPN Customer Gateway Configuration"
  default = "7224"
}

variable TUN2_VPN_GW_INSIDE_IP {
  description = "Tunnel 2 - Virtual Private Gateway from Inside IP Address, from AWS VPN Customer Gateway Configuration"
  default = "169.254.0.1"
}

variable TUN2_CUSTOMER_GW_INSIDE_IP {
  description = "Tunnel 2 - Customer Gateway from Inside IP Address, from AWS VPN Customer Gateway Configuration"
  default = "169.254.0.2"
}

variable TUN2_CUSTOMER_GW_INSIDE_NETWORK_CIDR {
  description = "Tunnel 2 - Customer Gateway from Inside IP Address CIDR block, from AWS VPN Customer Gateway Configuration"
  default = "30"
}