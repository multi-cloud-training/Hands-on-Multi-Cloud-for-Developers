# Overview


Kozinaki is an OpenStack hybrid/multi-cloud driver which aims to provide full lifecycle management of public cloud resource (AWS, Azure, GCP) using OpenStack. We beleive Kozinaki will enable true hybrid/multicloud OpenStack use case and provide the uniformity in using public clouds resources through versatile community developed OpenStack APIs and CLI. With Kozinaki you don't have to choose one provider - local or public, but you can pick based on on performance, price, and avilability while preserving time and money investments in skills and tools.

Kozinaki is completely pluggable into OpenStack without requiring any change to the code of core OpenStack components. The driver is modular, so adding new cloud providers is fast. Its architecture relies on provider developed SDKs to interface with APIs.

# Supported functionality

Supported function   | Nova command  | AWS | Azure             | GCP | Libcloud providers\* |
-------------------- | :-----------: | :-: | :---------------: | :-: | :----------------: |
Create               | boot          |  +  | +                 |  +  | +                  |
Delete               | delete        |  +  | +                 |  +  | +                  |
Stop                 | stop          |  +  | +                 |  +  | +                  |
Start                | start         |  +  | +                 |  +  | +                  |
Reboot               | reboot        |  +  | +                 |  +  | +                  |
Get instance details | show          |  +  | +                 |  +  | +                  |
List instances       | list          |  +  | +                 |  +  | +                  |
Create snapshot      | image-create  |  +  | not supported     |  +  | in progress        |
Resize               | resize        |  +  | not supported     |  +  | in progress        |
Add disk             | volume-attach |  +  | in progress       |  +  | in progress        |
Remove disk          | volume-detach |  +  | in progress       |  +  | in progress        |

\* [Supported Libcloud providers](https://libcloud.readthedocs.io/en/latest/supported_providers.html)

# Kozinaki support

We are glad to help with any issues that you have with Kozinaki via kozinaki@compu-nova.com and Skype @ compu-nova.

# Documentation

Please see https://github.com/compunova/kozinaki/wiki
