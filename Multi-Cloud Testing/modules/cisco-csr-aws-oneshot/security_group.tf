resource "aws_security_group" "sg_g1_csr1000v" {
    name = "SG_G1_CSR1000v"
    description = "Allow Traffic into the CSR1000v"

    ingress {
        from_port = 4500
        to_port = 4500
        protocol = "udp"
        cidr_blocks = ["0.0.0.0/0"]
    }
    ingress {
        from_port = 0
        to_port = 0
        protocol = "50"
        cidr_blocks = ["10.0.0.0/8"]
    }
    ingress {
        from_port = 500
        to_port = 500
        protocol = "udp"
        cidr_blocks = ["0.0.0.0/0"]
    }
    ingress {
        from_port = 22
        to_port = 22
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }
    ingress {
        from_port = 0
        to_port = 0
        protocol = "-1"
        cidr_blocks = ["10.0.0.0/8"]
    }
    ingress {
        from_port = -1
        to_port = -1
        protocol = "icmp"
        cidr_blocks = ["0.0.0.0/0"]
    }
	egress {
		from_port = 0
		to_port = 0
		protocol = "-1"
		cidr_blocks = ["0.0.0.0/0"]
    }

    vpc_id = "${data.aws_vpc.current.id}"
}
