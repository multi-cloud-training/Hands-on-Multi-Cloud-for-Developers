from __future__ import absolute_import

import argparse
import logging
import datetime, os
import apache_beam as beam
from helpers import *

PROJECT_ID = 'udemy-data-engineer-210920'
BUCKET_ID = 'udemy-data-engineer-210920'
BUCKET_FOLDER = 'dataflow-pipeline-py'

TOP_N = 5

def create_popularity_view(pcollection):
      
    return (
      pcollection
        | 'PackageUsage' >> beam.FlatMap(lambda rowdict: resolve_package_usage(rowdict['content'], 'import'))
        | 'TotalPackageUsage' >> beam.CombinePerKey(sum)
        | 'TopNPackageUsage' >> beam.transforms.combiners.Top.Of(TOP_N, compare_by_value)
      )

def create_help_view(pcollection):
     
    return (
      pcollection
        | 'PackageHelpScore' >> beam.FlatMap(lambda rowdict: resolve_package_help_score(rowdict['content'], 'package'))
        | 'TotalPackageHelpScore' >> beam.CombinePerKey(sum)
        | 'RemoveZeros' >> beam.Filter(lambda packages: packages[1]>0 )
      )

def create_pipeline(argv, runner, project, bucket, folder):
      
  limit_records=''
  if runner == 'DirectRunner':
      limit_records='LIMIT 3000'

  source_query = 'SELECT content FROM [fh-bigquery:github_extracts.contents_java_2016] {0}'.format(limit_records)

  pipeline = beam.Pipeline(argv=argv)

  source = pipeline | 'Source' >> beam.io.Read(beam.io.BigQuerySource(project=project,
                                                                      query=source_query))

  popularity_view = create_popularity_view(source)

  help_view = create_help_view(source)

  combined = popularity_view | 'Scores' >> beam.FlatMap(lambda element, the_dict: calculate_composite_score(element, the_dict), beam.pvalue.AsDict(help_view))
  
  output_prefix = 'gs://{0}/{1}/output'.format(BUCKET_ID, BUCKET_FOLDER)
  
  combined | 'Sink' >> beam.io.WriteToText(output_prefix,
                                           file_name_suffix='.csv',
                                           shard_name_template='')

  return pipeline

def run():

  parser = argparse.ArgumentParser(description='BigQuery as source & side input')
  parser.add_argument('--bucket',
                      default=BUCKET_ID,
                      help='Specify Cloud Storage bucket for output')
  parser.add_argument('--folder',
                      default=BUCKET_FOLDER,
                      help='Specify Cloud Storage bucket folder for output')
  parser.add_argument('--project',
                      default=PROJECT_ID,
                      help='Specify Google Cloud project')

  group = parser.add_mutually_exclusive_group(required=False)
  group.add_argument('--local',
                     action='store_true')
  group.add_argument('--dataflow',
                     action='store_true')

  opts = parser.parse_args()

  if opts.local:
    runner='DirectRunner'
  else:
    runner='DataFlowRunner'

  bucket = opts.bucket
  folder = opts.folder
  project = opts.project

  argv = [
    '--project={0}'.format(project),
    '--job_name=cooljob',
    '--save_main_session',
    '--staging_location=gs://{0}/{1}/staging/'.format(bucket, folder),
    '--temp_location=gs://{0}/{1}/staging/'.format(bucket, folder),
    '--runner={0}'.format(runner)
    ]

  pipeline = create_pipeline(argv, runner, project, bucket, folder)

  if runner == 'DataFlowRunner':
        pipeline.run()
  else:
        pipeline.run().wait_until_finish()
  
  logging.getLogger().setLevel(logging.INFO)


if __name__ == '__main__':
      
      run()

