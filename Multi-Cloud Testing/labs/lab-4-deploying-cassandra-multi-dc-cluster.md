# Cassandra Multi DataCenter 
1. Launch a Cassandra cluster with the default configuration (this will run in the local region).
2. Launch a second Cassandra cluster, with the following changes to the configuration in the UI: under the "Service" tab, change the "Name" field to "cassandra2", the "Data Center" field to "datacenter2", and the "Region" field to "<REMOTE_REGION>".
3. After the first service completes deployment, visit 
<<Public ELB of the master>>/service/v1/cassandra/seeds

For Example

https://amrabdelra-tf8ee7-pub-mas-elb-883657557.us-east-1.elb.amazonaws.com/service/v1/cassandra/seeds 

and record the list of addresses provided in that JSON blob. (Most likely "node-0-server.cassandra.autoip.dcos.thisdcos.directory" and "node-1-server.cassandra.autoip.dcos.thisdcos.directory").


4. After the second service completes deployment, perform Step 3 again but with the seed addresses obtained from $CLUSTER_URL/service/cassandra/v1/seeds instead.


5. Update the configuration for the "cassandra2" service, changing the "Remote Seeds" field to contain the two addresses noted earlier, separated by a comma (e.g. "node-0-server.cassandra.autoip.dcos.thisdcos.directory,node-1-server.cassandra.autoip.dcos.thisdcos.directory"). Wait for deployment to complete.


6. Perform step 5 again, but by updating the first cluster's config to reflect the seed addresses for the second cluster's config.

7. Create a keyspace that is replicated across all local and remote nodes by task-execing into a node with the following:

```bash
$ dcos node ssh --leader --master-proxy
```

```bash
docker run -it cassandra:3.0.16 bash
```

```bash
cqlsh node-0-server.cassandra.autoip.dcos.thisdcos.directory
```

```bash
> CREATE KEYSPACE IF NOT EXISTS mesosphere WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3, 'datacenter2': 3 };
> CREATE TABLE mesosphere.test ( id int PRIMARY KEY, message text );
> INSERT INTO mesosphere.test (id, message) VALUES (1, 'hello world!');
```

```bash
cqlsh cqlsh node-0-server.cassandra2.autoip.dcos.thisdcos.directory
DESC mesosphere;
SELECT * FROM mesosphere.test;
```

And you'll see the keyspace replicated to this remote node as well as its data.

### Navigation

1. [LAB1 - Deploying AWS Using Terraform](./lab-1-deploying-hybrid-cluster.md)
2. [LAB2 - Bursting from AWS to Azure](./lab-2-bursting-from-aws-to-azure.md)
3. [LAB3 - Deploying and Migrating Stateless App from AWS to Azure](./lab-3-deploying-and-migrating-stateless-app.md)
4. LAB4 - Deploying Cassandra Multi DataCenter (current)

[Return to Main Page](../README.md)
