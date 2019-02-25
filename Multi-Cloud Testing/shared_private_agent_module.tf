# Create DCOS Mesos Agent Scripts to execute
module "dcos-mesos-agent" {
  source = "github.com/dcos/tf_dcos_core"
  bootstrap_private_ip = "${aws_instance.bootstrap.private_ip}"
  dcos_install_mode    = "${var.state}"
  dcos_version         = "${var.dcos_version}"
  dcos_type            = "${var.dcos_type}"
  role                 = "dcos-mesos-agent"
}
