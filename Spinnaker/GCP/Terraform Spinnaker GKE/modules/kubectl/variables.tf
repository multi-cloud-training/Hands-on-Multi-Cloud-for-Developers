variable "temp_dir" {
  default     = "."
  description = "Temporary directory used for storing kubeconfig, certs files"
}

variable "kubeconfig_file" {
  default     = "/tmp/.kubeconfig"
  description = "Kube config file path"
}

variable "cluster_ca_certificate" {
  description = "PEM-encoded root certificates bundle for TLS authentication."
}

variable "client_key" {
  description = "PEM-encoded client certificate key for TLS authentication."
}

variable "client_certificate" {
  description = "PEM-encoded client certificate for TLS authentication."
}

variable "host" {
  description = "The hostname (in form of URI) of Kubernetes master"
}

variable "username" {
  description = "The username to use for HTTP basic authentication when accessing the Kubernetes master endpoint."
}

variable "password" {
  description = "The password to use for HTTP basic authentication when accessing the Kubernetes master endpoint."
}
