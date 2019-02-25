resource "aws_key_pair" "iperf" {
  key_name   = "iperf-key"
  public_key = "${var.EC2_SSH_PUB_KEY}"
}

resource "aws_security_group" "allow_vpn" {
  name        = "allow_vpn"
  description = "Allow all traffic from vpn resources"
  vpc_id      = "${aws_vpc.aws-vpc.id}"

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["${var.gcp_subnet1_cidr}"]
  }
}

resource "aws_security_group" "allow_ssh" {
  name        = "allow_ssh"
  description = "Allow ssh access from anywhere"
  vpc_id      = "${aws_vpc.aws-vpc.id}"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

data "aws_ami" "ubuntu" {
  most_recent = true

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-xenial-16.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  owners = ["099720109477"] # Canonical
}

resource "aws_instance" "iperf" {
  ami           = "${data.aws_ami.ubuntu.id}"
  instance_type = "${var.aws_instance_type}"
  subnet_id     = "${aws_subnet.vpc-subnet1.id}"
  key_name      = "${aws_key_pair.iperf.key_name}"
  
  associate_public_ip_address = true

  vpc_security_group_ids = [
    "${aws_security_group.allow_vpn.id}",
    "${aws_security_group.allow_ssh.id}",
  ]

  user_data = "${file("aws_userdata.sh")}"

  tags {
    Name = "${var.aws_region}-vpn-iperf"
  }
}