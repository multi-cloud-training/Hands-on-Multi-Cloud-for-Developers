const PubSub = require('@google-cloud/pubsub');

exports.handler = function (event, context, callback) {
  console.log('Event: ', JSON.stringify(event, null, '\t'));
  console.log('Context: ', JSON.stringify(context, null, '\t'));

  const projectId = 'terraform-demo-project';

  const pubsubClient = new PubSub({
    projectId: projectId,
    keyFilename: './terraform-admin.json'
  });

  const topicName = 'demo-topic';

  // Creates the new topic
  // pubsubClient
  //   .createTopic(topicName)
  //   .then(results => {
  //     const topic = results[0];
  //     console.log(`Topic ${topic.name} created.`);
  //   })
  //   .catch(err => {
  //     console.error('ERROR:', err);
  //   });

  const dataBuffer = Buffer.from(event);
  
  pubsubClient
    .topic(topicName)
    .publisher()
    .publish(dataBuffer)
    .then(results => {
      const messageId = results[0];
      console.log(`Message ${messageId} published.`);
    })
    .catch(err => {
      console.error('ERROR:', err);
    });

  callback(null);
};
