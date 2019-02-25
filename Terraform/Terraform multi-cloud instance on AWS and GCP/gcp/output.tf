output "alb_dns" {
  value = "${google_compute_global_address.external-address.address}"
}

output "cidr" {
  value = "${data.google_compute_subnetwork.default.ip_cidr_range}"
}
