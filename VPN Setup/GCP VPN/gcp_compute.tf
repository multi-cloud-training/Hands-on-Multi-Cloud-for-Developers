data "google_compute_zones" "available" {
  region = "${var.gcp_region}"
}

resource "google_compute_instance" "iperf" {
  name         = "${var.gcp_region}-iperf"
  machine_type = "${var.gcp_instance_type}"
  zone         = "${data.google_compute_zones.available.names[0]}"

  tags = ["${google_compute_network.gcp-network.name}-allow-ssh"]

  disk {
    image = "ubuntu-os-cloud/ubuntu-1604-lts"
  }

  network_interface {
    network = "${google_compute_network.gcp-network.name}"
    
    access_config {
      // Ephemeral IP
    }
  }

  metadata_startup_script = "${file("gcp_userdata.sh")}"
}