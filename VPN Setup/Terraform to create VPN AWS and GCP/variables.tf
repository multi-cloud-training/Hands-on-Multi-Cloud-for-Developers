variable "aws_cidr" {
  description = "CIDR group for AWS network"
  type        = "string"
}

variable "gcp_cidr" {
  description = "CIDR group for GCP network"
  type        = "string"
}

variable "gcp_network" {
  description = "Network name for GCP"
  type        = "string"
}

variable "gcp_region" {
  description = "Region for GCP"
  type        = "string"
}

variable "aws_region" {
  description = "Region for AWS"
  type        = "string"
}

variable "aws_vpc" {
  description = "VPC ID for AWS"
  type        = "string"
}

variable "aws_sg" {
  description = "Security group for AWS Network"
  type        = "string"
}

variable "aws_route_table_id" {
  description = "Routing table ID for AWS"
  type        = "string"
}
