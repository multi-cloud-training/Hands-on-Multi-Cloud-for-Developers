#!/bin/bash

echo 'launching pubsub publisher'

python sensors.py --speedFactor=60 --project=$DEVSHELL_PROJECT_ID

echo '+'
