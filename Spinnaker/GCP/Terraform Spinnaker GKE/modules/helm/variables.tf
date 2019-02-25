variable "service_account" {
  default     = "tiller"
  description = "Service account to install Tiller with. Default to 'tiller'"
}

variable "namespace" {
  default     = "kube-system"
  description = "Set an alternative Tiller namespace. Default to 'kube-system'"
}

variable "kubeconfig_file" {
  description = "Location of kubeconfig file. To be used for kubectl CLI"
}

# Refer https://medium.com/@bonya/terraform-adding-depends-on-to-your-custom-modules-453754a8043e
variable "depends_on" {
  default     = []
  type        = "list"
  description = "Hack for expressing module to module dependency"
}
