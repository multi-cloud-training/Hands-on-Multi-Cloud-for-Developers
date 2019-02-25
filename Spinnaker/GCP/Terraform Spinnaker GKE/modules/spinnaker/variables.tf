variable "service_account" {
  default     = "default"
  description = "Service account to install Spinnnaker with. Default to 'default'"
}

variable "host" {
  description = "Teller host, same as GKE cluster host"
}

variable "namespace" {
  default     = "default"
  description = "Spinnaker namespace. Default to 'default'"
}

variable "kubeconfig_file" {
  description = "Location of kubeconfig file. To be used for kubectl CLI"
}

variable "project" {
  description = "Project Id"
}

variable "gcs_location" {
  description = "GCS Location"
}

variable "spinnaker_gcs_sa" {
  default     = "spinnaker-gcs-sa"
  description = "GCS Location"
}

variable "temp_dir" {
  default     = "."
  description = "Temporary directory used for storing spinnaker-values.yaml."
}

# Refer https://medium.com/@bonya/terraform-adding-depends-on-to-your-custom-modules-453754a8043e
variable "depends_on" {
  default     = []
  type        = "list"
  description = "Hack for expressing module to module dependency"
}
