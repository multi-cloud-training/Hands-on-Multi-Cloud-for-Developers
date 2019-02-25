variable "name" {
  description = "The name of the cluster, unique within the project and zone."
}

variable "node_pool_name" {
  default     = "autoscale-np"
  description = "The name of the node pool"
}

variable "initial_node_count" {
  default     = 3
  description = "The number of nodes to create in this cluster (not including the Kubernetes master). Default to 3"
}

variable "min_node_count" {
  default     = 1
  description = "Minimum number of nodes in the NodePool. Must be >=1 and <= max_node_count Default to 1"
}

variable "max_node_count" {
  default     = 3
  description = "Maximum number of nodes in the NodePool. Must be >= min_node_count. Default to 3"
}

variable "preemptible" {
  default     = true
  description = "A boolean that represents whether or not the underlying node VMs are preemptible. See the official documentation for more information. Defaults to true."
}

variable "machine_type" {
  default     = "n1-standard-1"
  description = "The name of a Google Compute Engine machine type. Defaults to n1-standard-4"
}

variable "min_master_version" {
  default     = "1.10.5-gke.3"
  description = "The minimum version of the master"
}

variable "tags" {
  type        = "list"
  default     = ["spinnaker", "helm"]
  description = "The list of instance tags applied to all nodes. Tags are used to identify valid sources or targets for network firewalls."
}

variable "oauth_scopes" {
  type = "list"

  default = [
    "https://www.googleapis.com/auth/compute",
    "https://www.googleapis.com/auth/devstorage.read_only",
    "https://www.googleapis.com/auth/logging.write",
    "https://www.googleapis.com/auth/monitoring",
  ]

  description = <<EOF
The set of Google API scopes to be made available on all of the node VMs under the "default" service account. These can be either FQDNs, or scope aliases.

The following scopes are necessary to ensure the correct functioning of the cluster:
 - compute-rw (https://www.googleapis.com/auth/compute)
 - storage-ro (https://www.googleapis.com/auth/devstorage.read_only)
 - logging-write (https://www.googleapis.com/auth/logging.write), if logging_service points to Google
 - monitoring (https://www.googleapis.com/auth/monitoring), if monitoring_service points to Google
EOF
}
