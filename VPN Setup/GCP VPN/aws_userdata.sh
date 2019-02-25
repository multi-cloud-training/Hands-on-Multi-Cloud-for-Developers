#!/bin/bash -xe
apt-get update
apt-get install -y iperf3

cat > /etc/systemd/system/iperf3.service <<EOF
[Unit]
Description=iPerf 3 Server
[Service]
Restart=always
TimeoutStartSec=0
RestartSec=3
WorkingDirectory=/tmp
ExecStart=/usr/bin/iperf3 -s -p 80
[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable iperf3
systemctl start iperf3
