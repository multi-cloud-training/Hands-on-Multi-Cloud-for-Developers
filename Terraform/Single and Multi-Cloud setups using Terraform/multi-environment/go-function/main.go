// main.go
package main

import (
	"io/ioutil"
	"log"
	"fmt"
	"github.com/aws/aws-lambda-go/lambda"
	"cloud.google.com/go/pubsub"

	"golang.org/x/net/context"
	"golang.org/x/oauth2/google"
	"google.golang.org/api/option"
)

func hello() (payload string, err error) {

	fmt.Println("Incoming Payload::", payload)

	jsonKey, err := ioutil.ReadFile("terraform-admin.json")
	if err != nil {
		log.Fatal(err)    
	}

	conf, err := google.JWTConfigFromJSON(
			jsonKey,
			pubsub.ScopeCloudPlatform,
			pubsub.ScopePubSub,
	)
	if err != nil {
			log.Fatal(err)
	}
	
	ctx := context.Background()
	ts := conf.TokenSource(ctx)
	
	client, err := pubsub.NewClient(ctx, "terraform-demo-project", option.WithTokenSource(ts))
	if err != nil {
		log.Fatal(err)
	}
		
	var topic *pubsub.Topic
	topic = client.Topic("demo-topic")

	msg := &pubsub.Message{
		// TODO pass payload here
		Data: []byte("blahblah"),
	}

	fmt.Println("pubsub payload", msg)

	if _, err := topic.Publish(ctx, msg).Get(ctx); err != nil {
		log.Fatal(err)
		return "error", nil
	}

	fmt.Print("Message published.")

	return "Your message has been published!", nil
}

func main() {
	// Make the handler available for Remote Procedure Call by AWS Lambda
	lambda.Start(hello)
}
