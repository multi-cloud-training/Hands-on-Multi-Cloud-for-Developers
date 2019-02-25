# kubectl CLI needs credentials as local files
resource "null_resource" "kubecerts" {
  provisioner "local-exec" {
    command = "echo '${var.cluster_ca_certificate}' > ${var.temp_dir}/.cluster_ca_certificate"
  }
  provisioner "local-exec" {
    command = "echo '${var.client_certificate}' > ${var.temp_dir}/.client_certificate"
  }
  provisioner "local-exec" {
    command = "echo '${var.client_key}' > ${var.temp_dir}/.client_key"
  }
}

# Generate kubectl CLI commands to setup kuebconfig
data "template_file" "setup_kubectl" {
  depends_on = [
    "null_resource.kubecerts"
  ]

  template = <<EOF
set -ex \
&& rm -f $${kubeconfig_file} \
&& kubectl --kubeconfig=$${kubeconfig_file} config set-cluster mycluster --server=$${host} --certificate-authority=$${cluster_ca_certificate} \
&& kubectl --kubeconfig=$${kubeconfig_file} config set-credentials admin --client-certificate=$${client_certificate} --client-key=$${client_key} --username=$${username} --password=$${password}
EOF

  vars {
    kubeconfig_file        = "${var.kubeconfig_file}"
    host                   = "${var.host}"
    username               = "${var.username}"
    password               = "${var.password}"
    cluster_ca_certificate = "${var.temp_dir}/.cluster_ca_certificate"
    client_certificate     = "${var.temp_dir}/.client_certificate"
    client_key             = "${var.temp_dir}/.client_key"
  }
}

resource "null_resource" "exec_setup_kubectl" {
  #  triggers {  sha256_client_key = "${base64sha256(var.client_key)}"  }

  provisioner "local-exec" {
    command = "${data.template_file.setup_kubectl.rendered}"
  }
}
