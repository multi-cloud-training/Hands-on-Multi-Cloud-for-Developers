data_dir   = "/mnt/nomad"
datacenter = "gcp"
region = "gcp"

bind_addr = "0.0.0.0"

advertise {
  # Defaults to the node's hostname. If the hostname resolves to a loopback
  # address you must manually configure advertise addresses.
  http = "$PRIVATE_IP"
  rpc  = "$PRIVATE_IP"
  serf = "$PRIVATE_IP" 
}

server {
    enabled = true

    # Self-elect, should be 3 or 5 for production
    bootstrap_expect = ${instances}
}
