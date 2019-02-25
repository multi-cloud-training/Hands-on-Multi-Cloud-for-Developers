resource "aws_vpc" "aws-vpc" {
  cidr_block = "10.0.0.0/16"
  tags {
    "Name" = "aws-vpc"
  }
}

resource "aws_subnet" "vpc-subnet1" {
  vpc_id            = "${aws_vpc.aws-vpc.id}"
  cidr_block        = "10.0.1.0/24"

  tags {
    Name = "aws-vpn-subnet"
  }
}

resource "aws_internet_gateway" "aws-vpc-igw" {
  vpc_id = "${aws_vpc.aws-vpc.id}"

  tags {
    Name = "aws-vpn-igw"
  }
}

resource "aws_vpn_gateway" "vpn_gateway" {
  vpc_id = "${aws_vpc.aws-vpc.id}"
}

resource "aws_customer_gateway" "aws-customer-gateway" {
  bgp_asn    = 65000
  ip_address = "${google_compute_address.vpn_static_ip.address}"
  type       = "ipsec.1"
  tags {
    "Name" = "aws-customer-gateway"
  }
}

resource "aws_default_route_table" "aws-vpc" {
  default_route_table_id = "${aws_vpc.aws-vpc.default_route_table_id}"
  route {
    cidr_block  = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.aws-vpc-igw.id}"
  }
  propagating_vgws = [
    "${aws_vpn_gateway.vpn_gateway.id}"
  ]
}

resource "aws_vpn_connection" "aws-vpn-connection1" {
  vpn_gateway_id      = "${aws_vpn_gateway.vpn_gateway.id}"
  customer_gateway_id = "${aws_customer_gateway.aws-customer-gateway.id}"
  type                = "ipsec.1"
  static_routes_only  = false
  tags {
    "Name" = "aws-vpn-connection1"
  }
}