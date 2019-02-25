resource "aws_customer_gateway" "customer_gateway" {
  bgp_asn    = 60000
  ip_address = "${google_compute_address.vpn_static_ip.address}"
  type       = "ipsec.1"
}

resource "aws_vpn_gateway" "default" {
  vpc_id = "${var.aws_vpc}"
}

resource "aws_vpn_connection" "main" {
  vpn_gateway_id      = "${aws_vpn_gateway.default.id}"
  customer_gateway_id = "${aws_customer_gateway.customer_gateway.id}"
  type                = "ipsec.1"
  static_routes_only  = true
}

resource "aws_vpn_connection_route" "gcp" {
  destination_cidr_block = "${var.gcp_cidr}"
  vpn_connection_id      = "${aws_vpn_connection.main.id}"
}

resource "aws_route" "gcp" {
  route_table_id         = "${var.aws_route_table_id}"
  gateway_id             = "${aws_vpn_gateway.default.id}"
  destination_cidr_block = "${var.gcp_cidr}"
}

# Allow inbound access to VPC resources from GCP CIDR
resource "aws_security_group_rule" "google_ingress_vpn" {
  type        = "ingress"
  from_port   = 0
  to_port     = 0
  protocol    = "-1"
  cidr_blocks = ["${var.gcp_cidr}"]

  security_group_id = "${var.aws_sg}"
}

# Allow outbound access from VPC resources to GCP CIDR
resource "aws_security_group_rule" "google_egress_vpn" {
  type        = "egress"
  from_port   = 0
  to_port     = 0
  protocol    = "-1"
  cidr_blocks = ["${var.gcp_cidr}"]

  security_group_id = "${var.aws_sg}"
}
