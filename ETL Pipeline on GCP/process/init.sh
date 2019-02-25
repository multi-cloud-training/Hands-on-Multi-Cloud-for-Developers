#!/bin/bash

echo 'creating BigQuery dataset'
echo 'usage: ./run.sh BigQueryDataset.Table'

if ["$#" -ne 1]; then
  echo 'wrong usage'
  exit
fi

BQ=$1

bq mk iot
bq mk -t BQ freeway:STRING,speed:FLOAT,window_start:TIMESTAMP,window_end:TIMESTAMP

sudo pip install --upgrade google-cloud-dataflow

echo '+'
