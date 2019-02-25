#!/usr/bin/env python

import apache_beam as beam
import argparse
import logging


BUCKET_ID = 'udemy-data-engineer-210920'
BUCKET_FOLDER = 'iot-stream'


class SpeedOnFreewayFn(beam.DoFn):

    def process(self, el):

        #logging.info('SpeedOnFreewayFn in {}'.format(el))

        parsed = el.split(',')

        freeway_and_speed = (str(parsed[3]), float(parsed[6]))

        #logging.info('SpeedOnFreewayFn out {}'.format(freeway_and_speed))

        yield freeway_and_speed

class FormatBQRowFn(beam.DoFn):

    def process(self, el, window=beam.DoFn.WindowParam):

        #logging.info('FormatBQRowFn in {}'.format(el))

        ts_format = '%Y-%m-%d %H:%M:%S.%f UTC'

        window_start = window.start.to_utc_datetime().strftime(ts_format)
        window_end = window.end.to_utc_datetime().strftime(ts_format)

        formatted = {
            'freeway': str(el[0]),
            'speed': el[1],
            'window_start': window_start,
            'window_end': window_end
            }

        #logging.info('FormatBQRowFn out {}'.format(formatted))

        yield formatted

def resolve_average_speed(el):

    (freeway, speed) = el

    average_speed = sum(speed)/len(speed) if len(speed) > 0 else 0

    return (freeway, average_speed)

def run():

    parser = argparse.ArgumentParser()

    parser.add_argument('--pubsub',
                        required=True,
                        help='PubSub topic')
    parser.add_argument('--bq',
                        required=True,
                        help='BigQuery table')
    parser.add_argument('--project',
                        required=True,
                        help='Project ID')

    args = parser.parse_args()

    argv = [
      '--project={0}'.format(args.project),
      '--job_name=iotstream',
      '--save_main_session',
      '--staging_location=gs://{0}/{1}/staging/'.format(BUCKET_ID, BUCKET_FOLDER),
      '--temp_location=gs://{0}/{1}/staging/'.format(BUCKET_ID, BUCKET_FOLDER),
      '--runner=DataflowRunner',
      '--streaming']

    with beam.Pipeline(argv=argv) as pipeline:

        pubsub_topic_path = 'projects/{0}/topics/{1}'.format(args.project,
                                                             args.pubsub)

        stream = pipeline | beam.io.ReadFromPubSub(pubsub_topic_path)

        speeds = stream | 'SpeedOnFreeway' >> beam.ParDo(SpeedOnFreewayFn())
        #speeds = stream | 'SpeedOnHighway' >> beam.Map(lambda x: (x[3], float(x[6])))

        window = speeds | beam.WindowInto(beam.transforms.window.FixedWindows(5, 0))

        average = (window
            | 'Group' >> beam.GroupByKey()
            | 'Average' >> beam.Map(resolve_average_speed))


        formatted = average | 'Format' >> beam.ParDo(FormatBQRowFn())
        #| 'FormatForBQ' >> beam.Map(lambda x: {'freeway': str(x[0]), 'speed': x[1]})

        formatted | 'SinkToBQ' >> beam.io.WriteToBigQuery(args.bq,
                schema='freeway:STRING, speed:FLOAT, window_start:TIMESTAMP, window_end:TIMESTAMP',
                create_disposition=beam.io.BigQueryDisposition.CREATE_IF_NEEDED,
                write_disposition=beam.io.BigQueryDisposition.WRITE_APPEND)

if __name__ == '__main__':

    run()
