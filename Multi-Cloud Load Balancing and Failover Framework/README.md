#                           Cloud Hydra: A Multi-Cloud Load Balancing and Failover Framework

** **

## 1.   Vision and Goals Of The Project:

Hydra will be a framework for applications in the cloud to mitigate DDoS attacks and provider outages by providing resiliency at multiple levels both intra- and inter-clouds. Intra-cloud resiliency is a topic that has been studied and practiced in depth, making it possible for pieces of applications to migrate from one server to another. Intra-cloud reliability platforms, however, are not nearly as common. We seek to include this in order to protect applications from several issues ranging from cyber attacks to hardware failures. For example, if parts of AWS or GCP go down, for example, the application itself should be alive and kicking, as resources will be directed to the provider that is still up. We seek to implement load balancing at both the intra- and inter-cloud levels so that all requests are serviced, as well as duplicate data throughout different cloud providers in order to ensure that application users always have access to their current data. We will test our framework with our own application by running it on multiple cloud providers and testing its reliability when different cloud instances are turned off.

** **

## 2. Users/Personas Of The Project:

*Hydra targets:*

- Software Developers and Ops teams that want a resiliency framework for their applications running on Clouds

*Hydra does not target:*

- End users of above applications
- Cloud Administrators

** **

## 3.   Scope and Features Of The Project:

*Hydra will feature:*

- Request-level load balancing and queueing between hosts across clouds.
- If the compute layer of one cloud provider goes down, the application should still function.
- Distributed writes to all database solutions.
- Ability to read from any healthy database server.
- Eventual consistency of newly spawned databases via replication.
- If the database layer of one cloud provider goes down, the application should still function.

*Hydra will not feature:*

- More efficient use of compute resources, as many of the servers will be heartbeating and anycasting between each other to track uptime and distribute data.
- Full disaster recovery
- Database replication strategies
- Deduplication of data persisted in the DB

** **

## 4. Solution Concept

![alt text](https://raw.githubusercontent.com/bu-528-sp19/Multi-cloud-defensive-load-balancing/development/528Architecture.png)

Hydra will provide resiliency at multiple levels.

### Compute

Hydra will feature a request server layer in each cloud that will receive incoming requests and round robin load balance them to hosts per Cloud. The Request Server layers in each Cloud will be heartbeating between each other to ensure uptime and analyze load. In a two Request Server architecture, one will be marked as primary and will forward every other request to the secondary Request Server (e.g GCP forwards every other request to AWS). In the event that a secondary server goes down, the primary Request Server will remove it's eligibility in the round robin scheme until it can re-establish contact. If the primary Request Server goes down, the secondary servers will be notified via unresponsive heartbeat. They will elect a new primary Request Server via a simple leadership election algorithm any-cast. The new primary Request Server will then change DNS records to point to domain to itself. In the event that a priority is provided to the framework to prefer a certain Cloud over another (one may be cheaper, etc), the leadership election algorithm will introduce bias when generating the random IDs.

### Data

To use multiple database services across many clouds, Hydra will also feature a distributed database access layer that will ensure consistency across all DBs. Hydra's aim is to ensure consistency and availability (and not necessarily partitioning), so writes will be distributed across all DBs. There will be a database access server (DAS) in all clouds that sits in between the webserver and the database service. Requests to write will pass through the DAS which connects to the DB service and execute the write. All DASs will be connected in this architecture, so write requests that a single DAS receives will be forwarded to every other DAS in the system, so that one write in one system fans out to a write in every system. Similarly, reads will be sourced from any system that is up, optimized for spacial locality. For example, if the AWS side of the system receives a request to read from the DB, and AWS RDS is up, it will simply read from RDS. If RDS is down, however, the AWS DAS will request a read from the GCP DAS and information will be retrieved from CloudSQL and forwarded to AWS. Any writes during this time will be queued by the AWS DAS for pushing when RDS comes back up.

Design Implications and Discussion:

- Request and Database Access Servers usually don't communicate horizontally, but Hydra necessitates at least one server per cloud in the event that an entire provider goes offline.

- Request and Database Access Servers are constantly in contact with each other to distribute data across clouds and to make decisions based on the status of their peers. This is not more efficient than a single datacenter system, as there will be a compute overhead with constantly checking status and forwarding data. This does, however, lead to great resiliency and fault-tolerance at each layer of each cloud in the system.

- DB writes will be distributed to all Database Access Servers, meaning that each database service should have a full copy of all the data. Per the CAP theorem, Hydra prioritizes consistency and availability (naturally, as a resiliency system) over more efficient data storage and retrieval strategies afforded by data partitioning.

- Since Hydra is meant to work on a multi cloud platform, CI/CD for each cloud will need to differ slightly in accordance with each respective API and access structure.

** **

## 5. Acceptance criteria

MVP (not in any particular order):

1) Basic CRUD web app that, by nature, will exercise DB reads, writes, and possible concurrency issues as a base to apply resiliency strategies to
2) Request load balancing between clouds
3) Host level load balancing within clouds
4) Turning off host instances should not crash the application for an extended period of time
5) DNS failover in the event that the main server goes down
6) Turning off all compute instances of a Cloud should not kill the application
7) 1 Request server per Cloud
8) Two clouds / at least two isolated systems

Stretch (also not in any particular order):

1) Multiple Request Servers and leadership election
2) Distributed DB writes
3) Distributed reads from any healthy database
4) Eventual consistency of new and recovered databases via replication and write queuing
5) Turning off database instances should not break the application
6) Multiple Request servers per Cloud
7) More than two clouds

** **

## 6.  Release Planning:

Sprint 1:
* Create a sample garage reservation system that mirrors a real world application
  * Get a basic web server running on GCP that can connect to CloudSQL
  * Get a basic web server running on AWS that can connect to RDS
  * Ensure the web app can serve static content
  * Ensure the web app can serve dynamic content and have will CRUD functionality as described by the mentor (garage reservation system with users and cars)

Product Backlog:
* Separate the front end to be served by a CDN
* Extract the request handling to Request Servers that will load balance requests to the webserver(s) and track their statuses (per cloud)
* Create a DNS name for the primary request server and route CDN traffic to it
* Connect the Request Servers together and implement cross-cloud load balancing and health checks
* Implement DNS failover on the secondary Request Server to redirect DNS servers to it instead
* Extract the database access layer into a separate Database Access Server
* Connect Database Access Servers together and implement distributed reads and writes
