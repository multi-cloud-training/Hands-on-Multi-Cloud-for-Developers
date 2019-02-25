# Terraforming steps from https://cloud.google.com/solutions/continuous-delivery-spinnaker-kubernetes-engine

provider "google" {
  version     = "~> 1.16"
  credentials = "${file("${var.credential}")}"
  project     = "${var.project}"
  zone        = "${var.zone}"
}

# Need to enable serviceusage API manully on the console first
# https://console.developers.google.com/apis/library/serviceusage.googleapis.com
resource "google_project_services" "myproject" {
  disable_on_destroy = false

  services = [
    "serviceusage.googleapis.com",
    "iam.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "pubsub.googleapis.com",
    "logging.googleapis.com",
    "monitoring.googleapis.com",
    "storage-api.googleapis.com",
    "storage-component.googleapis.com",
    "container.googleapis.com",
    "oslogin.googleapis.com",
    "bigquery-json.googleapis.com",
    "containerregistry.googleapis.com",
    "compute.googleapis.com",
    "deploymentmanager.googleapis.com",
    "replicapool.googleapis.com",
    "replicapoolupdater.googleapis.com",
    "resourceviews.googleapis.com",
    "cloudbuild.googleapis.com",
    "sourcerepo.googleapis.com",
  ]
}

# Create GKE cluster
module "gke_cluster" {
  source             = "modules/cluster"
  name               = "${var.name}"
  initial_node_count = "3"
  machine_type       = "n1-standard-4"
}

# k8s provider is used for installing helm
provider "kubernetes" {
  load_config_file       = false
  version                = "~> 1.1"
  host                   = "${module.gke_cluster.host}"
  username               = "${module.gke_cluster.username}"
  password               = "${module.gke_cluster.password}"
  client_certificate     = "${module.gke_cluster.client_certificate}"
  client_key             = "${module.gke_cluster.client_key}"
  cluster_ca_certificate = "${module.gke_cluster.cluster_ca_certificate}"
}

# Configure kubectl CLI. kubernetes provider does not support ClusterRoleBinding API
# Issue https://github.com/hashicorp/terraform/issues/15194
# Waiting for PR https://github.com/terraform-providers/terraform-provider-kubernetes/pull/73 to be merged

module "kubectl_config" {
  source                 = "modules/kubectl"
  host                   = "${module.gke_cluster.host}"
  username               = "${module.gke_cluster.username}"
  password               = "${module.gke_cluster.password}"
  client_certificate     = "${module.gke_cluster.client_certificate}"
  client_key             = "${module.gke_cluster.client_key}"
  cluster_ca_certificate = "${module.gke_cluster.cluster_ca_certificate}"
}

#Initialize and configure helm
module "helm_init" {
  source          = "modules/helm"
  kubeconfig_file = "${module.kubectl_config.kubeconfig_file}"

  # Wait for kubectl config to complete. ** Workaround for missing module dependency in terraform **
  # Refer https://medium.com/@bonya/terraform-adding-depends-on-to-your-custom-modules-453754a8043e
  depends_on = [
    "${module.kubectl_config.kubeconfig_setup_id}",
  ]
}

# Install spinnaker chart
module "deploy_spinnaker" {
  source          = "modules/spinnaker"
  project         = "${var.project}"
  gcs_location    = "${var.gcs_location}"
  kubeconfig_file = "${module.kubectl_config.kubeconfig_file}"
  host            = "${module.gke_cluster.host}"

  # Wait for helm init to complete.
  depends_on = [
    "${module.helm_init.init_id}",
  ]
}
