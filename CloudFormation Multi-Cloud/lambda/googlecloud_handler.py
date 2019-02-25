import traceback
import time

import googleapiclient.discovery
from google.oauth2 import service_account


class GoogleCloudResourceHandler:
    credentials = None

    def __init__(self, credentials):
        SCOPES = [
            'https://www.googleapis.com/auth/cloud-platform'
        ]

        self.credentials = service_account.Credentials.from_service_account_info(credentials, scopes=SCOPES)

    def process(self, event):
        if event['RequestType'] == "Create" and event['ResourceType'] == "Custom::GoogleCloud_Compute_Network":
            return self.create_compute_network(event['ResourceProperties'])
        elif event['RequestType'] == "Create" and event['ResourceType'] == "Custom::GoogleCloud_Compute_Subnetwork":
            return self.create_compute_subnetwork(event['ResourceProperties'])
        elif event['RequestType'] == "Create" and event['ResourceType'] == "Custom::GoogleCloud_Compute_Instance":
            return self.create_compute_instance(event['ResourceProperties'])
        elif event['RequestType'] == "Create" and event['ResourceType'] == "Custom::GoogleCloud_Storage_Bucket":
            return self.create_storage_bucket(event['ResourceProperties'])
        elif event['RequestType'] == "Create" and event['ResourceType'] == "Custom::GoogleCloud_PubSub_Topic":
            return self.create_pubsub_topic(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::GoogleCloud_Compute_Network":
            return self.delete_compute_network(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::GoogleCloud_Compute_Subnetwork":
            return self.delete_compute_subnetwork(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::GoogleCloud_Compute_Instance":
            return self.delete_compute_instance(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::GoogleCloud_Storage_Bucket":
            return self.delete_storage_bucket(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::GoogleCloud_PubSub_Topic":
            return self.delete_pubsub_topic(event['ResourceProperties'])
        else:
            raise Exception('Unhandled Google Cloud resource or request type')

    def wait_for_global_operation(self, client, project, operation):
        while True:
            result = client.globalOperations().get(
                project=project,
                operation=operation
            ).execute()

            if result['status'] == 'DONE':
                if 'error' in result:
                    raise Exception(result['error'])
                return result

            time.sleep(1)

    def wait_for_region_operation(self, client, project, region, operation):
        while True:
            result = client.regionOperations().get(
                project=project,
                region=region,
                operation=operation
            ).execute()

            if result['status'] == 'DONE':
                if 'error' in result:
                    raise Exception(result['error'])
                return result

            time.sleep(1)

    def wait_for_zone_operation(self, client, project, zone, operation):
        while True:
            result = client.zoneOperations().get(
                project=project,
                zone=zone,
                operation=operation
            ).execute()

            if result['status'] == 'DONE':
                if 'error' in result:
                    raise Exception(result['error'])
                return result

            time.sleep(1)

    def create_compute_network(self, resource_properties):
        compute_client = googleapiclient.discovery.build('compute', 'v1', credentials=self.credentials)

        op = compute_client.networks().insert(
            project=resource_properties['Project'],
            body={
                "routingConfig": {
                    "routingMode": resource_properties['RoutingConfig']['RoutingMode']
                },
                "name": resource_properties['Name'],
                "description": resource_properties['Description'],
                "autoCreateSubnetworks": resource_properties['AutoCreateSubnetworks']
            }
        ).execute()

        self.wait_for_global_operation(compute_client, resource_properties['Project'], op['name'])
        
        return {
            'Name': resource_properties['Name']
        }

    def delete_compute_network(self, resource_properties):
        compute_client = googleapiclient.discovery.build('compute', 'v1', credentials=self.credentials)

        op = compute_client.networks().delete(
            project=resource_properties['Project'],
            network=resource_properties['Name']
        ).execute()

        self.wait_for_global_operation(compute_client, resource_properties['Project'], op['name'])
        
        return {}

    def create_compute_subnetwork(self, resource_properties):
        compute_client = googleapiclient.discovery.build('compute', 'v1', credentials=self.credentials)

        op = compute_client.subnetworks().insert(
            project=resource_properties['Project'],
            region=resource_properties['Region'],
            body={
                "privateIpGoogleAccess": resource_properties['PrivateIpGoogleAccess'],
                "enableFlowLogs": resource_properties['EnableFlowLogs'],
                "name": resource_properties['Name'],
                "description": resource_properties['Description'],
                "ipCidrRange": resource_properties['IpCidrRange'],
                "network": "https://clients6.google.com/compute/v1/projects/%s/global/networks/%s" % (resource_properties['Project'], resource_properties['Network'])
            }
        ).execute()

        self.wait_for_region_operation(compute_client, resource_properties['Project'], resource_properties['Region'], op['name'])
        
        return {
            'Name': resource_properties['Name']
        }

    def delete_compute_subnetwork(self, resource_properties):
        compute_client = googleapiclient.discovery.build('compute', 'v1', credentials=self.credentials)

        op = compute_client.subnetworks().delete(
            project=resource_properties['Project'],
            region=resource_properties['Region'],
            subnetwork=resource_properties['Name']
        ).execute()

        self.wait_for_region_operation(compute_client, resource_properties['Project'], resource_properties['Region'], op['name'])
        
        return {}

    def create_compute_instance(self, resource_properties):
        compute_client = googleapiclient.discovery.build('compute', 'v1', credentials=self.credentials)

        disks = []
        for disk in resource_properties['Disks']:
            disks.append({
                'boot': disk['Boot'],
                'autoDelete': disk['AutoDelete'],
                'initializeParams': {
                    'sourceImage': 'https://www.googleapis.com/compute/v1/projects/%s/global/images/family/%s' % (disk['InitializeParams']['SourceImage']['Project'], disk['InitializeParams']['SourceImage']['Family']),
                }
            })
        
        network_interfaces = []
        for network_interface in resource_properties['NetworkInterfaces']:
            access_configs = []
            for access_config in network_interface['AccessConfigs']:
                access_configs.append({
                    'type': access_config['Type'],
                    'name': access_config['Name']
                })

            network_interfaces.append({
                'subnetwork': "projects/%s/regions/%s/subnetworks/%s" % (network_interface['Subnetwork']['Project'], network_interface['Subnetwork']['Region'], network_interface['Subnetwork']['Name']),
                'accessConfigs': access_configs
            })
        
        service_accounts = []
        for service_account in resource_properties['ServiceAccounts']:
            scopes = []
            for scope in service_account['Scopes']:
                scopes.append('https://www.googleapis.com/auth/%s' % scope)
            
            service_accounts.append({
                'email': service_account['Email'],
                'scopes': scopes
            })

        op = compute_client.instances().insert(
            project=resource_properties['Project'],
            zone=resource_properties['Zone'],
            body={
                'name': resource_properties['Name'],
                'machineType': "zones/%s/machineTypes/%s" % (resource_properties['Zone'], resource_properties['MachineType']),
                'disks': disks,
                'networkInterfaces': network_interfaces,
                'serviceAccounts': service_accounts
            }
        ).execute()

        self.wait_for_zone_operation(compute_client, resource_properties['Project'], resource_properties['Zone'], op['name'])
        
        return {
            'Name': resource_properties['Name']
        }

    def delete_compute_instance(self, resource_properties):
        compute_client = googleapiclient.discovery.build('compute', 'v1', credentials=self.credentials)

        op = compute_client.instances().delete(
            project=resource_properties['Project'],
            zone=resource_properties['Zone'],
            instance=resource_properties['Name']
        ).execute()

        self.wait_for_zone_operation(compute_client, resource_properties['Project'], resource_properties['Zone'], op['name'])
        
        return {}

    def create_storage_bucket(self, resource_properties):
        storage_client = googleapiclient.discovery.build('storage', 'v1', credentials=self.credentials)

        op = storage_client.buckets().insert(
            project=resource_properties['Project'],
            body={
                "name": resource_properties['Name']
            }
        ).execute()

        self.wait_for_global_operation(storage_client, resource_properties['Project'], op['name'])
        
        return {
            'Name': resource_properties['Name']
        }

    def delete_storage_bucket(self, resource_properties):
        storage_client = googleapiclient.discovery.build('storage', 'v1', credentials=self.credentials)

        op = storage_client.buckets().delete(
            project=resource_properties['Project'],
            bucket=resource_properties['Name']
        ).execute()

        self.wait_for_global_operation(storage_client, resource_properties['Project'], op['name'])
        
        return {}

    def create_pubsub_topic(self, resource_properties):
        pubsub_client = googleapiclient.discovery.build('pubsub', 'v1', credentials=self.credentials)

        topic = pubsub_client.projects().topics().create(
            name='projects/%s/topics/%s' % (resource_properties['Project'], resource_properties['Name']),
            body={}
        ).execute()
        
        return {
            'Name': resource_properties['Name']
        }

    def delete_pubsub_topic(self, resource_properties):
        pubsub_client = googleapiclient.discovery.build('pubsub', 'v1', credentials=self.credentials)

        pubsub_client.projects().topics().delete(
            topic='projects/%s/topics/%s' % (resource_properties['Project'], resource_properties['Name'])
        ).execute()
        
        return {}
