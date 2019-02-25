output "kubeconfig_file" {
  value = "${var.kubeconfig_file}"
}

output "kubeconfig_setup_id" {
  value = "${null_resource.exec_setup_kubectl.id}"
}
