data "aws_route53_zone" "mc" {
  name = "${var.aws_hosted_zone}"
}

# Local resources
resource "aws_route53_record" "r53_gcp" {
  zone_id = "${data.aws_route53_zone.mc.zone_id}"
  name    = "mc-gcp.${var.aws_hosted_zone}"
  type    = "A"
  ttl     = "300"
  records = ["${google_compute_forwarding_rule.http.ip_address}"]
}

resource "aws_route53_record" "r53_aws" {
  zone_id = "${data.aws_route53_zone.mc.zone_id}"
  name    = "mc-aws.${var.aws_hosted_zone}"
  type    = "CNAME"
  ttl     = "300"
  records = ["${module.aws_elb.this_elb_dns_name}"]
}

resource "aws_route53_record" "mc_r53_aws" {
  zone_id = "${data.aws_route53_zone.mc.zone_id}"
  name    = "mc.${var.aws_hosted_zone}"
  type    = "CNAME"
  ttl     = "5"

  latency_routing_policy {
    region = "eu-west-1"
  }

  set_identifier = "aws"
  records        = ["mc-aws.${var.aws_hosted_zone}"]
}

resource "aws_route53_record" "mc_r53_gcp" {
  zone_id = "${data.aws_route53_zone.mc.zone_id}"
  name    = "mc.${var.aws_hosted_zone}"
  type    = "CNAME"
  ttl     = "5"

  latency_routing_policy {
    region = "us-west-1"
  }

  set_identifier = "gcp"
  records        = ["mc-gcp.${var.aws_hosted_zone}"]
}
