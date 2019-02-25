# Multi-cloud-defensive-load-balancing

## Overview

### Background: 
Reliability is an important problem across platforms, be they local or cloud-based: ultimately a user’s experience is directly linked to how well a service interacts with them, which in a large part is linked to the system’s overall reliability. Though the cloud generally provides a strong toolset for combatting that problem (often providing over 99.9% reliability out of the box), it is imperative that a service be prepared for those instances wherein the cloud fails (be it via soft/hard failures or via cloud-direct/service-direct attacks). If we consider the reliability rates of 99.9 %-99.999%, we can assume that every year failures will span between 5 minutes and nearly 9 hours; whether these are contiguous or not, the effects may range from poor experience for users to jeopardy of the overall system/company, neither of which are desirable.
 
### Project Specifics: 
Though some monetized solutions exist, there are few good, embedded multi-cloud solutions since single-cloud providers generally abstain from trying due to the obvious conflict of interest. We, as such, suggest a service which would defend from both failures and DDoS attacks by leveraging multiple cloud providers to switch traffic and service as required based on both load and failures. This lightweight service would manage cloud mirroring, deployment, and load balancing to ensure failures of any kind and target would be as ineffective as possible in taking a service down (note that not everything should need to be mirrored, only essential services).

## Some Technologies you will learn/use:
* agile methods
* Designing for resilience
* Using Cloud and Open Source load-balancing tech
* Testing complex distributed systems



## Example applications:
We provide three examples of applications that might use this service at different scales. 

### Large-Scale Example
ZocDoc provides appointments and ratings for doctors around the United States, while maintaining your medical information for user convenience. Consider the following possible architecture (NOTE: Architecture provided here is a guess and may not match exactly to what ZocDoc incorporates):
 
(AWS) Front-end website is static and exists in a thin server layer (e.g.: EC2), distributed by CDNs (e.g.: CloudFront). Back-end portion consists of a REST endpoint (e.g.: EC2 or Lambda) backed by several large-scale databases (e.g.: Dynamo for key-value store, S3 for photos, RDS for relational things), and a simple map-reduce layer (e.g.: Scheduled EMR to help distance calculations).
 
This model provides numerous points of failure, many of which could completely shut down the website, some of which may just significantly affect user experience: CDN and EC2 failures disable front-end page distribution (phone apps and pre-loaded, cached pages might still function, but the main pages should be down). Lambda failures would shut the back-end quite significantly, where, though a static page may be loaded, any request will fail (this includes apps and cached pages). EC2 could also affect EMR, but that is not an immediate concern (though I might make it so users only see old pages if it lasts many hours/days). Dynamo failing would also shut down some capabilities, and likewise RDS; meanwhile S3 will just make it so you don’t see doctor’s pages.
 
At this point, a cautious lead developer for ZocDoc will evaluate which services are critical, and which are simply important to be up. In order to be frugal (an important concept for all companies), she will select greater protections for the critical services (as each protection may end up being expensive) and smaller, or no protection for the important ones. Here she might create a round-robin aname, which maps to different CDNs or instances, or even functions from each cloud provider; alternatively, she may make an automated service which healthchecks and modifies the aname accordingly whenever a host or CND is down or unresponsive. Databases writes could also round robin through a thin layer which then sends writes to databases in each provider (reads can come from any which is active). Also, services need to be placed across regions and zones in each cloud provider for further resilience. Through this, the developer deems that the service is protected.
 
### Middle-Scale Example
Halite is an online programming game played by many thousands of people, continuously running arbitrary code; it runs thousands of games per hour, each requiring a minimum number of resources (cored, memory, etc). Consider the following architecture:
 
(GCP) Front-end website is static and is served via a set of auto-scaled compute hosts fronted by a CDN. Back-end consists of a set of auto-scaled hosts for REST and general game coordination (Managed Instance Groups via Compute)(externally accessible) and a greater auto-scaled hosts for running the games (Managed Instance Groups via Compute) (only accessible internally). The backend consists of Cloud SQL hosts running PostgreSQL (only accessible internally). GCS stores user code and game engine code to be deployed to hosts which play the game (also only accessible internally).
 
In here direct attacks are not as easy to specific components, since many are not externally reachable. However, attacks to Google directly would still affect us. Further, attacks on the CDN or compute would still bring the static website down (though games would still be playable). Direct attacks on compute, S3 or CloudSQL would effectively disable the game, however. Once could effectively DDoS by submitting many thousands of bots through fake accounts, but that is expected to be hard, considering a very large number of fake accounts is required, and we can assume it is a very impractical attack.
 
The protections from this method are very similar to that above, with an extra layer of hosts existing in the other cloud and being balanced by the coordinator. Perhaps a VPN would also be connected between cloud providers in this case to ensure communication is still “internal” only. Unhealthy hosts will generally also need to be replaced by some health check (Google takes care of this with the Managed Instance Groups), and scaled up and down accordingly with traffic.
 
### Small-Scale Example
You have a small valet garage for which you desire to have a webpage to take reservations and allow users to rent out spots or set times to take your car out. Consider the following possible architecture:
 
(GCP) Front-end static website, backed by a simple Flask REST app in a small number of auto-scaled hosts in a managed instance group. Back-end database is a simple PostgreSQL database. Twillio sends scheduled SMS messages to drivers to know when to bring cars up front. We can assume payments are handled by another provider and are not time sensitive (that is to say, we need to get paid, but a 9 hour delay is acceptable), so we don’t consider them here.
 
Overall this model is also very similar to above, with the addition that we probably want a secondary SMS service in case Twillio goes down: that’s a simple endpoint change in case of failures.
