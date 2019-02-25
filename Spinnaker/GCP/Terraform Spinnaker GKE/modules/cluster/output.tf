output "cluster_ca_certificate" {
  value = "${base64decode(google_container_cluster.primary.master_auth.0.cluster_ca_certificate)}"
}

output "client_key" {
  value = "${base64decode(google_container_cluster.primary.master_auth.0.client_key)}"
}

output "client_certificate" {
  value = "${base64decode(google_container_cluster.primary.master_auth.0.client_certificate)}"
}

output "id" {
  value = "${google_container_cluster.primary.id}"
}

output "endpoint" {
  value = "${google_container_cluster.primary.endpoint}"
}

output "host" {
  value = "https://${google_container_cluster.primary.endpoint}"
}

output "username" {
  value = "${google_container_cluster.primary.master_auth.0.username}"
}

output "password" {
  value = "${google_container_cluster.primary.master_auth.0.password}"
}
