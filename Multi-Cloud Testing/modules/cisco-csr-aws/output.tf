output "cisco_csr_ami" {
  value = "${data.aws_ami_ids.cisco_csr.ids}"
}

output "destination_cidr" {
  value = "${data.template_file.terraform-dcos-default-cidr.rendered}"
}

output "public_ip_address" {
  value = "${aws_eip.csr_public_ip.public_ip}"
}

output "private_ip_address" {
  value = "${aws_instance.cisco.private_ip}"
}

output "ssh_user" {
  value = "ec2-user"
}
