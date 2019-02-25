job "nginx" {
  datacenters = ["aws", "gcp"]
  type        = "service"

  update {
    stagger      = "10s"
    max_parallel = 1
  }

  group "web" {
    constraint {
      distinct_hosts = true
    }

    restart {
      attempts = 10
      interval = "5m"
      delay    = "25s"
      mode     = "delay"
    }

    task "nginx" {
      driver = "docker"

      config {
        image = "nginx:latest"

        port_map {
          http = 80
        }
      }

      resources {
        cpu    = 250 # 500 MHz
        memory = 128 # 256MB

        network {
          mbits = 10

          port "http" {}
        }
      }

      service {
        name = "nginx"

        tags = [
          "urlprefix-/nginx strip=/nginx",
        ]

        port = "http"

        check {
          name     = "alive"
          type     = "http"
          interval = "10s"
          timeout  = "2s"
          path     = "/"
        }
      }
    }
  }
}
