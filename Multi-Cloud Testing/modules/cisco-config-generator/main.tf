data "template_file" "ssh_emulator" {
  template = "${file("${path.module}/config/ssh_emulator.sh")}"

  vars {
    public_subnet_private_ip_local_site        = "${var.public_subnet_private_ip_local_site}"
    public_subnet_public_ip_remote_site        = "${var.public_subnet_public_ip_remote_site}"
    public_subnet_private_ip_cidr_remote_site  = "${var.public_subnet_private_ip_cidr_remote_site}"
    public_subnet_private_ip_network_mask      = "${var.public_subnet_private_ip_network_mask}"
    private_subnet_private_ip_local_site       = "${var.private_subnet_private_ip_local_site}"
    private_subnet_private_ip_network_mask     = "${var.private_subnet_private_ip_network_mask}"
    public_subnet_private_ip_cidr_remote_site_network_mask = "${var.public_subnet_private_ip_cidr_remote_site_network_mask}"
    public_gateway_ip            = "${var.public_gateway_ip}"
    remote_pre_share_key         = "${var.remote_pre_share_key}"
    local_pre_share_key          = "${var.local_pre_share_key}"
    tunnel_ip_local_site         = "${var.tunnel_ip_local_site}"
    tunnel_ip_remote_site        = "${var.tunnel_ip_remote_site}"
    local_hostname               = "${var.local_hostname}"
  }
}

data "template_file" "userdata" {
  template = "${file("${path.module}/config/userdata.sh")}"

  vars {
    public_subnet_private_ip_local_site        = "${var.public_subnet_private_ip_local_site}"
    public_subnet_public_ip_remote_site        = "${var.public_subnet_public_ip_remote_site}"
    public_subnet_private_ip_cidr_remote_site  = "${var.public_subnet_private_ip_cidr_remote_site}"
    public_subnet_private_ip_network_mask      = "${var.public_subnet_private_ip_network_mask}"
    private_subnet_private_ip_local_site       = "${var.private_subnet_private_ip_local_site}"
    private_subnet_private_ip_network_mask     = "${var.private_subnet_private_ip_network_mask}"
    public_subnet_private_ip_cidr_remote_site_network_mask = "${var.public_subnet_private_ip_cidr_remote_site_network_mask}"
    public_gateway_ip            = "${var.public_gateway_ip}"
    remote_pre_share_key         = "${var.remote_pre_share_key}"
    local_pre_share_key          = "${var.local_pre_share_key}"
    tunnel_ip_local_site         = "${var.tunnel_ip_local_site}"
    tunnel_ip_remote_site        = "${var.tunnel_ip_remote_site}"
    local_hostname               = "${var.local_hostname}"
  }
}

data "template_file" "userdata_ssh" {
  template = "${file("${path.module}/config/userdata_ssh_emulator.sh")}"

  vars {
    public_subnet_private_ip_local_site        = "${var.public_subnet_private_ip_local_site}"
    public_subnet_public_ip_remote_site        = "${var.public_subnet_public_ip_remote_site}"
    public_subnet_private_ip_cidr_remote_site  = "${var.public_subnet_private_ip_cidr_remote_site}"
    public_subnet_private_ip_network_mask      = "${var.public_subnet_private_ip_network_mask}"
    private_subnet_private_ip_local_site       = "${var.private_subnet_private_ip_local_site}"
    private_subnet_private_ip_network_mask     = "${var.private_subnet_private_ip_network_mask}"
    public_subnet_private_ip_cidr_remote_site_network_mask = "${var.public_subnet_private_ip_cidr_remote_site_network_mask}"
    public_gateway_ip            = "${var.public_gateway_ip}"
    remote_pre_share_key         = "${var.remote_pre_share_key}"
    local_pre_share_key          = "${var.local_pre_share_key}"
    tunnel_ip_local_site         = "${var.tunnel_ip_local_site}"
    tunnel_ip_remote_site        = "${var.tunnel_ip_remote_site}"
    local_hostname               = "${var.local_hostname}"
  }
}
