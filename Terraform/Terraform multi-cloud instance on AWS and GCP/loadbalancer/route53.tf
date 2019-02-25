data "aws_route53_zone" "default" {
  name         = "${var.route53_zone}."
  private_zone = false
}

resource "aws_route53_record" "nomad" {
  zone_id = "${data.aws_route53_zone.default.zone_id}"
  name    = "nomad.${data.aws_route53_zone.default.name}"
  type    = "CNAME"
  ttl     = "300"
  records = ["nonssl.global.fastly.net."]
}

resource "aws_route53_record" "consul" {
  zone_id = "${data.aws_route53_zone.default.zone_id}"
  name    = "consul.${data.aws_route53_zone.default.name}"
  type    = "CNAME"
  ttl     = "300"
  records = ["${var.alb_internal_dns_name}."]
}
