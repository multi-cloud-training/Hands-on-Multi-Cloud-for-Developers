resource "datadog_timeboard" "events" {
  title       = "Events Timeboard (created via Terraform)"
  description = "created using the Datadog provider in Terraform"
  read_only   = true

  graph {
    title = "Publisher"
    viz   = "timeseries"

    request {
      q    = "sum:nomad.publisher.badrequest{*} by {cloud}.as_count()"
      type = "bars"

      style {
        palette = "warm"
      }
    }

    request {
      q    = "sum:nomad.publisher.success{*} by {cloud}.as_count()"
      type = "bars"
    }
  }

  graph {
    title = "Receiver"
    viz   = "timeseries"

    request {
      q    = "sum:nomad.receiver.badrequest{*} by {cloud}.as_count()"
      type = "bars"

      style {
        palette = "warm"
      }
    }

    request {
      q    = "sum:nomad.receiver.received{*} by {cloud}.as_count()"
      type = "bars"
    }
  }
}
