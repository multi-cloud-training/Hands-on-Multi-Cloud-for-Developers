# MultiAppX

[![GitHub issues](https://img.shields.io/github/issues/yamamoto42/multiappx.svg)](https://github.com/yamamoto42/multiappx)
[![Docker Automated build](https://img.shields.io/docker/automated/yamamoto42/multiappx.svg)](https://hub.docker.com/r/yamamoto42/multiappx/)
[![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/yamamoto42/multiappx.svg)](https://github.com/yamamoto42/multiappx)
[![Discord](https://img.shields.io/discord/492866063051128852.svg)](https://discordapp.com/channels/492866063051128852)

This is [Multicloud](https://en.wikipedia.org/wiki/Multicloud) Sample application by [Bootstrap](http://getbootstrap.com/).
These build and deploy are using [Azure DevOps](https://azure.microsoft.com/en-us/services/devops/).

![multicloudflow](https://github.com/yamamoto42/multiappx/blob/master/images/multicloud.png)

## Examples
|  cloud  |  service  |   type  |
|  ----  |  ----  |  ----  |
|  [AWS](http://hy01env.3rwrm5immp.us-west-2.elasticbeanstalk.com/) | Elastic Beanstalk | Tomcat |
| [Azure](http://hy01app.azurewebsites.net/) | App Service | Web app (PHP) |
| [Google Cloud Platform](https://hy01pj1.appspot.com/) | App Engine | Node.js |
| [GE Digital Predix](https://hy01app.run.aws-usw02-pr.ice.predix.io) |  Cloud Foundry APP  | staticfile_buildpack (nginx) |
| [IBM Cloud](https://hy01app.mybluemix.net/) |  Cloud Foundry APP  | staticfile_buildpack (nginx) |
| [IBM Cloud](http://hy01apc.mybluemix.net/) |  Cloud Foundry APP  | docker-image |

## Getting Started

Download and push each cloud.
```
git clone https://github.com/yamamoto42/multiappx.git
cd multiappx
```

- Google Cloud Platform (Node.js)
```
npm install
gcloud app deploy
```

- GE Digital Predix / IBM Cloud
```
cf push <APP_NAME> -b staticfile_buildpack -m 64mb
```

- IBM Cloud (Docker Container)
```
cf push <APP_NAME> -m 64mb --docker-image yamamoto42/multiappx
```

## License

MIT
