# Generic
variable "public_key_path" {
  description = "The absolute path on disk to the SSH public key."
  default     = "~/.ssh/id_rsa.pub"
}

variable "instance_type" {
  default = "t2.micro"
}

variable "namespace" {
  description = <<EOH
The namespace to create the virtual training lab. This should describe the
training and must be unique to all current trainings. IAM users, workstations,
and resources will be scoped under this namespace.

It is best if you add this to your .tfvars file so you do not need to type
it manually with each run
EOH
}

# Consul settings
variable "consul_version" {
  description = "Consul version to install"
}

variable "consul_join_tag_key" {
  description = "AWS Tag to use for consul auto-join"
}

variable "consul_join_tag_value" {
  description = "Value to search for in auto-join tag to use for consul auto-join"
}

variable "consul_enabled" {
  description = "Is consul enabled on this instance"
}

variable "consul_wan" {
  description = "Consul WAN address for joining clusters"
}

# Nomad settings
variable "nomad_servers" {
  description = "The number of nomad servers."
}

variable "nomad_enabled" {
  description = "Is nomad enabled on this instance"
}

variable "nomad_agents" {
  description = "The number of nomad agents"
}

variable "nomad_version" {
  description = "Nomad version to install"
}

# HashiUI configuration
variable "hashiui_enabled" {
  description = "Is HashiUI enabled on this instance"
}

variable "hashiui_version" {
  description = "Version number for hashi-ui"
}

variable "gcp_region" {
  description = "region for GCP"
}
