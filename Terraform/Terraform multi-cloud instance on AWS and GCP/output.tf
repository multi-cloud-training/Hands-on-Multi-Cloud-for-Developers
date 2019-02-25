output "alb_dns" {
  value = "${module.aws.alb_dns}"
}

output "alb_arn" {
  value = "${module.aws.alb_arn}"
}

output "alb_intenal_dns" {
  value = "${module.aws.alb_internal_dns}"
}

output "ssh_host" {
  value = "${module.aws.ssh_host}"
}

output "gcp_alb_dns" {
  value = "${module.gcp.alb_dns}"
}
