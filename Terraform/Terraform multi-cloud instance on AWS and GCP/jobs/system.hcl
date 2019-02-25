job "fabio" {
  datacenters = ["aws", "gcp"]
  type        = "system"

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

    task "fabio" {
      driver = "docker"

      env = {
        registry.consul.addr = "${NOMAD_IP_http}:8500"
      }

      config {
        image = "magiconair/fabio:latest"

        port_map {
          http = 9999
        }
      }

      resources {
        cpu    = 250 # 500 MHz
        memory = 128 # 256MB

        network {
          mbits = 10

          port "http" {
            static = "80"
          }

          port "admin" {
            static = "9998"
          }
        }
      }

      service {
        name = "fabio"
        tags = ["router"]
        port = "admin"

        check {
          name     = "alive"
          type     = "http"
          interval = "10s"
          timeout  = "2s"
          path     = "/health"
        }
      }
    }

    task "dogstatsd" {
      driver = "docker"

      env = {
        API_KEY = "6980eb5311fa8e6b5822a6a562bf7c6e"
      }

      config {
        image = "datadog/docker-dogstatsd"

        port_map {
          statsd = 8125
        }
      }

      resources {
        cpu    = 250 # 500 MHz
        memory = 128 # 256MB

        network {
          mbits = 10

          port "statd" {
            static = "8125"
          }
        }
      }
    }
  }
}
