job "events" {
  datacenters = ["aws", "gcp"]
  type        = "service"

  update {
    stagger      = "10s"
    max_parallel = 1
  }

  group "pubsub" {
    constraint {
      distinct_hosts = true
    }

    restart {
      attempts = 10
      interval = "5m"
      delay    = "25s"
      mode     = "delay"
    }

    task "publisher" {
      driver = "docker"

      config {
        image = "nicholasjackson/example-nats-publisher:latest"

        port_map {
          http = 8080
        }
      }

      env {
        NATS_CONNECTION   = "tls://test:9XpKSQucPlwNM1SVD5QzYf@n0.us-east-1.aws.prod.nats.cloud:10800"
        STATSD_CONNECTION = "${NOMAD_IP_http}:8125"
        STATSD_TAG        = "cloud:${node.datacenter}"
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
        name = "publisher"

        tags = [
          "urlprefix-/product",
        ]

        port = "http"

        check {
          name     = "alive"
          type     = "http"
          interval = "10s"
          timeout  = "2s"
          path     = "/product"
        }
      }
    }

    task "receiver" {
      driver = "docker"

      config {
        image = "nicholasjackson/example-nats-receiver:latest"

        port_map {
          http = 8080
        }
      }

      env {
        NATS_CONNECTION   = "tls://test:9XpKSQucPlwNM1SVD5QzYf@n0.us-east-1.aws.prod.nats.cloud:10800"
        STATSD_CONNECTION = "${NOMAD_IP_http}:8125"
        STATSD_TAG        = "cloud:${node.datacenter}"
      }

      resources {
        cpu    = 250 # 500 MHz
        memory = 128 # 256MB

        network {
          mbits = 10

          port "http" {}
        }
      }
    }
  }
}
