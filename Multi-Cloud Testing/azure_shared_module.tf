# Provide tested AMI and user from listed region startup commands
  module "azure-tested-oses" {
      source   = "./modules/dcos-tested-azure-oses"
      provider = "azure"
      os       = "${var.os}"
      region   = "${var.azure_region}"
}
