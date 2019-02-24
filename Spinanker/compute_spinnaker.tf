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

resource "google_compute_http_health_check" "spinnaker" {
  name = "${var.deployment_name}-spinnaker"
  request_path = "/"
  port = 9000
}

resource "google_compute_instance_template" "spinnaker" {
  name_prefix = "spinnaker-"

  machine_type = "${var.spinnaker_machine_type}"

  region = "${var.region}"

  tags = [
    "spinnaker-vm",
    "allow-ssh"
  ]

  network_interface {
    subnetwork = "${google_compute_subnetwork.spinnaker.name}"
    access_config {
    }
  }

  disk {
    auto_delete = true
    boot = true
    source_image = "${var.spinnaker_image}"
    type = "PERSISTENT"
    disk_type = "pd-ssd"
  }

  service_account {
    email = "default"
    scopes = [
      "https://www.googleapis.com/auth/compute",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring.write",
      "https://www.googleapis.com/auth/devstorage.full_control"
    ]
  }

  metadata {
    deployment = "${var.deployment_name}"
    region = "${var.region}"
    zone = "${var.zone}"
    jenkinsIP = "${var.jenkins_ip}"
    jenkinsPassword = "${var.jenkins_password}"
    redisIP = "${var.redis_ip}"
    spinnakerLocal = "${file("${path.module}/config/spinnaker-local.yml")}"
    startup-script = "${file("${path.module}/scripts/spinnaker.sh")}"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource google_compute_target_pool "spinnaker" {
  name = "${var.deployment_name}-spinnaker"

  health_checks = [
    "${google_compute_http_health_check.spinnaker.name}"
  ]
}

resource "google_compute_instance_group_manager" "spinnaker" {
  name = "${var.deployment_name}-spinnaker"
  description = "Spinnaker VM Instance Group"

  base_instance_name = "${var.deployment_name}-spinnaker"

  instance_template = "${google_compute_instance_template.spinnaker.self_link}"

  zone = "${var.zone}"

  update_strategy = "RESTART"

  target_pools = [
    "${google_compute_target_pool.spinnaker.self_link}"
  ]

  target_size = 1

  named_port {
    name = "http"
    port = 9000
  }
}
