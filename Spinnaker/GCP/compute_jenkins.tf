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

resource "google_compute_health_check" "jenkins" {
  name = "${var.deployment_name}-jenkins"

  tcp_health_check {
    port = "80"
  }
}

resource "google_compute_instance_template" "jenkins" {
  name_prefix = "jenkins-"

  machine_type = "${var.jenkins_machine_type}"

  region = "${var.region}"

  tags = [
    "jenkins-vm"
  ]

  network_interface {
    subnetwork = "${google_compute_subnetwork.spinnaker.name}"
    access_config {
    }
  }

  disk {
    auto_delete = true
    boot = true
    source_image = "projects/ubuntu-os-cloud/global/images/family/ubuntu-1604-lts"
    type = "PERSISTENT"
    disk_type = "pd-standard"
  }

  service_account {
    email = "default"
    scopes = [
      "https://www.googleapis.com/auth/cloud.useraccounts.readonly",
      "https://www.googleapis.com/auth/devstorage.read_only",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring.write"
    ]
  }

  metadata {
    jenkinsPassword = "${var.jenkins_password}"
    startup-script = "${file("${path.module}/scripts/jenkins.sh")}"
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "google_compute_instance_group_manager" "jenkins" {
  name = "${var.deployment_name}-jenkins"
  description = "Jenkins VM Instance Group"

  base_instance_name = "${var.deployment_name}-jenkins"

  instance_template = "${google_compute_instance_template.jenkins.self_link}"

  zone = "${var.zone}"

  update_strategy = "RESTART"

  target_size = 1

  named_port {
    name = "http"
    port = 80
  }
}
