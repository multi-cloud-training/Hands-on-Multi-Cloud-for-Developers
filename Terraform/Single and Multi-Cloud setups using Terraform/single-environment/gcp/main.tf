#####################################################################
# Google Cloud Platform
#####################################################################
provider "google" {
  credentials = "${file("~/.config/gcloud/terraform-admin.json")}"
  project     = "terraform-demo-project"
  region      = "us-east1"
}

#####################################################################
# Firewall Rules
#####################################################################
resource "google_compute_firewall" "allow-http-all" {
  name    = "allow-http"
  network = "default"

  allow {
    protocol = "tcp"
    ports    = ["80"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["http"]
}

#####################################################################
# Variables
#####################################################################
variable "terraform_zone" {
  type        = "string"
  default     = "us-central1-a"
  description = "The zone to provision into"
}

#####################################################################
# Disks
#####################################################################
resource "google_compute_disk" "terraform" {
  name     = "terraform"
  type     = "pd-standard"
  zone     = "${var.terraform_zone}"
  size     = 10
}

#####################################################################
# VM Instances
#####################################################################
resource "google_compute_instance" "terraform" {
  name         = "terraform"
  machine_type = "f1-micro"
  zone         = "${var.terraform_zone}"

  tags = ["http"]

  boot_disk {
    source      = "${google_compute_disk.terraform.name}"
    auto_delete = true
  }

  network_interface {
    network = "default"

    access_config {
      # ephemeral external ip address
    }
  }

  scheduling {
    preemptible         = false
    on_host_maintenance = "MIGRATE"
    automatic_restart   = true
  }

  # provisioner "remote-exec" {
  #   inline = [
  #     "echo 'hello from $HOSTNAME' > ~/terraform_complete",
  #   ]

  #   connection {
  #     type        = "ssh"
  #     user        = "ubuntu"
  #     private_key = "${file("~/.ssh/google_compute_engine")}"
  #   }
  # }
}
