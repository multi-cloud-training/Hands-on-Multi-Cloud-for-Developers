variable aws_instance_type {
  default = "r4.2xlarge"
}

variable EC2_SSH_PUB_KEY {
  description = "Content of public key used when provisioning EC2 instances with SSH access."
}