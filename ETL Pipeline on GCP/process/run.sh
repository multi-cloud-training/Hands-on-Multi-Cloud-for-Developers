#!/bin/bash

echo 'launching streaming pipeline'
echo 'usage: ./run.sh Project BigQueryDataset.Table PubSubTopic'

if ["$#" -ne 3]; then
  echo 'wrong usage'
  exit
fi

PROJECT=$1
BQ=$2
PUBSUB=$3

python pipeline.py --project=$PROJECT --bq=$BQ --pubsub=$PUBSUB

echo '+'
