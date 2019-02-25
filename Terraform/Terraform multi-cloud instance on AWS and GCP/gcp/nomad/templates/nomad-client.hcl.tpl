datacenter = "gcp"
region = "gcp"
data_dir   = "/mnt/nomad"

bind_addr = "0.0.0.0"

advertise {
  # Defaults to the node's hostname. If the hostname resolves to a loopback
  # address you must manually configure advertise addresses.
  http = "$PRIVATE_IP"
  rpc  = "$PRIVATE_IP"
  serf = "$PRIVATE_IP" 
}

client {
  enabled = true
}
