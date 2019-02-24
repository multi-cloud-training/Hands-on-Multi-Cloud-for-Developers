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

variable deployment_name {
  description = "Prefix used for GCP resources."
}

variable region {
  description = "Region for managed instance groups"
  default = "us-central1"
}

variable zone {
  description = "Zone for managed instance groups"
  default = "us-central1-f"
}

variable cidr_range {
  description = "CIDR range used for spinnaker network."
  default = "10.254.0.0/24"
}

variable spinnaker_machine_type {
  description = "Machine type for the VM running Spinnaker components"
  default = "n1-standard-4"
}

variable spinnaker_image {
  description = "Packer image used for spinnaker VM"
  default = "spinnaker-packer-1491247378"
}

variable jenkins_machine_type {
  description = "Machine type for the VM running Jenkins"
  default = "n1-standard-1"
}

variable jenkins_ip {
  description = "Internal address for the Jenkins instance"
  default = "10.254.0.201"
}

variable jenkins_password {
  description = "Default password for the Jenkins instance"
}

variable redis_machine_type {
  description = "Machine type for the VM running Redis"
  default = "n1-highmem-2"
}

variable redis_ip {
  description = "Internal address for the Redis instance"
  default = "10.254.0.202"
}
