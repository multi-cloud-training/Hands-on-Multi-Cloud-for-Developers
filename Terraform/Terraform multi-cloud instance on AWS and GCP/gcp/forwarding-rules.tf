resource "google_compute_global_address" "external-address" {
  name = "${var.namespace}-fabio"
}

resource "google_compute_global_forwarding_rule" "fabio" {
  name       = "${var.namespace}-fabio"
  target     = "${google_compute_target_http_proxy.default.self_link}"
  ip_address = "${google_compute_global_address.external-address.address}"
  port_range = "80"
}

resource "google_compute_target_http_proxy" "default" {
  name        = "fabio"
  description = "a description"
  url_map     = "${google_compute_url_map.default.self_link}"
}

resource "google_compute_url_map" "default" {
  name            = "fabio"
  description     = "a description"
  default_service = "${google_compute_backend_service.default.self_link}"

  host_rule {
    hosts        = ["*"]
    path_matcher = "allpaths"
  }

  path_matcher {
    name            = "allpaths"
    default_service = "${google_compute_backend_service.default.self_link}"

    path_rule {
      paths   = ["/*"]
      service = "${google_compute_backend_service.default.self_link}"
    }
  }
}

resource "google_compute_backend_service" "default" {
  name        = "fabio"
  port_name   = "http"
  protocol    = "HTTP"
  timeout_sec = 10
  enable_cdn  = false

  backend {
    group = "${module.nomad-clients.target_group}"
  }

  health_checks = ["${google_compute_http_health_check.default.self_link}"]
}

resource "google_compute_http_health_check" "default" {
  name               = "fabio"
  request_path       = "/nginx"
  port               = "80"
  check_interval_sec = 1
  timeout_sec        = 1
}
