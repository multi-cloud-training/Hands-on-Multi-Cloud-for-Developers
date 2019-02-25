## Role that you're going to assume in the master account to do additional operations vs. running as an IAM user or similar.  
variable "masterAccountRole" {
  default = "CrossAccountAdmin"
}
variable "masterAccountId" {
  type = "string"
}

## Add the account roles you want spinnaker to manage.  Terraform will assume the below role to apply an account policy and grant your "master" account permissions to the role.  You'll need to be able to assume these roles in that remote account and have the trust relationship setup for the master account.  That's an entirely different discussion.  There's even debate of whether you should set this up here, but for demo purposes it will work.  Just be cognizant of how multi-account setups work and the security implications to granting access before you'd EVER run this in prod!!
## 
variable "subAccountRole" {
  type = "list"
  #default = ["arn:aws::iam:1234566:role/CrossAccountAdmin"]
}
