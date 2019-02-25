output "userdata" {
  value = "${data.template_file.userdata.rendered}"
}

output "userdata_ssh_emulator" {
  value = "${data.template_file.userdata_ssh.rendered}"
}

output "ssh_emulator" {
  value = "${data.template_file.ssh_emulator.rendered}"
}
