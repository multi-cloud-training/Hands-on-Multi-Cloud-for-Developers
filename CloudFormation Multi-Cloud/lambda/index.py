import json
import logging
import signal
import requests
import boto3
import os
import traceback
import sys
from azure_handler import AzureResourceHandler
from googlecloud_handler import GoogleCloudResourceHandler

LOGGER = logging.getLogger()
LOGGER.setLevel(logging.INFO)

def handler(event, context):
    # Setup alarm for remaining runtime minus a second
    signal.alarm(int(context.get_remaining_time_in_millis() / 1000) - 1)

    if 'RequestType' in event:
        # Custom:: handler
        try:
            LOGGER.info('REQUEST RECEIVED:\n %s', event)
            LOGGER.info('REQUEST RECEIVED:\n %s', context)

            if event['ResourceType'].startswith("Custom::Azure"):
                azure_credentials = get_secret(os.environ['AZURE_SECRET_ID'])
                azure_resource_handler = AzureResourceHandler(azure_credentials)
                response_data = azure_resource_handler.process(event)
            elif event['ResourceType'].startswith("Custom::GoogleCloud"):
                google_cloud_credentials = get_secret(os.environ['GOOGLE_CLOUD_SECRET_ID'])
                google_cloud_resource_handler = GoogleCloudResourceHandler(google_cloud_credentials)
                response_data = google_cloud_resource_handler.process(event)

            if event['RequestType'] == 'Create':
                send_response(event, context, "SUCCESS", response_data)
            elif event['RequestType'] == 'Update':
                send_response(event, context, "SUCCESS", response_data)
            elif event['RequestType'] == 'Delete':
                send_response(event, context, "SUCCESS", response_data)
            else:
                LOGGER.warning('FAILED!')
                send_response(event, context, "FAILED",
                    {"Message": "Unexpected RequestType received from CloudFormation"})
        except: #pylint: disable=W0702
            LOGGER.warning('FAILED! %s %s', traceback.format_exc(), sys.exc_info()[0])
            send_response(event, context, "FAILED",
                {"Message": "Exception during processing"})
    elif 'fragment' in event:
        # Transform handler
        return handle_transform(event, context)
    else:
        LOGGER.warning('Unknown event')


def get_secret(secret_id):
    smclient = boto3.client('secretsmanager')
    credential = json.loads(smclient.get_secret_value(SecretId=secret_id)['SecretString'])

    return credential


def send_response(event, context, response_status, response_data):
    response_object = {
        "Status": response_status,
        "PhysicalResourceId": context.log_stream_name + event['LogicalResourceId'],
        "StackId": event['StackId'],
        "RequestId": event['RequestId'],
        "LogicalResourceId": event['LogicalResourceId'],
        "Data": response_data
    }

    if response_status != "SUCCESS":
        response_object['Reason'] = "See the details in CloudWatch Log Stream: " + context.log_stream_name
    
    response_body = json.dumps(response_object)

    LOGGER.info('ResponseURL: %s', event['ResponseURL'])
    LOGGER.info('ResponseBody: %s', response_body)

    requests.put(event['ResponseURL'], data=response_body)
    LOGGER.info('Response Sent')


def timeout_handler(_signal, _frame):
    raise Exception('Time exceeded')


def handle_transform(event, context):
    macro_response = {
        "requestId": event["requestId"],
        "status": "success"
    }

    try:
        params = {
            "params": event["templateParameterValues"],
            "template": event["fragment"],
            "account_id": event["accountId"],
            "region": event["region"]
        }
        response = event["fragment"]
        for k in list(response["Resources"].keys()):
            if response["Resources"][k]["Type"].startswith("Azure::") or response["Resources"][k]["Type"].startswith("GoogleCloud::"):
                if "Properties" not in response["Resources"][k]:
                    response["Resources"][k]["Properties"] = {}
                response["Resources"][k]["Type"] = "Custom::" + response["Resources"][k]["Type"].replace("::","_")
                response["Resources"][k]["Properties"]["ServiceToken"] = context.invoked_function_arn
        macro_response["fragment"] = response
    except:
        LOGGER.info('Failed to process template for transform')
        macro_response["status"] = "failure"
        macro_response["errorMessage"] = "failed to process"
    
    LOGGER.info('MacroResponse: %s', json.dumps(macro_response))

    return macro_response


signal.signal(signal.SIGALRM, timeout_handler)
