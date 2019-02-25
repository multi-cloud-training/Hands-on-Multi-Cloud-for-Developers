# s3-multicloud-backup

A serverless application that replicate all objects in S3 bucket to Google Cloud Storage.

## Archtecture

TBD

## Run locally as testing

It requires following components on AWS,

- S3 bucket: s3-multicloud-backup-staging
  - Put a file to the bucket with `banners/1000/1000.jpg` key.
- GCS bucket: s3-multicloud-backup-staging

You should build native dependencies with lambci/lambda:build-nodejs6.10 image and then you can invoke the function with [aws-sam-local](https://github.com/awslabs/aws-sam-local) command.

```
$ docker run --rm -v "$PWD/src/":/var/task lambci/lambda:build-nodejs6.10 npm install
$ cd ../
# You need to set AWS credentials before execute following command
$ aws-sam-local local invoke S3MulticloudBackupStagingFunction -e sample-event-s3-via-sns.json --template=deploy/template/staging.yml
$ aws-sam-local local invoke S3MulticloudBackupStagingFunction -e sample-event-dlq.json --template=deploy/template/staging.yml
```
