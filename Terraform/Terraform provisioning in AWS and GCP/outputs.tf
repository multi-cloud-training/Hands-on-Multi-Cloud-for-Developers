output "0-dns" {
  value = "${aws_route53_record.mc_r53_aws.fqdn}"
}

output "aws-dns" {
  value = "${aws_route53_record.r53_aws.fqdn}"
}

output "aws-lb" {
  value = "${module.aws_elb.this_elb_dns_name}"
}

output "gcp-dns" {
  value = "${aws_route53_record.r53_gcp.fqdn}"
}

output "gcp-lb" {
  value = "${google_compute_forwarding_rule.http.ip_address}"
}
