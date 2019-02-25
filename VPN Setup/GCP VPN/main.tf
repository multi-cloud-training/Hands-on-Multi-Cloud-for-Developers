variable "aws_region" {
  default = "us-east-2"
}

variable "gcp_region" {
  default = "us-central1"
}

provider "aws" {
  region = "${var.aws_region}"
}

provider "google" {
  region = "${var.gcp_region}"
}

output "ec2_instance_public_ip" {
  value = "${aws_instance.iperf.public_ip}"
}

output "gcp_instance_ip" {
  value = "${google_compute_instance.iperf.network_interface.0.address}"
}

output "ec2_instance_ip" {
  value = "${aws_instance.iperf.private_ip}"
}
