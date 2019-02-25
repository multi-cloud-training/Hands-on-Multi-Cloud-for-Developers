'use strict';

const fs = require('fs');
const uuid = require('uuid');
const aws = require('aws-sdk');
const kms = new aws.KMS();
const s3 = new aws.S3();

const GCS_BUCKET = process.env.GCS_BUCKET;
const PROJECT_ID = process.env.PROJECT_ID;
const ENCRYPTED_GC_CREDENTIAL = process.env.GC_CREDENTIAL;
const SKIP_KEY_REGEX = process.env.SKIP_KEY_REGEX;
const RETRY_SNS_TOPIC_ARN = process.env.RETRY_SNS_TOPIC_ARN;
const GC_CREDENTIAL_FILE_PATH = '/tmp/gcp_cred.json';
const TMP_FILE_PATH = `/tmp/${uuid.v4()}`;

function replicateObject(s3ObjectKey, s3Bucket) {
  const kmsParamas = {
    CiphertextBlob: new Buffer(ENCRYPTED_GC_CREDENTIAL, 'base64'),
  };

  const s3Params = {
    Bucket: s3Bucket,
    Key: s3ObjectKey,
  };

  return Promise.all([
    kms.decrypt(kmsParamas).promise().then((data) => {
      const decrypted = data.Plaintext.toString('ascii');
      fs.writeFileSync(GC_CREDENTIAL_FILE_PATH, decrypted);
    }),
    s3.getObject(s3Params).promise(),
  ]).then((results) => {
    const s3Object = results[1];
    fs.writeFileSync(TMP_FILE_PATH, s3Object.Body);

    const storage = require('@google-cloud/storage')({
      projectId: PROJECT_ID,
      keyFilename: GC_CREDENTIAL_FILE_PATH,
    });

    const gcsBucket = storage.bucket(GCS_BUCKET);
    const gcsParams = {
      destination: s3ObjectKey,
      validation: 'crc32c',
      resumable: false,
    };

    return gcsBucket.upload(TMP_FILE_PATH, gcsParams);
  });
}

function deleteTempFile() {
  if (fs.existsSync(TMP_FILE_PATH)) {
    fs.unlinkSync(TMP_FILE_PATH);
  }
}

exports.handler = (event, context) => {
  const message = (event.Records[0].Sns.TopicArn == RETRY_SNS_TOPIC_ARN)
    ? JSON.parse(JSON.parse(event.Records[0].Sns.Message).Records[0].Sns.Message)
    : JSON.parse(event.Records[0].Sns.Message);

  const s3Bucket = message.Records[0].s3.bucket.name;
  const s3ObjectKey = message.Records[0].s3.object.key;

  if (!(SKIP_KEY_REGEX == null) && s3ObjectKey.match(SKIP_KEY_REGEX)) {
    const logMessage = `skipped: Bucket: ${s3Bucket}, Key: ${s3ObjectKey}`;
    console.log(logMessage);
    context.succeed(logMessage);
  } else {
    replicateObject(s3ObjectKey, s3Bucket).then((success) => {
      deleteTempFile();
      const logMessage = `replicated: Bucket: ${s3Bucket}, Key: ${s3ObjectKey}`;
      console.log(logMessage);
      context.succeed(logMessage);
    }).catch((err) => {
      deleteTempFile();
      const logMessage = `ERROR!: Bucket: ${s3Bucket}, Key: ${s3ObjectKey}`;
      console.log(logMessage);
      console.log(err);
      context.fail(logMessage);
    });
  }
};
