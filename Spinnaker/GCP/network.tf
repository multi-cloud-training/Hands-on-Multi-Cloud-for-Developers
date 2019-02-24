# Copyright 2016 Google Inc. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

resource "google_compute_network" "spinnaker" {
  name                    = "spinnaker"
  auto_create_subnetworks = "false"
}

resource "google_compute_subnetwork" "spinnaker" {
  name          = "${var.deployment_name}-spinnaker"
  ip_cidr_range = "${var.cidr_range}"
  network       = "${google_compute_network.spinnaker.self_link}"
}

resource "google_compute_firewall" "spinnaker-vm-fw" {
  name    = "spinnaker-vm-ssh"
  network = "${google_compute_network.spinnaker.name}"

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags = ["allow-ssh"]
}

resource "google_compute_firewall" "spinnaker-redis-fw" {
  name    = "redis-vm"
  network = "${google_compute_network.spinnaker.name}"

  allow {
    protocol = "tcp"
    ports    = ["6379","22"]
  }

  source_tags = ["spinnaker-vm"]
  target_tags = ["redis-vm"]
}

resource "google_compute_firewall" "spinnaker-redis-hc" {
  name    = "redis-hc"
  network = "${google_compute_network.spinnaker.name}"

  allow {
    protocol = "tcp"
    ports    = ["6379"]
  }

  source_ranges = ["130.211.0.0/22"]
  target_tags = ["redis-vm"]
}

resource "google_compute_firewall" "spinnaker-jenkins-fw" {
  name    = "jenkins-vm-from-spinnaker"
  network = "${google_compute_network.spinnaker.name}"

  allow {
    protocol = "tcp"
    ports    = ["80","22"]
  }

  source_tags = ["spinnaker-vm"]
  target_tags = ["jenkins-vm"]
}

resource "google_compute_firewall" "spinnaker-jenkins-hc" {
  name    = "jenkins-vm-hc"
  network = "${google_compute_network.spinnaker.name}"

  allow {
    protocol = "tcp"
    ports    = ["80"]
  }

  source_ranges = ["130.211.0.0/22"]
  target_tags = ["jenkins-vm"]
}

resource "google_compute_firewall" "spinnaker-hc" {
  name    = "spinnaker-vm-hc"
  network = "${google_compute_network.spinnaker.name}"

  allow {
    protocol = "tcp"
    ports    = ["9000"]
  }

  source_ranges = ["130.211.0.0/22"]
  target_tags = ["spinnaker-vm"]
}

resource "google_compute_region_backend_service" "redis" {
  name        = "${var.deployment_name}-redis-backend"
  protocol    = "TCP"
  timeout_sec = 10
  session_affinity = "CLIENT_IP"

  backend {
    group = "${google_compute_instance_group_manager.redis.instance_group}"
  }

  health_checks = ["${google_compute_health_check.redis.self_link}"]
}

resource "google_compute_forwarding_rule" "redis" {
  name       = "redis-instance-group"
  load_balancing_scheme = "INTERNAL"
  backend_service = "${google_compute_region_backend_service.redis.self_link}"
  network    = "${google_compute_network.spinnaker.self_link}"
  subnetwork = "${google_compute_subnetwork.spinnaker.self_link}"
  ip_address = "${var.redis_ip}"
  ports = ["6379"]
}

resource "google_compute_region_backend_service" "jenkins" {
  name        = "${var.deployment_name}-jenkins-backend"
  protocol    = "TCP"
  timeout_sec = 10
  session_affinity = "CLIENT_IP"

  backend {
    group = "${google_compute_instance_group_manager.jenkins.instance_group}"
  }

  health_checks = ["${google_compute_health_check.jenkins.self_link}"]
}

resource "google_compute_forwarding_rule" "jenkins" {
  name       = "jenkins-instance-group"
  load_balancing_scheme = "INTERNAL"
  backend_service = "${google_compute_region_backend_service.jenkins.self_link}"
  network    = "${google_compute_network.spinnaker.self_link}"
  subnetwork = "${google_compute_subnetwork.spinnaker.self_link}"
  ip_address = "${var.jenkins_ip}"
  ports = ["80"]
}
