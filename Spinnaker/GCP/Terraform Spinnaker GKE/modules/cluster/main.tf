resource "google_container_cluster" "primary" {
  name               = "${var.name}"
  min_master_version = "${var.min_master_version}"

  node_pool {
    name               = "${var.node_pool_name}"
    initial_node_count = "${var.initial_node_count}"

    autoscaling {
      min_node_count = "${var.min_node_count}"
      max_node_count = "${var.max_node_count}"
    }

    node_config {
      preemptible  = "${var.preemptible}"
      machine_type = "${var.machine_type}"
      tags         = "${var.tags}"
      oauth_scopes = "${var.oauth_scopes}"
    }
  }
}
