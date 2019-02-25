/* ########## Network Setup ############## */

resource "google_compute_network" "gcp-network" {
  name = "gcp-network"
  auto_create_subnetworks = "true"
}

resource "google_compute_firewall" "allow-icmp" {
  name    = "${google_compute_network.gcp-network.name}-allow-icmp"
  network = "${google_compute_network.gcp-network.name}"

  allow {
    protocol = "icmp"
  }

  source_ranges = [
    "0.0.0.0/0"
  ]
}

resource "google_compute_firewall" "allow-internal" {
  name    = "${google_compute_network.gcp-network.name}-allow-internal"
  network = "${google_compute_network.gcp-network.name}"

  allow {
    protocol = "icmp"
  }

  allow {
    protocol = "tcp"
    ports = ["0-65535"]
  }

  allow {
    protocol = "udp"
    ports = ["0-65535"]
  }

  source_ranges = [
    "${var.gcp_network_cidr}"
  ]
}

resource "google_compute_firewall" "allow-ssh" {
  name    = "${google_compute_network.gcp-network.name}-allow-ssh"
  network = "${google_compute_network.gcp-network.name}"

  allow {
    protocol = "tcp"
    ports = ["22"]
  }

  source_ranges = [
    "0.0.0.0/0"
  ]
}


/* ########## VPN Connection ############## */

resource "google_compute_vpn_gateway" "target_gateway" {
  name    = "gcp-vpn-${var.gcp_region}"
  network = "${google_compute_network.gcp-network.self_link}"
  region  = "${var.gcp_region}"
}

resource "google_compute_address" "vpn_static_ip" {
  name   = "vpn-static-ip"
  region = "${var.gcp_region}"
}

resource "google_compute_forwarding_rule" "fr_esp" {
  name        = "fr-esp"
  ip_protocol = "ESP"
  ip_address  = "${google_compute_address.vpn_static_ip.address}"
  target      = "${google_compute_vpn_gateway.target_gateway.self_link}"
}

resource "google_compute_forwarding_rule" "fr_udp500" {
  name        = "fr-udp500"
  ip_protocol = "UDP"
  port_range  = "500-500"
  ip_address  = "${google_compute_address.vpn_static_ip.address}"
  target      = "${google_compute_vpn_gateway.target_gateway.self_link}"
}

resource "google_compute_forwarding_rule" "fr_udp4500" {
  name        = "fr-udp4500"
  ip_protocol = "UDP"
  port_range  = "4500-4500"
  ip_address  = "${google_compute_address.vpn_static_ip.address}"
  target      = "${google_compute_vpn_gateway.target_gateway.self_link}"
}


/* ########## VPN Tunnel 1 ############## */

resource "google_compute_vpn_tunnel" "tunnel1" {
  name          = "tunnel1"
  peer_ip       = "${aws_vpn_connection.aws-vpn-connection1.tunnel1_address}"
  shared_secret = "${aws_vpn_connection.aws-vpn-connection1.tunnel1_preshared_key}"
  ike_version   = 1

  target_vpn_gateway = "${google_compute_vpn_gateway.target_gateway.self_link}"

  router = "${google_compute_router.gcp-router1.name}"

  depends_on = [
    "google_compute_forwarding_rule.fr_esp",
    "google_compute_forwarding_rule.fr_udp500",
    "google_compute_forwarding_rule.fr_udp4500",
  ]
}

resource "google_compute_router" "gcp-router1" {
  name = "gcp-router1"
  region = "${var.gcp_region}"
  network = "${google_compute_network.gcp-network.self_link}"
  bgp {
    asn = "${aws_customer_gateway.aws-customer-gateway.bgp_asn}"
  }
}

resource "google_compute_router_peer" "router1_peer" {
  name = "gcp-to-aws-bgp1"
  router  = "${google_compute_router.gcp-router1.name}"
  region  = "${google_compute_router.gcp-router1.region}"
  ip_address = "${var.TUN1_VPN_GW_INSIDE_IP}"
  asn = "${var.TUN1_VPN_GW_ASN}"
  interface = "${google_compute_router_interface.router_interface1.name}"
}

resource "google_compute_router_interface" "router_interface1" {
  name    = "gcp-to-aws-interface1"
  router  = "${google_compute_router.gcp-router1.name}"
  region  = "${google_compute_router.gcp-router1.region}"
  ip_range = "${var.TUN1_CUSTOMER_GW_INSIDE_IP}/${var.TUN1_CUSTOMER_GW_INSIDE_NETWORK_CIDR}"
  vpn_tunnel = "${google_compute_vpn_tunnel.tunnel1.name}"
}


/* ########## VPN Tunnel 2 ############## */

resource "google_compute_vpn_tunnel" "tunnel2" {
  name          = "tunnel2"
  peer_ip       = "${aws_vpn_connection.aws-vpn-connection1.tunnel2_address}"
  shared_secret = "${aws_vpn_connection.aws-vpn-connection1.tunnel2_preshared_key}"
  ike_version   = 1

  target_vpn_gateway = "${google_compute_vpn_gateway.target_gateway.self_link}"

  router = "${google_compute_router.gcp-router2.name}"

  depends_on = [
    "google_compute_forwarding_rule.fr_esp",
    "google_compute_forwarding_rule.fr_udp500",
    "google_compute_forwarding_rule.fr_udp4500",
  ]
}

resource "google_compute_router" "gcp-router2" {
  name = "gcp-router2"
  region = "${var.gcp_region}"
  network = "${google_compute_network.gcp-network.self_link}"
  bgp {
    asn = "${aws_customer_gateway.aws-customer-gateway.bgp_asn}"
  }
}

resource "google_compute_router_peer" "router2_peer" {
  name = "gcp-to-aws-bgp2"
  router  = "${google_compute_router.gcp-router2.name}"
  region  = "${google_compute_router.gcp-router2.region}"
  ip_address = "${var.TUN2_VPN_GW_INSIDE_IP}"
  asn = "${var.TUN2_VPN_GW_ASN}"
  interface = "${google_compute_router_interface.router_interface2.name}"
}

resource "google_compute_router_interface" "router_interface2" {
  name    = "gcp-to-aws-interface2"
  router  = "${google_compute_router.gcp-router2.name}"
  region  = "${google_compute_router.gcp-router2.region}"
  ip_range = "${var.TUN2_CUSTOMER_GW_INSIDE_IP}/${var.TUN2_CUSTOMER_GW_INSIDE_NETWORK_CIDR}"
  vpn_tunnel = "${google_compute_vpn_tunnel.tunnel2.name}"
}