#!/bin/bash

while true; do
  curl -v -XPOST nomad.demo.gs/product -d '{"name": "nic", "sku":"22323"}'
  sleep .5
done

