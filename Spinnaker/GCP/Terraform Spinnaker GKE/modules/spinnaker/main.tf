# Create GCS bucket
resource "google_storage_bucket" "spinnaker_config" {
  name          = "${var.project}-spinnaker-config"
  location      = "${var.gcs_location}"
  storage_class = "NEARLINE"
  force_destroy = "true"
}

# Create service account for spinner storage
resource "google_service_account" "spinnaker_gcs" {
  depends_on = [
    "google_storage_bucket.spinnaker_config",
  ]

  account_id   = "${var.spinnaker_gcs_sa}"
  display_name = "${var.spinnaker_gcs_sa}"
}

# Bind the storage.admin role to your service account:
resource "google_project_iam_binding" "spinnaker_gcs" {
  role = "roles/storage.admin"

  members = [
    "serviceAccount:${google_service_account.spinnaker_gcs.email}",
  ]
}

resource "google_service_account_key" "spinnaker_gcs" {
  service_account_id = "${google_service_account.spinnaker_gcs.name}"
}

data "template_file" "spinnaker_config" {
  template = <<EOF
cat <<EOI > ${var.temp_dir}/.spinnaker-values.yaml
storageBucket: $${bucket}
gcs:
  enabled: true
  project: $${project}
  jsonKey: '$${sa_json}'

# Disable minio as the default
minio:
  enabled: false

# Configure your Docker registries here
accounts:
- name: gcr
  address: https://gcr.io
  username: _json_key
  email: $${sa_email}
  password: '$${sa_json}'
EOI
EOF

  vars {
    bucket     = "${google_storage_bucket.spinnaker_config.name}"
    project    = "${var.project}"
    sa_json    = "${base64decode(google_service_account_key.spinnaker_gcs.private_key)}"
    sa_email   = "${google_service_account.spinnaker_gcs.email}"
    depends_on = "${join(",", var.depends_on)}"
  }
}

resource "null_resource" "spinnaker_config" {
  provisioner "local-exec" {
    command = "${data.template_file.spinnaker_config.rendered}"
  }
}

data "template_file" "install_spinnaker" {
  template = <<EOF
set -ex \
&& kubectl --kubeconfig=$${kubeconfig_file} config set-context default --cluster=mycluster --namespace=$${namespace} --user=admin \
&& kubectl --kubeconfig=$${kubeconfig_file} config use-context default \
&& kubectl --kubeconfig=$${kubeconfig_file} delete --ignore-not-found=true clusterrolebinding $${crb_name} \
&& kubectl --kubeconfig=$${kubeconfig_file} create clusterrolebinding $${crb_name} --clusterrole=cluster-admin --serviceaccount=$${namespace}:$${service_account} \
&& export KUBECONFIG=$${kubeconfig_file} \
&& helm --debug install --kube-context default --namespace $${namespace} --wait --timeout 600 --version 0.5.0 -f $${values_yaml} --name spinnaker stable/spinnaker
EOF

  vars {
    kubeconfig_file = "${var.kubeconfig_file}"
    namespace       = "${var.namespace}"
    service_account = "${var.service_account}"
    crb_name        = "${var.service_account}-admin-binding"
    values_yaml     = "${var.temp_dir}/.spinnaker-values.yaml"
    host            = "${var.host}"

    # Wait for values.yaml to be available
    depends_on = "${null_resource.spinnaker_config.id}"
  }

  depends_on = [
    "null_resource.spinnaker_config",
  ]
}

resource "null_resource" "install_spinnaker" {
  #  triggers { sha256 = "${base64sha256(data.template_file.crb_commands.rendered)}" }

  provisioner "local-exec" {
    command = "${data.template_file.install_spinnaker.rendered}"
  }
}
