output "target_group" {
  value = "${google_compute_instance_group_manager.nomad.instance_group}"
}
