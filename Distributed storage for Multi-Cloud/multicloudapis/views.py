# Create your views here.

import hashlib
import json
import os

import boto3
from azure.storage.blob import BlockBlobService, PublicAccess
from boto.s3.connection import S3Connection
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods
from gcloud import storage
from oauth2client.service_account import ServiceAccountCredentials
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny

from . import models

"""
The universal-upload api takes in the location of the
file to be uploaded to the cloud and hosts the file
onto multiple cloud-providers. The file is initially
divided into small chunks of data which is uploaded
sequentially onto different providers. To ensure
security of data we save the XOR of files of every two
chunks of data onto a provider.

"""


@require_http_methods(["POST"])
@api_view(['POST'])
@permission_classes([AllowAny])
@csrf_exempt
def universal_uploadfile(request):
    if request.method == 'POST':

        pram = models.CloudFileSystem.objects.filter(name='ram')
        pram.delete()

        # global aws_file_counter
        # global settings.AZURE_FILE_COUNTER
        # global settings.GCP_FILE_COUNTER

        county = models.CloudFileSystem()
        county.name = 'ram'
        county.file_location = request.data['file_location']
        azure_count = 0
        gcp_count = 0

        chunk_size = 2097152
        counter = 0

        handle = request.data['file_location']
        dat = []
        size = os.path.getsize(handle)
        num = int(size / chunk_size)
        file = open(handle, 'rb')
        filename = str(counter)

        flag = 0
        for piece in range(num - 1):
            # dat.append(file.read())
            if counter % 2 == 0:

                data_gcp = file.read(chunk_size)
                upload_gcp_func(data_gcp, str(counter))
                gcp_count = gcp_count + 1
                flag = flag + chunk_size
            else:
                data_azure = file.read(chunk_size)
                upload_azure_func(data_azure, str(counter))
                azure_count = azure_count + 1
                flag = flag + chunk_size
                l = bytes(a ^ b for a, b in zip(data_gcp, data_azure))
                upload_aws_func(l, str(counter - 1) + "_" + str(counter))
            counter = counter + 1
            # file.close()

        rem_chunk_size = (size - flag)

        if num % 2 != 0:

            data_gcp = file.read(int(rem_chunk_size / 2))
            upload_gcp_func(data_gcp, str(counter))
            gcp_count = gcp_count + 1
            counter = counter + 1
            data_azure = file.read(int(rem_chunk_size / 2))
            upload_azure_func(data_azure, str(counter))
            azure_count = azure_count + 1

            l = bytes(a ^ b for a, b in zip(data_gcp, data_azure))
            upload_aws_func(l, str(counter - 1) + "_" + str(counter))

        else:

            if counter % 2 == 0:
                data_gcp = file.read(chunk_size)
                upload_gcp_func(data_gcp, str(counter))
                gcp_count = gcp_count + 1
            else:
                data_gcp = file.read(chunk_size)
                upload_gcp_func(data_gcp, str(counter))
                azure_count = azure_count + 1

                l = bytes(a ^ b for a, b in zip(data_gcp, data_azure))
                upload_aws_func(l, str(counter - 1) + "_" + str(counter))

        county.azure_count = azure_count
        county.gcp_count = gcp_count
        county.save()

        return HttpResponse(
            json.dumps(
                {
                    'message': 'Successfully Uploaded file to the cloud providers.'
                }
            )
        )


"""
The Universal Download API downloads all the chunks of data from different servers and integrates into a singlke data file
to trace back the original data file.
"""


@require_http_methods(["POST"])
@api_view(['POST'])
@permission_classes([AllowAny])
@csrf_exempt
def universal_download(request):
    if request.method == 'POST':

        final_file_path = request.data['file_location']

        file2 = open(final_file_path, 'wb')
        # file2.write(d)

        job_name = models.CloudFileSystem.objects.get(name='ram')

        total_file_count = job_name.gcp_count + job_name.azure_count

        # total_file_count = 6

        # print(total_file_count)

        for i in range(total_file_count):

            if i % 2 == 0:
                try:
                    gcp_blob = download_blob_gcp(str(i))
                    file2.write(gcp_blob)
                except:
                    gcp_blob = None

            else:
                try:
                    azure_blob = azure_downloadtxt(str(i))
                except:
                    azure_blob = None
                    if gcp_blob is None:
                        print("Data is corrupted!!")
                        return HttpResponse(
                            json.dumps(
                                {
                                    'message': 'The data has been corrupted.',

                                }
                            )
                        )
                    else:
                        aws_blob = aws_downloadtxt(str(i - 1) + "_" + str(i))
                        file1_b = bytearray(gcp_blob)
                        file2_b = bytearray(aws_blob)

                        # Set the length to be the smaller one
                        size = len(file1_b) if len(file1_b) < len(file2_b) else len(file2_b)
                        azure_blob = bytearray(size)

                        # XOR between the files
                        for ii in range(size):
                            azure_blob[ii] = file1_b[ii] ^ file2_b[ii]

                if gcp_blob is None:
                    aws_blob = aws_downloadtxt(str(i - 1) + "_" + str(i))
                    file1_b = bytearray(azure_blob)
                    file2_b = bytearray(aws_blob)

                    # Set the length to be the smaller one
                    size = len(file1_b) if len(file1_b) < len(file2_b) else len(file2_b)
                    gcp_blob = bytearray(size)

                    # XOR between the files
                    for ii in range(size):
                        gcp_blob[ii] = file1_b[ii] ^ file2_b[ii]
                    file2.write(gcp_blob)
                file2.write(azure_blob)

        file2.close()

        if md5(final_file_path) == md5(job_name.file_location):
            checksum = "Checksum successful."

        # upload_gcp_func(file.read(), 'final')

        return HttpResponse(
            json.dumps(
                {
                    'message': 'Successfully Uploaded file to the AWS Platform',
                    'checksum': checksum,
                    'hash_value_of_the_uploaded_file': md5(job_name.file_location),
                    'hash_value_of_the_final_generated_file': md5(final_file_path)
                }
            )
        )


def md5(fname):
    hash_md5 = hashlib.md5()
    with open(fname, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)

    print(hash_md5.hexdigest())
    return hash_md5.hexdigest()


@require_http_methods(["POST"])
@api_view(['POST'])
@permission_classes([AllowAny])
@csrf_exempt
def uploadfile_gcp(request):
    if request.method == 'POST':
        credentials_dict = {
            "type": "service_account",
            "project_id": "hackator",
            "private_key_id": "<enter your private key id>",
            "private_key": "<enter your private key>",
            "client_email": "koushikhack44@hackator.iam.gserviceaccount.com",
            "client_id": "<enter client id>>",
            "auth_uri": "https://accounts.google.com/o/oauth2/auth",
            "token_uri": "https://oauth2.googleapis.com/token",
            "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
            "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/koushikhack44%40hackator.iam.gserviceaccount.com"
        }
        credentials = ServiceAccountCredentials.from_json_keyfile_dict(
            credentials_dict
        )

        file_location = request.data['file_location']
        client = storage.Client(credentials=credentials, project='hackator')
        bucket = client.get_bucket('kbuckethack')
        blob = bucket.blob('get-pip.py')
        blob.upload_from_filename(file_location)

        return HttpResponse(
            json.dumps(
                {
                    'message': 'Successfully Uploaded file to the Google CLoud Platform'
                }
            )
        )


def upload_gcp_func(file, filename):
    credentials_dict = {
        "type": "service_account",
        "project_id": "hackator",
        "private_key_id": "<enter your private key id>",
        "private_key": "<enter private key>",
        "client_email": "koushikhack44@hackator.iam.gserviceaccount.com",
        "client_id": "<enter client id>",
        "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        "token_uri": "https://oauth2.googleapis.com/token",
        "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/koushikhack44%40hackator.iam.gserviceaccount.com"
    }
    credentials = ServiceAccountCredentials.from_json_keyfile_dict(
        credentials_dict
    )

    client = storage.Client(credentials=credentials, project='hackator')
    bucket = client.get_bucket('kbuckethack')
    blob = bucket.blob(filename)
    blob.upload_from_string(file)


def upload_azure_func(file, filename):
    block_blob_service = BlockBlobService(account_name='<enter account name>',
                                          account_key='<enter account key>')

    # Create a container called 'quickstartblobs'.
    container_name = 'quickstartblobs'
    block_blob_service.create_container(container_name)

    # Set the permission so the blobs are public.
    block_blob_service.set_container_acl(container_name, public_access=PublicAccess.Container)

    full_path_to_file = file

    # print(request.data['file_location'])

    # print("Temp file = " + full_path_to_file)
    # print("\nUploading to Blob storage as blob" + full_path_to_file)

    # Upload the created file, use local_file_name for the blob name
    block_blob_service.create_blob_from_text(container_name, filename, file)


def upload_aws_func(data, filename):
    AWS_ID = "<enter your aws id>"
    AWS_KEY = "<enter your aws key>"
    conn = S3Connection(aws_access_key_id=AWS_ID, aws_secret_access_key=AWS_KEY)

    # s3 = boto3.client('s3',
    #                   aws_access_key_id=AWS_ID,
    #                   aws_secret_access_key=AWS_KEY)
    s3 = boto3.client('s3')

    mypath = "aws file"
    # filename = file_path
    print(filename)
    bucket_name = 'my-bucket-hackathon'
    # bucket = conn.get_bucket(bucket_name)
    s3.put_object(Body=data, Bucket='my-bucket-hackathon', Key=filename)
    # s3.upload_file(file, bucket_name, filename)


def split(handle, chunk_size):
    dat = []
    size = os.path.getsize(handle)
    num = size / chunk_size
    if size % chunk_size != 0:
        num += 1
    file = open(handle, 'rb')
    for piece in range(num - 1):
        dat.append(file.read(chunk_size))
    dat.append(file.read())
    file.close()

    return dat


@require_http_methods(["POST"])
@api_view(['POST'])
@permission_classes([AllowAny])
@csrf_exempt
def uploadfile_chunk_gcp(request):
    if request.method == 'POST':

        chunk_size = 3145728
        counter = 0

        handle = request.data['file_location']
        dat = []
        size = os.path.getsize(handle)
        num = int(size / chunk_size)
        if size % chunk_size != 0:
            num += 1
        file = open(handle, 'rb')
        filename = str(counter)
        # print('size ' + str(size))
        # print(num)

        for piece in range(num - 1):
            # dat.append(file.read())
            upload_gcp_func(file.read(chunk_size).decode('utf-8'), str(counter))
            counter = counter + 1
            # file.close()
        upload_gcp_func(file.read(chunk_size).decode('utf-8'), str(counter))
        return HttpResponse(
            json.dumps(
                {
                    'message': 'Successfully Uploaded file to the AWS Platform'
                }
            )
        )


def joinFile(handle, jobId):
    dat = []
    file2 = open("<enter file location>", 'wb')
    for d in dat:
        file2.write(d)
    file2.close()


@require_http_methods(["POST"])
@api_view(['POST'])
@permission_classes([AllowAny])
@csrf_exempt
def uploadfile_azure(request):
    if request.method == 'POST':
        # Create the BlockBlockService that is used to call the Blob service for the storage account
        block_blob_service = BlockBlobService(account_name='<enter account name>',
                                              account_key='<enter account key>')

        # Create a container called 'quickstartblobs'.
        container_name = 'quickstartblobs'
        block_blob_service.create_container(container_name)

        # Set the permission so the blobs are public.
        block_blob_service.set_container_acl(container_name, public_access=PublicAccess.Container)

        full_path_to_file = request.data['file_location']

        print(request.data['file_location'])

        # Write text to the file.
        file = open(full_path_to_file, 'w')
        # file.write("Hello, World!")
        file.close()

        print("Temp file = " + full_path_to_file)
        print("\nUploading to Blob storage as blob" + full_path_to_file)

        # Upload the created file, use local_file_name for the blob name
        block_blob_service.create_blob_from_path(container_name, 'ram', full_path_to_file)

        return HttpResponse(
            json.dumps(
                {
                    'message': 'Successfully Uploaded file to the Google CLoud Platform'
                }
            )
        )


def upload(myfile):
    # bucket = conn.get_bucket(bucket_name)
    s3 = boto3.client('s3')
    bucket_name = 'my-bucket-hackathon'
    s3.upload_file(myfile, bucket_name, myfile)
    return myfile


# print(file)


# bucket_name = 'my-bucket-hackathon'


@require_http_methods(["POST"])
@api_view(['POST'])
@permission_classes([AllowAny])
@csrf_exempt
def uploadfile_aws(request):
    if request.method == 'POST':
        AWS_ID = "<enter aws id>"
        AWS_KEY = "<enter aws key>"
        conn = S3Connection(aws_access_key_id=AWS_ID, aws_secret_access_key=AWS_KEY)

        file_path = request.data['file_location']
        # s3 = boto3.client('s3',
        #                   aws_access_key_id=AWS_ID,
        #                   aws_secret_access_key=AWS_KEY)
        s3 = boto3.client('s3')

        mypath = "aws file"
        filename = file_path
        print(filename)
        bucket_name = 'my-bucket-hackathon'
        # bucket = conn.get_bucket(bucket_name)

        s3.upload_file(filename, bucket_name, 'ram')

        return HttpResponse(
            json.dumps(
                {
                    'message': 'Successfully Uploaded file to the AWS Platform'
                }
            )
        )


def aws_downloadtxt(key):
    client = boto3.client('s3')
    s3_response_object = client.get_object(Bucket='my-bucket-hackathon', Key=key)
    object_content = s3_response_object['Body'].read()
    return object_content


def uploadfile_azure2():
    #    if request.method == 'POST':
    # Create the BlockBlockService that is used to call the Blob service for the storage account
    try:
        block_blob_service = BlockBlobService(account_name='<enter account name>',
                                              account_key='<enter account key>')

        # Create a container called 'quickstartblobs'.
        container_name = 'quickstartblobs'
        block_blob_service.create_container(container_name)

        # Set the permission so the blobs are public.
        block_blob_service.set_container_acl(container_name, public_access=PublicAccess.Container)

        #   full_path_to_file =

        local_path = os.path.expanduser("~/Downloads")
        local_file_name = '10mb.txt'
        full_path_to_file = os.path.join(local_path, local_file_name)

        local_path1 = os.path.expanduser("~/Downloads")
        local_file_name1 = '10MBfile'
        full_path_to_file1 = os.path.join(local_path1, local_file_name1)

        local_path2 = os.path.expanduser("~/Downloads")
        local_file_name2 = 'xorfiledirect'
        full_path_to_file2 = os.path.join(local_path2, local_file_name2)

        # print(request.data['file_location'])

        # Write text to the file.
        # file = open(full_path_to_file, 'w')
        # file.write("Hello, World!")
        #  file.close()

        #  print("Temp file = " + full_path_to_file)
        print("\nUploading to Blob storage as blob" + full_path_to_file)

        # Read two files as byte arrays
        file1_b = bytearray(open(full_path_to_file, 'rb').read())
        file2_b = bytearray(open(full_path_to_file1, 'rb').read())

        # file1_b = bytearray(open(full_path_to_file, 'rb').read()
        # file2_b = open(full_path_to_file1, 'rb').read()

        # Set the length to be the smaller one
        size = len(file1_b) if len(file1_b) < len(file2_b) else len(file2_b)
        xord_byte_array = bytearray(size)

        # XOR between the files
        for i in range(size):
            xord_byte_array[i] = file1_b[i] ^ file2_b[i]

        # Write the XORd bytes to the output file
        open(full_path_to_file2, 'wb').write(xord_byte_array)

        print("file is being written XORED")
        #	print "[*] %s XOR %s\n[*] Saved to \033[1;33m%s\033[1;m."%(sys.argv[1], sys.argv[2], sys.argv[3])

        # Upload the created file, use local_file_name for the blob name
        block_blob_service.create_blob_from_path(container_name, local_file_name2, full_path_to_file2)

        print("file successfully uploaded")
        '''return HttpResponse(
            json.dumps(
                {
                    'message': 'Successfully Uploaded file to the Google CLoud Platform'
                }
            )
        )'''

    except Exception as e:
        print(e)


def download_blob_gcp(source_blob_name):
    """Downloads a blob from the bucket."""
    bucket_name = 'kbuckethack'
    credentials_dict = {
        "type": "service_account",
        "project_id": "hackator",
        "private_key_id": "<enter private key id>",
        "private_key": "<enter private key>",
        "client_email": "koushikhack44@hackator.iam.gserviceaccount.com",
        "client_id": "<enter client id>>",
        "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        "token_uri": "https://oauth2.googleapis.com/token",
        "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/koushikhack44%40hackator.iam.gserviceaccount.com"
    }
    credentials = ServiceAccountCredentials.from_json_keyfile_dict(
        credentials_dict
    )

    client = storage.Client(credentials=credentials, project='hackator')

    bucket = client.get_bucket(bucket_name)
    blob = bucket.blob(source_blob_name)

    # blob.download_to_filename(destination_file_name)

    return blob.download_as_string()


def azure_downloadtxt(myblockblob):
    block_blob_service = BlockBlobService(account_name='<enter account name>',
                                          account_key='<enter account key>')

    blob = block_blob_service.get_blob_to_bytes('quickstartblobs', myblockblob)
    # print(blob.content)
    # print("content downloaded !! ")

    return blob.content
