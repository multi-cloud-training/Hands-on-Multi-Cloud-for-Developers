data "template_file" "config_consul" {
  template = "${file("${path.module}/templates/consul-${var.consul_type}.json.tpl")}"

  vars {
    instances             = "${var.instances}"
    consul_join_tag_value = "${var.consul_join_tag_key}"
    consul_wan            = "${var.consul_wan}"
  }
}

data "template_file" "config_nomad" {
  template = "${file("${path.module}/templates/nomad-${var.nomad_type}.hcl.tpl")}"

  vars {
    instances = "${var.instances}"
  }
}

data "template_file" "startup" {
  template = "${file("${path.module}/templates/startup.sh.tpl")}"

  vars {
    consul_enabled = "${var.consul_enabled}"
    consul_version = "${var.consul_version}"

    consul_type = "${var.consul_type}"

    consul_config = "${data.template_file.config_consul.rendered}"

    nomad_enabled = "${var.nomad_enabled}"
    nomad_version = "${var.nomad_version}"

    nomad_type = "${var.nomad_type}"

    nomad_config = "${data.template_file.config_nomad.rendered}"

    hashiui_enabled = "${var.hashiui_enabled}"
    hashiui_version = "${var.hashiui_version}"
  }
}

resource "google_compute_instance_template" "nomad" {
  name           = "nomad-cluster-${var.nomad_type}"
  machine_type   = "n1-standard-1"
  can_ip_forward = false

  disk {
    source_image = "ubuntu-os-cloud/ubuntu-1604-lts"
  }

  network_interface {
    network = "default"

    access_config {}
  }

  tags = ["${var.consul_join_tag_key}"]

  metadata {
    sshKeys = "nicj:${file(var.public_key_path)}"
  }

  metadata_startup_script = "${data.template_file.startup.rendered}"

  service_account {
    scopes = ["https://www.googleapis.com/auth/compute.readonly"]
  }
}

resource "google_compute_target_pool" "nomad" {
  name = "nomad-cluster-${var.nomad_type}"

  session_affinity = "NONE"
}

resource "google_compute_instance_group_manager" "nomad" {
  name = "nomad-${var.nomad_type}"
  zone = "us-central1-f"

  instance_template  = "${google_compute_instance_template.nomad.self_link}"
  target_pools       = ["${google_compute_target_pool.nomad.self_link}"]
  base_instance_name = "nomad"
}

resource "google_compute_autoscaler" "nomad" {
  name   = "nomad-${var.nomad_type}"
  zone   = "us-central1-f"
  target = "${google_compute_instance_group_manager.nomad.self_link}"

  autoscaling_policy = {
    max_replicas    = 5
    min_replicas    = "${var.instances}"
    cooldown_period = 60

    cpu_utilization {
      target = 0.5
    }
  }
}
