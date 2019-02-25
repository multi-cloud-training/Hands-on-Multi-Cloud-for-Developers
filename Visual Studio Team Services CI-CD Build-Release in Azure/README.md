# Visual Studio Team Services CI/CD Build/Release Pipelines with Azure Container Services (AKS) Workshop

## Overview

This workshop will guide you through building Continuous Integration (CI) and Continuous Deployment (CD) pipelines with Visual Studio Team Services (VSTS) for use with Azure Container Service (AKS - Managed Kubernetes Service).

The labs are based upon a node.js application that allows for voting on the Justice League Superheroes. Data is stored in MongoDB backend.  This builds upon concepts from [Microsoft Intelligent Cloud Blackbelt Team :: Hackfest](https://github.com/Azure/blackbelt-aks-hackfest): Labs 1-3.

The general workflow/result will be as follows:

- Push code to source control (Git hosted in VSTS)
- Trigger a continuous integration (CI) build pipeline when project code is updated via Git
- Package app code into a container image (Docker Image)
- Push docker image to a central container registry (Azure Container Registry) upon a successful build
- Trigger a continuous deployment (CD) release pipeline upon a successful build
- Deploy container image to target deployment environment/platform (Azure Container Services aka. ACS or AKS) upon successful a release
- Rinse and repeat upon each code update via Git
- Profit

![workflow](hol-content/img/workflow.png)

> Note: These labs are designed to run on a Linux CentOS VM running in Azure (jumpbox) along with Azure Cloud Shell. They can potentially be run locally on a Mac or Windows, but the instructions are not written towards that experience. ie - "You're on your own."

> Note: Since we are working on a jumpbox, note that Copy and Paste are a bit different when working in the terminal. You can use Shift+Ctrl+C for Copy and Shift+Ctrl+V for Paste when working in the terminal. Outside of the terminal Copy and Paste behaves as expected using Ctrl+C and Ctrl+V. 

## Prerequisite Lab

You must have previously completed one (1) of the following Hands on Labs/"Hackfests":
1. [Microsoft Intelligent Cloud Blackbelt Team :: Hackfest](https://github.com/Azure/blackbelt-aks-hackfest): Labs 1-5
  - This HoL/hackfest is a detailed guide to deploying apps to a Kuberenetes istributed computing cluster on Azure (i.e. AKS)
2. [OSS Canada AKS Mini Hackfest](https://github.com/OSSCanada/aks-mini-hackfest)
  - This HoL/hackfest is a subset of the [Microsoft Intelligent Cloud Blackbelt Team :: Hackfest](https://github.com/Azure/blackbelt-aks-hackfest).  It only includes steps to build apps into docker images, deploying those images to a container registry and creating an Azure Managed Kubernetes Service (i.e. AKS).

## Hands-on Lab Guide

- Complete one (1) of the [Prerequisite Labs](#prerequisite-lab) first

1. [Setting up VSTS](hol-content/01-setup_vsts.md)
2. [Building a VSTS Continuous Integration Pipeline](hol-content/02-build_vsts_ci.md)
3. [Building a VSTS Continuous Deployment Pipeline](hol-content/03-build_vsts_cd.md)

## Maintainers and Contact Information

- Kevin Harris - [@kevingbb](https://github.com/kevingbb)
- Ray Kao - [@raykao](https://github.com/raykao)