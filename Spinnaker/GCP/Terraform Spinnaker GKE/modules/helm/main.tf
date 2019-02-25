# Create service account for tiller
resource "kubernetes_service_account" "tiller" {
  "metadata" {
    name      = "${var.service_account}"
    namespace = "${var.namespace}"
  }
}

# Grant cluster-admin CRB to tiller
data "template_file" "helm_init_commands" {
  depends_on = [
    "kubernetes_service_account.tiller",
  ]

  # kubectl patch (3rd command below) is workaround to get helm working with RBAC in k8s. For more info refer https://github.com/helm/helm/issues/4020
  template = <<EOF
set -ex \
&& kubectl --kubeconfig=$${kubeconfig_file} config set-context default --cluster=mycluster --namespace=$${namespace} --user=admin \
&& kubectl --kubeconfig=$${kubeconfig_file} config use-context default \
&& kubectl --kubeconfig=$${kubeconfig_file} patch --namespace=$${namespace} serviceaccount $${service_account}  -p $'automountServiceAccountToken: true' \
&& kubectl --kubeconfig=$${kubeconfig_file} delete --ignore-not-found=true clusterrolebinding $${crb_name} \
&& kubectl --kubeconfig=$${kubeconfig_file} create clusterrolebinding $${crb_name} --clusterrole=cluster-admin --serviceaccount=$${namespace}:$${service_account} \
&& export KUBECONFIG=$${kubeconfig_file} \
&& helm --debug init --tiller-namespace=$${namespace} --service-account=$${service_account} --wait \
&& helm --debug repo update --tiller-namespace=$${namespace} --kube-context default
EOF

  vars {
    kubeconfig_file = "${var.kubeconfig_file}"
    namespace       = "${kubernetes_service_account.tiller.metadata.0.namespace}"
    service_account = "${var.service_account}"
    crb_name        = "${var.service_account}-admin-binding"
    depends_on      = "${join(",", var.depends_on)}"
  }
}

resource "null_resource" "helm_init" {
  #  triggers {  sha256 = "${base64sha256(data.template_file.crb_commands.rendered)}"  }

  provisioner "local-exec" {
    command = "${data.template_file.helm_init_commands.rendered}"
  }
}
