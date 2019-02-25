data "google_compute_zones" "available" {}

data "google_compute_subnetwork" "default" {
  name   = "default"
  region = "${var.gcp_region}"
}

resource "google_compute_firewall" "http" {
  name          = "http"
  network       = "default"
  source_ranges = ["0.0.0.0/0"]

  allow {
    protocol = "tcp"
    ports    = ["80"]
  }
}
