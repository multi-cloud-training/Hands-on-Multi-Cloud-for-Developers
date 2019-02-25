Sample Application for Public PaaS Model
===============================

Introduction
------------

This repository contains a sample application and auto deployment scripts for the public cloud.
By running the auto deployment script, you are able to deploy the same code to your multiple public cloud environments.

In this example, Predix, Azure and AWS environment are targeted as the public cloud.
The architecture overview is shown below.
Auto deployment scripts help us deploy the application easily. They create Web Apps service and AWS Elastic Beanstalk service in your Azure and AWS environment, respectively. And then, the bootstrap based html code stored in [modules](/modules) are deployed.

![Architecture](/img/overall.png "Architecture overall")


Demo
----
Sample applications work on the following URLs. They are deployed by the auto deployment script. It is found that the same application is running on all platforms.

Predix

<https://predix-model-sampleppm.run.aws-usw02-pr.ice.predix.io>

Azure

<https://azure-model-sampleppm.azurewebsites.net>

AWS

<http://aws-model-sampleppm.us-west-2.elasticbeanstalk.com>


Usage
-----
By running the script as described below, you can deploy a sample application to your selected environment.


    $ ./auto-deploy-<predix/azure/aws>.sh <sub-domain name>


**Remember:** Before running the auto deployment script, you have to meet the [Precondition](#precondition) as described below.

At the time of executing auto deployment script, you must specify one command line argument. Specify the ``sub-domain name`` in that argument.
Once it is done with running the script successfully, a sample application is available at following URLs.


|Script name|Deployed platform  |URL  |
|---|---|---|
|auto-deploy-predix.sh|Predix|https://\<sub-domain name>.run.aws-usw02-pr.ice.predix.io|
|auto-deploy-azure.sh|Azure  |https://\<sub-domain name>.azurewebsites.net|
|auto-deploy-aws.sh|AWS      |http://\<sub-domain name>.us-west-2.elasticbeanstalk.com|



Precondition
------------
The auto deployment script assumes that you have the following already in place:
* Linux environment
* ``zip`` command has been installed.
* git clone this repository to your current directory.

Following is the precondition for each auto deployment script.
* Predix
    * Install [Cloud Foundry CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) or [devbox](https://www.predix.io/services/other-resources/devbox.html) which is supported by GE Digital.
    * Log in to your Predix environment by using `cf login` command.

* Azure
    * Install [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest) which is supported by MicroSoft.
    * Log in to your Azure environment by using `az login` command.

* AWS
    * Install [EB CLI](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3-install.html) which is supported by Amazon Web Services.
    * Attach ``AWSElasticBeanstalkFullAccess`` policy to your IMA user

Feedback
--------
Please use [Github Issues](https://github.com/ppmuser2018/multi-cloud/issues) to submit any bugs you might find.
