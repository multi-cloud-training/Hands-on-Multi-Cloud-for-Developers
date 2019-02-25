#!/bin/bash

gsutil cp gs://cloud-training-demos/sandiego/sensor_obs2008.csv.gz .

sudo pip install --upgrade google-cloud-pubsub
