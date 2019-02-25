variable "namespace" {}

variable "instances" {
  description = "The number of nomad servers."
}

# Consul configuration
variable "consul_enabled" {
  description = "Should consul be installed onto the instance?"
}

variable "consul_type" {
  description = "Is the consul instance a server or client"
}

variable "consul_version" {
  description = "Version number for nomad"
}

variable "consul_join_tag_key" {
  description = "AWS Tag to use for consul auto-join"
}

variable "consul_join_tag_value" {
  description = "Value to search for in auto-join tag to use for consul auto-join"
}

variable "consul_wan" {
  description = "Consul WAN address for joining clusters"
}

# Nomad configuration
variable "nomad_enabled" {
  description = "Is nomad enabled on this instance"
}

variable "nomad_type" {
  description = "Is nomad a server or an agent"
}

variable "nomad_version" {
  description = "Version number for nomad"
}

variable "nomad_consul_uri" {
  description = "Location of consul server for bootstrapping"
  default     = "http://localhost:8500"
}

variable "public_key_path" {
  description = "The absolute path on disk to the SSH public key."
  default     = "~/.ssh/id_rsa.pub"
}

# HashiUI configuration
variable "hashiui_enabled" {
  description = "Is HashiUI enabled on this instance"
}

variable "hashiui_version" {
  description = "Version number for hashi-ui"
}
