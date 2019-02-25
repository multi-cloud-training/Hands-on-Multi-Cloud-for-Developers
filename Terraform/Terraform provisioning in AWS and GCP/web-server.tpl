#!/bin/bash

new_hostname="multi-cloud-$(hostname)"

# set the hostname
hostnamectl set-hostname "$${new_hostname}"

# install httpd and create a landing page
yum install -y httpd
service httpd start
chkconfig httpd on
cat > /var/www/html/index.html <<EOF
<html>
<head><title>$${new_hostname}</title></head>
<body><h1 style="font-family: monospace;">${cloud} $${new_hostname}</h1></body>
</html>
EOF
