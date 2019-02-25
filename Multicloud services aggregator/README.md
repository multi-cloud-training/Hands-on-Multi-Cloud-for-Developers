# Cloud services aggregator - Multicloud

In today’s competitive cloud computing environment, using a single cloud provider results in vendor dependency and risk of single point of failure. Multi-Cloud is an unified cloud service provider which aggregates the different cloud providers for building a cost effective yet highly scalable, reliable and available cloud services.
It purchases cloud from different CSPs (Cloud Service Providers) - Amazon AWS, Microsoft Azure, IBM Softlayer, etc. in advance. Later it provides an unified cloud service to end customer which includes different CSP stacks. This way it satisfies the customer’s need and provides a smooth and reliable experience in the minimum possible cost and makes cloud-agnostic system at the same time. The system provides customized attention to individual user, keeping priorities straight.

## Steps to set up application

1. Start mysql server
2. Run `CMPE_226_Project_2.sql` script to crgit eate and load data
3. Start MongoDB server and create database with name `multicloud`
4. cd "CMPE 226 Project"
5. Install python 3.6, flask, flask-mysql, pymongo, werkzeug 
6. Start application by `sudo python3 "CMPE 226 Project.py"`

<img src="https://github.com/bhattmaulik1991/cmpe226project2/blob/master/2.png" />

## Architecture

This is 3 tier architecture. Request comes from UI server hits backend server. Backend server performs database query and returns response to UI.

<img src="https://github.com/bhattmaulik1991/cmpe226project2/blob/master/1.png" />
