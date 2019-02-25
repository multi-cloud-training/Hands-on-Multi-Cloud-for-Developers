provider "google" {
  project = "dan-hashicorp-001"
  region  = "us-west1"
}

# Local resources
resource "google_compute_forwarding_rule" "http" {
  name       = "${var.configuration_name}-forward-http"
  target     = "${google_compute_target_pool.web.self_link}"
  port_range = "80"
}

resource "google_compute_target_pool" "web" {
  name = "${var.configuration_name}-target-pool"
}

resource "google_compute_region_instance_group_manager" "default" {
  name               = "${var.configuration_name}"
  base_instance_name = "web-server"
  instance_template  = "${google_compute_instance_template.default.self_link}"
  target_pools       = ["${google_compute_target_pool.web.self_link}"]
  region             = "us-west1"
  target_size        = 3
  depends_on         = ["google_compute_instance_template.default"]

  named_port {
    name = "http"
    port = 80
  }
}

resource "google_compute_instance_template" "default" {
  machine_type            = "f1-micro"
  tags                    = ["http-server"]
  can_ip_forward          = "true"
  metadata_startup_script = "${data.template_file.web_server_google.rendered}"

  network_interface {
    network       = "default"
    access_config = [{}]
  }

  disk {
    auto_delete  = true
    boot         = true
    source_image = "centos-cloud/centos-7"
    type         = "PERSISTENT"
    disk_type    = "pd-ssd"
  }

  service_account {
    scopes = [
      "https://www.googleapis.com/auth/compute",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring.write",
      "https://www.googleapis.com/auth/devstorage.full_control",
    ]
  }

  lifecycle {
    create_before_destroy = true
  }
}

# Data sources
data "template_file" "web_server_google" {
  template = "${file("${path.module}/web-server.tpl")}"

  vars {
    cloud = "gcp"
  }
}
