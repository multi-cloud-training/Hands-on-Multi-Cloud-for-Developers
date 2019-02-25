## Deploying and Migrating a Stateless App

### Adding or Removing Remote Nodes or Default Region Nodes

1. Deploy Marathon-lb on AWS

_Note:_ _We currently do not have a way to force marathon-lb on a remote site using the catalog. You can use this [remote-mlb.json](./remote-mlb.json) to serve as your example. You can modify the constraints to deploy it to a specific location._

2. Run `terraform output` and locate you AWS Public Agent ELB name. For example:
```bash
AWS Public Agent ELB Address = mbernadin-tfd132-pub-agt-elb-544778731.us-east-1.elb.amazonaws.com
```

3. Copy your ELB name and place it in your _dcos-website.json_ in the `HAPROXY_0_VHOST` value. 

```bash 
{
  "id": "dcos-website",
  "container": {
    "type": "DOCKER",
    "docker": {
      "image": "mesosphere/dcos-website:cff383e4f5a51bf04e2d0177c5023e7cebcab3cc",
      "network": "BRIDGE",
      "portMappings": [
        { "hostPort": 0, "containerPort": 80, "servicePort": 10004 }
      ]
    }
  },
  "instances": 3,
  "cpus": 0.25,
  "mem": 100,
  "healthChecks": [{
      "protocol": "HTTP",
      "path": "/",
      "portIndex": 0,
      "timeoutSeconds": 2,
      "gracePeriodSeconds": 15,
      "intervalSeconds": 3,
      "maxConsecutiveFailures": 2
  }],
  "labels":{
    "HAPROXY_DEPLOYMENT_GROUP":"dcos-website",
    "HAPROXY_DEPLOYMENT_ALT_PORT":"10005",
    "HAPROXY_GROUP":"external",
    "HAPROXY_0_REDIRECT_TO_HTTPS":"true",
    "HAPROXY_0_VHOST":"<INSERT_CLOUD_PUBLIC_AGENT_ELB_NAME_FROM_TERRAFORM_OUTPUT>"
  }
}
```

_protip:_ _you can specify multiple VHOST by using an example like this below in a comma delimited format_

```json
"HAPROXY_0_VHOST": "public-agent-mbernadin-tfbb33.ukwest.cloudapp.azure.com,mbernadin-tfbb33-pub-agt-elb-1287283532.us-east-1.elb.amazonaws.com"
```

4. Deploy the application using the json editor on DC/OS UI or using the DC/OS CLI. 
 
Because we haven't decided which region by default it will be automatically deployed on the local region which is AWS. 

5. Ensure you can reach your application from the web via the ELB name on your browser. This will ensure that your application is successfully running on AWS.

6. Go to the Services tab on the DC/OS website and edit the configuration and edit your dcos-website and go to the placement tab and change the default region from local to Azure. Apply changes and validate that the application gets redeployed to Azure.

Note that it may take a couple of minutes for the application to download the docker images and gets rescheduled to Azure. Also In some situations , especially if you are on a slow network, you may need to force refresh the browser CMD+R/CTRL+R/F5

7. Check that you can still see the application still running on the same AWS ELB address.

The application will be a little bit slower since it is responding from AWS ELB --> Marathon LB (on AWS) --> VPN over the internet --> Azure

### Navigation

1. [LAB1 - Deploying AWS Using Terraform](./lab-1-deploying-hybrid-cluster.md)
2. [LAB2 - Bursting from AWS to Azure](./lab-2-bursting-from-aws-to-azure.md)
3. LAB3 - Deploying and Migrating Stateless App from AWS to Azure (current)
4. [LAB4 - Deploying Cassandra Multi DataCenter](./lab-4-deploying-cassandra-multi-dc-cluster.md)

[Return to Main Page](../README.md)
