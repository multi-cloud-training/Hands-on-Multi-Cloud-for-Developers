/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * MULTICLOUD CHAT DEMO
 * The intent of this demo is to provide the audience with a very simple and easy way to visualize a multi-cloud application with
 * the ability to simply failover from one cloud to the next
 * Thomas Kunnumpurath
 * thomas.kunnumpurath@solace.com 
*/

var vpnSettings = {
    "vpnName":"solace-chat",
    "userName":"solace-chat-user",
    "password":"solacechatter"
};

var cloudPropertiesMap = {
    "aws": {
        "host":"ws://x.x.x.x:port",
        "up": true,
        "connected":false
    },
    "gcp": {
        "host":"ws://x.x.x.x:port",
      "up":true,
      "connected":false  
    },
    "azure": {
        "host":"ws://x.x.x.x:port",
        "up":true,
        "connected":false
    }
};


var CloudSessionChecker = function(cloudProvider){
    'use strict';
    var cloudSessionChecker = {};
    cloudSessionChecker.session = null;
    
    
    //Logger
    cloudSessionChecker.log = function(line){
        var now = new Date();
        var time = [('0' + now.getHours()).slice(-2), ('0' + now.getMinutes()).slice(-2),
            ('0' + now.getSeconds()).slice(-2)];
        var timestamp = '[' + time.join(':') + '] ';
        console.log(timestamp + ' ' + line);
    };

    cloudSessionChecker.connect = function () {
        // extract params
        if (cloudSessionChecker.session !== null) {
            cloudSessionChecker.log('Already connected and ready to subscribe.');
            return;
        }
        
        // create session
        try {
            cloudSessionChecker.session = solace.SolclientFactory.createSession({
                // solace.SessionProperties
                url:      cloudPropertiesMap[cloudProvider].host,
                vpnName:  vpnSettings.vpnName,
                userName: vpnSettings.userName,
                password: vpnSettings.password,
                connectRetries:5
            });
            cloudSessionChecker.session.connect();
        } catch (error) {
            cloudSessionChecker.log(error.toString());
        }

        // define session event listeners
        cloudSessionChecker.session.on(solace.SessionEventCode.UP_NOTICE, function (sessionEvent) {
            cloudSessionChecker.log(sessionEvent);
            cloudPropertiesMap[cloudProvider].up=true;
            updateCanvas();
        });

        cloudSessionChecker.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, function (sessionEvent) {
            cloudPropertiesMap[cloudProvider].up=false;
            updateCanvas();
            cloudSessionChecker.log('Connection failed to the message router: ' + sessionEvent.infoStr +
                ' - check correct parameter values and connectivity!');
        });

        cloudSessionChecker.session.on(solace.SessionEventCode.DISCONNECTED, function (sessionEvent) {
            if (cloudSessionChecker.session !== null) {
                cloudSessionChecker.session.dispose();
                cloudSessionChecker.session = null;
            }
            cloudPropertiesMap[cloudProvider].up=false;
            updateCanvas();
        });
      
       cloudSessionChecker.session.on(solace.SessionEventCode.RECONNECTING_NOTICE,function(sessionEvent){
            cloudSessionChecker.log(sessionEvent);
            cloudPropertiesMap[cloudProvider].up=false;
            updateCanvas();
        });

        cloudSessionChecker.session.on(solace.SessionEventCode.RECONNECTED_NOTICE,function(sessionEvent){
            cloudSessionChecker.log(sessionEvent);
            cloudPropertiesMap[cloudProvider].up=true;
            updateCanvas();
          
        });
    };
   
    cloudSessionChecker.connectToSolace = function () {
        try {
            cloudSessionChecker.connect();
        } catch (error) {
            cloudSessionChecker.log(error.toString());
        }
    };

    return cloudSessionChecker;
}
    

var SolaceChatter = function (cloudProvider) {
    'use strict';
    
    var hostList=[];

    if(cloudProvider=='gcp'){
       hostList.push.apply(hostList,[cloudPropertiesMap.gcp.host,cloudPropertiesMap.aws.host,cloudPropertiesMap.azure.host]);
    }else if(cloudProvider=='aws'){
        hostList.push.apply(hostList,[cloudPropertiesMap.aws.host,cloudPropertiesMap.azure.host,cloudPropertiesMap.gcp.host]);
    }else if(cloudProvider=='azure'){
        hostList.push.apply(hostList,[cloudPropertiesMap.azure.host,cloudPropertiesMap.aws.host,cloudPropertiesMap.gcp.host]);
    }


    var solaceChatter = {};
    solaceChatter.session = null;
    solaceChatter.topicName = 'SOLACE/CHAT';
    solaceChatter.subscribed = false;

    // Logger
    solaceChatter.log = function (line) {
        var now = new Date();
        var time = [('0' + now.getHours()).slice(-2), ('0' + now.getMinutes()).slice(-2),
            ('0' + now.getSeconds()).slice(-2)];
        var timestamp = '[' + time.join(':') + '] ';
        console.log(timestamp + ' ' + line);
      
    };

    solaceChatter.chat = function(line,type){
        var now = new Date();
        var time = [('0' + now.getHours()).slice(-2), ('0' + now.getMinutes()).slice(-2),
            ('0' + now.getSeconds()).slice(-2)];
        var timestamp = '[' + time.join(':') + '] ';
        let chatbox = document.getElementById('chat-box');
        let chatLog = document.createElement('span');
        chatLog.className = type;
        chatLog.innerHTML = timestamp + line + '<br>';
        chatbox.appendChild(chatLog);
    }

    solaceChatter.log('\n*** SolaceChatter to topic "' + solaceChatter.topicName + '" is ready to connect ***');

    // Establishes connection to Solace router
    solaceChatter.connect = function () {
        // extract params
        if (solaceChatter.session !== null) {
            solaceChatter.log('Already connected and ready to subscribe.');
            return;
        }
        
        // create session
        try {
            solaceChatter.session = solace.SolclientFactory.createSession({
                // solace.SessionProperties
                url:      hostList,
                vpnName:  vpnSettings.vpnName,
                userName: vpnSettings.userName,
                password: vpnSettings.password,
                reapplySubscriptions: true
            });
            solaceChatter.session.connect();
        } catch (error) {
            solaceChatter.log(error.toString());
        }
        // define session event listeners
        solaceChatter.session.on(solace.SessionEventCode.UP_NOTICE, function (sessionEvent) {
            solaceChatter.chat('Succesfully connected to '+ getCloudFromInfoStr(sessionEvent.infoStr),'chat-log');
      
            solaceChatter.log(sessionEvent);
            solaceChatter.subscribe();
        });
        solaceChatter.session.on(solace.SessionEventCode.CONNECT_FAILED_ERROR, function (sessionEvent) {
            solaceChatter.log('Connection failed to the message router: ' + sessionEvent.infoStr +
                ' - check correct parameter values and connectivity!');
        });
        solaceChatter.session.on(solace.SessionEventCode.DISCONNECTED, function (sessionEvent) {
            solaceChatter.chat('Disconnected.');
            solaceChatter.subscribed = false;
            if (solaceChatter.session !== null) {
                solaceChatter.session.dispose();
                solaceChatter.session = null;
            }
        });
        solaceChatter.session.on(solace.SessionEventCode.SUBSCRIPTION_ERROR, function (sessionEvent) {
            solaceChatter.chat('Cannot subscribe to topic: ' + sessionEvent.correlationKey);
        });
        solaceChatter.session.on(solace.SessionEventCode.SUBSCRIPTION_OK, function (sessionEvent) {
            if (solaceChatter.subscribed) {
                solaceChatter.subscribed = false;
                solaceChatter.log('Successfully unsubscribed from topic: ' + sessionEvent.correlationKey);
            } else {
                solaceChatter.subscribed = true;
                solaceChatter.log('Successfully subscribed to topic: ' + sessionEvent.correlationKey);
                solaceChatter.log('=== Ready to receive messages. ===');
            }
        });

        solaceChatter.session.on(solace.SessionEventCode.RECONNECTING_NOTICE,function(sessionEvent){
            solaceChatter.chat('Lost connection...','chat-log');
            solaceChatter.log(sessionEvent);
        });

        solaceChatter.session.on(solace.SessionEventCode.RECONNECTED_NOTICE,function(sessionEvent){
            solaceChatter.chat('Succesfully re-connected to '+ getCloudFromInfoStr(sessionEvent.infoStr),'chat-log');
            solaceChatter.log(sessionEvent);
          
        });
        // define message event listener
        solaceChatter.session.on(solace.SessionEventCode.MESSAGE, function (message) {
            solaceChatter.chat(message.getSenderId()+":"+message.getBinaryAttachment(),'chat');
        });
    };

    
    solaceChatter.connectToSolace = function () {
        try {
            solaceChatter.connect();
        } catch (error) {
            solaceChatter.log(error.toString());
        }
    };


    solaceChatter.publish = function (senderId,messageText) {
        if (solaceChatter.session !== null) {
            var message = solace.SolclientFactory.createMessage();
            message.setDestination(solace.SolclientFactory.createTopicDestination(solaceChatter.topicName));
            message.setBinaryAttachment(messageText);
            message.setSenderId(senderId);
            message.setDeliveryMode(solace.MessageDeliveryModeType.DIRECT);
            try {
                solaceChatter.session.send(message);
            } catch (error) {
                solaceChatter.log(error.toString());
            }
        } else {
            solaceChatter.log('Cannot publish because not connected to Solace message router.');
        }
    };

    // Subscribes to topic on Solace message router
    solaceChatter.subscribe = function () {
        if (solaceChatter.session !== null) {
            if (solaceChatter.subscribed) {
                solaceChatter.log('Already subscribed to "' + solaceChatter.topicName
                    + '" and ready to receive messages.');
            } else {
                solaceChatter.log('Subscribing to topic: ' + solaceChatter.topicName);
                try {
                    solaceChatter.session.subscribe(
                        solace.SolclientFactory.createTopicDestination(solaceChatter.topicName),
                        true, // generate confirmation when subscription is added successfully
                        solaceChatter.topicName, // use topic name as correlation key
                        10000 // 10 seconds timeout for this operation
                    );
                } catch (error) {
                    solaceChatter.log(error.toString());
                }
            }
        } else {
            solaceChatter.log('Cannot subscribe because not connected to Solace message router.');
        }
    };

    // Unsubscribes from topic on Solace message router
    solaceChatter.unsubscribe = function () {
        if (solaceChatter.session !== null) {
            if (solaceChatter.subscribed) {
                solaceChatter.log('Unsubscribing from topic: ' + solaceChatter.topicName);
                try {
                    solaceChatter.session.unsubscribe(
                        solace.SolclientFactory.createTopicDestination(solaceChatter.topicName),
                        true, // generate confirmation when subscription is removed successfully
                        solaceChatter.topicName, // use topic name as correlation key
                        10000 // 10 seconds timeout for this operation
                    );
                } catch (error) {
                    solaceChatter.log(error.toString());
                }
            } else {
                solaceChatter.log('Cannot unsubscribe because not subscribed to the topic "'
                    + solaceChatter.topicName + '"');
            }
        } else {
            solaceChatter.log('Cannot unsubscribe because not connected to Solace message router.');
        }
    };

    // Gracefully disconnects from Solace message router
    solaceChatter.disconnect = function () {
        solaceChatter.log('Disconnecting from Solace message router...');
        if (solaceChatter.session !== null) {
            try {
                solaceChatter.session.disconnect();
            } catch (error) {
                solaceChatter.log(error.toString());
            }
        } else {
            solaceChatter.log('Not connected to Solace message router.');
        }
    };

    return solaceChatter;
};



function getCloudFromInfoStr(infoStr) {
    var host = infoStr.substring(infoStr.indexOf('ws://'),infoStr.lastIndexOf("/"));
    var cloudIcon='';
    var buttonClassName='';
    var cloud='';
    if(host===cloudPropertiesMap.aws.host){
        cloudPropertiesMap.aws.connected=true;
        cloudPropertiesMap.azure.connected=false;
        cloudPropertiesMap.gcp.connected=false;
        cloud='aws';
        cloudIcon='fab fa-aws';
    }else if(host===cloudPropertiesMap.gcp.host){
        cloudPropertiesMap.aws.connected=false;
        cloudPropertiesMap.azure.connected=false;
        cloudPropertiesMap.gcp.connected=true;
        cloud='gcp';
        cloudIcon='fab fa-google-plus-g';
    }else if(host===cloudPropertiesMap.azure.host){
        cloudPropertiesMap.aws.connected=false;
        cloudPropertiesMap.azure.connected=true;
        cloudPropertiesMap.gcp.connected=false;
        cloud='azure';
        cloudIcon='fab fa-windows';
    }

    document.getElementById("banner").className='banner-'+cloud;
    document.getElementById("cloud-icon-button").className=cloudIcon;
    document.getElementById("cloud-icon-banner").className=cloudIcon;
    document.getElementById("send-chat-button").className='pure-button button-'+cloud;
    updateCanvas();

    return cloud;
}



function updateCanvas(){
    var canvas = document.getElementById('multi-cloud-canvas');
    if (canvas.getContext) {

        var ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        //Loading of the home test image - multiCloudImg
        var multiCloudImg = new Image();
        multiCloudImg.src = 'resources/multi-cloud.png';
        multiCloudImg.onload = function () {
        //draw background image
        ctx.drawImage(multiCloudImg, 0, 0,320,200);
        ctx.lineWidth=2;
        ctx.strokeStyle='#ff8c00';
        //draw a box over the top
        //Connectivity to azure
        if(cloudPropertiesMap.azure.connected){
        ctx.beginPath();
        ctx.moveTo(166,120);
        ctx.lineTo(166,170);
        ctx.stroke();
        }else if(cloudPropertiesMap.aws.connected){
        //aws
        ctx.beginPath();
        ctx.moveTo(87,65);
        ctx.lineTo(160,100);
        ctx.stroke();
        }else if(cloudPropertiesMap.gcp.connected){
        //gcp
        ctx.beginPath();
        ctx.moveTo(235,63);
        ctx.lineTo(175,100);
        ctx.stroke();
        }

        var xImg = new Image();
        xImg.src = 'resources/x-disabled.png';
        
        xImg.onload = function(){
            if(!cloudPropertiesMap.aws.up){
            //aws
            ctx.drawImage(xImg,40,25,50,50);
            }    
            if(!cloudPropertiesMap.azure.up){
            //azure
            ctx.drawImage(xImg,145,140,50,50);
            } 
            if(!cloudPropertiesMap.gcp.up){
             //gcp
             ctx.drawImage(xImg,230,25,50,50);
            }

            if(!cloudPropertiesMap.aws.up || !cloudPropertiesMap.azure.up){
            //aws-azure
            ctx.drawImage(xImg,85,110,25,25);
            }
            
            if(!cloudPropertiesMap.azure.up || !cloudPropertiesMap.gcp.up){
            //azure-gcp
            ctx.drawImage(xImg,220,110,25,25);
            }

            if(!cloudPropertiesMap.aws.up || !cloudPropertiesMap.gcp.up){
             //aws-gcp
            ctx.drawImage(xImg,150,45,25,25);
            }

        };    


        };
}
}
