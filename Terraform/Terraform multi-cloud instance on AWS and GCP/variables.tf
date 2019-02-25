variable "namespace" {
  description = <<EOH
The namespace to create the virtual training lab. This should describe the
training and must be unique to all current trainings. IAM users, workstations,
and resources will be scoped under this namespace.

It is best if you add this to your .tfvars file so you do not need to type
it manually with each run
EOH
}

variable "route53_zone" {
  description = "Route53 DNS Zone"
}

# Consul settings
variable "consul_version" {
  description = "Consul version to install"
}

variable "nomad_version" {
  description = "Nomad version to install"
}

variable "consul_join_tag_key" {
  description = "AWS Tag to use for consul auto-join"
}

variable "consul_join_tag_value" {
  description = "Value to search for in auto-join tag to use for consul auto-join"
}

# Nomad settings
variable "nomad_servers" {
  description = "The number of nomad servers."
}

variable "nomad_agents" {
  description = "The number of nomad agents"
}

# HashiUI configuration
variable "hashiui_enabled" {
  description = "Is HashiUI enabled on this instance"
}

variable "hashiui_version" {
  description = "Version number for hashi-ui"
  default     = "0.13.6"
}

# Nats.io settings
variable "nats_connection" {
  description = "Connection string for Nats cloud"
}

variable "aws_region" {
  description = "Region for AWS"
}

variable "gcp_region" {
  description = "Region for GCP"
}
