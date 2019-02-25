import traceback

from azure.common.credentials import ServicePrincipalCredentials
from azure.mgmt.resource import ResourceManagementClient
from azure.mgmt.network import NetworkManagementClient
from azure.mgmt.compute import ComputeManagementClient
from azure.mgmt.compute.models import DiskCreateOption
from azure.mgmt.storage import StorageManagementClient
from azure.storage.queue import QueueService

from msrestazure.azure_exceptions import CloudError


class AzureResourceHandler:
    subscription_id = None
    credentials = None

    def __init__(self, credentials):
        self.subscription_id = credentials['subscriptionId']
        self.credentials = ServicePrincipalCredentials(
            client_id=credentials['appId'],
            secret=credentials['appPassword'],
            tenant=credentials['directoryId']
        )

    def process(self, event):
        if (event['RequestType'] == "Create" or event['RequestType'] == "Update") and event['ResourceType'] == "Custom::Azure_Resource_ResourceGroup":
            return self.create_resource_resourcegroup(event['ResourceProperties'])
        elif (event['RequestType'] == "Create" or event['RequestType'] == "Update") and event['ResourceType'] == "Custom::Azure_Network_VirtualNetwork":
            return self.create_network_virtualnetwork(event['ResourceProperties'])
        elif (event['RequestType'] == "Create" or event['RequestType'] == "Update") and event['ResourceType'] == "Custom::Azure_Network_Subnet":
            return self.create_network_subnet(event['ResourceProperties'])
        elif (event['RequestType'] == "Create" or event['RequestType'] == "Update") and event['ResourceType'] == "Custom::Azure_Network_NetworkInterface":
            return self.create_network_networkinterface(event['ResourceProperties'])
        elif (event['RequestType'] == "Create" or event['RequestType'] == "Update") and event['ResourceType'] == "Custom::Azure_Compute_VirtualMachine":
            return self.create_compute_virtualmachine(event['ResourceProperties'])
        elif event['RequestType'] == "Create" and event['ResourceType'] == "Custom::Azure_Storage_StorageAccount":
            return self.create_storage_storageaccount(event['ResourceProperties'])
        elif event['RequestType'] == "Create" and event['ResourceType'] == "Custom::Azure_Storage_Queue":
            return self.create_storage_queue(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::Azure_Resource_ResourceGroup":
            return self.delete_resource_resourcegroup(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::Azure_Network_VirtualNetwork":
            return self.delete_network_virtualnetwork(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::Azure_Network_Subnet":
            return self.delete_network_subnet(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::Azure_Network_NetworkInterface":
            return self.delete_network_networkinterface(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::Azure_Compute_VirtualMachine":
            return self.delete_compute_virtualmachine(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::Azure_Storage_StorageAccount":
            return self.delete_storage_storageaccount(event['ResourceProperties'])
        elif event['RequestType'] == "Delete" and event['ResourceType'] == "Custom::Azure_Storage_Queue":
            return self.delete_storage_queue(event['ResourceProperties'])
        else:
            raise Exception('Unhandled Azure resource or request type')

    def create_resource_resourcegroup(self, resource_properties):
        resource_client = ResourceManagementClient(self.credentials, self.subscription_id)

        resource_group = resource_client.resource_groups.create_or_update(resource_properties['Name'], {'location': resource_properties['Location']})

        # resource_group['_Ref'] = resource_group['name']

        return {
            'Id': resource_group.id,
            'Name': resource_group.name,
            'Location': resource_group.location,
            'ManagedBy': resource_group.managed_by
        }
    
    def delete_resource_resourcegroup(self, resource_properties):
        resource_client = ResourceManagementClient(self.credentials, self.subscription_id)

        op = resource_client.resource_groups.delete(resource_properties['Name'])
        op.wait()

        return {}
    
    def create_network_virtualnetwork(self, resource_properties):
        network_client = NetworkManagementClient(self.credentials, self.subscription_id)

        op = network_client.virtual_networks.create_or_update(
            resource_properties['ResourceGroupName'],
            resource_properties['Name'],
            {
                'location': resource_properties['Location'],
                'address_space': {
                    'address_prefixes': resource_properties['AddressSpace']['AddressPrefixes']
                }
            }
        )
        result = op.result()

        return {
            'Id': result.id,
            'Name': result.name,
            'Type': result.type,
            'Location': result.location
        }
    
    def delete_network_virtualnetwork(self, resource_properties):
        network_client = NetworkManagementClient(self.credentials, self.subscription_id)

        op = network_client.virtual_networks.delete(
            resource_group_name=resource_properties['ResourceGroupName'],
            virtual_network_name=resource_properties['Name']
        )
        op.wait()

        return {}

    def create_network_subnet(self, resource_properties):
        network_client = NetworkManagementClient(self.credentials, self.subscription_id)

        op = network_client.subnets.create_or_update(
            resource_properties['ResourceGroupName'],
            resource_properties['VirtualNetworkName'],
            resource_properties['Name'],
            {
                'address_prefix': resource_properties['AddressPrefix']
            }
        )
        result = op.result()

        return {
            'Id': result.id,
            'Name': result.name,
            'AddressPrefix': result.address_prefix
        }

    def delete_network_subnet(self, resource_properties):
        network_client = NetworkManagementClient(self.credentials, self.subscription_id)

        op = network_client.subnets.delete(
            resource_group_name=resource_properties['ResourceGroupName'],
            virtual_network_name=resource_properties['VirtualNetworkName'],
            subnet_name=resource_properties['Name']
        )
        op.wait()

        return {}

    def create_network_networkinterface(self, resource_properties):
        network_client = NetworkManagementClient(self.credentials, self.subscription_id)

        ip_configurations = []
        for ip_configuration in resource_properties['IpConfigurations']:
            ip_configurations.append({
                'name': ip_configuration['Name'],
                'subnet': {
                    'id': ip_configuration['Subnet']['Id']
                }
            })

        op = network_client.network_interfaces.create_or_update(
            resource_properties['ResourceGroupName'],
            resource_properties['Name'],
            {
                'location': resource_properties['Location'],
                'ip_configurations': ip_configurations
            }
        )
        result = op.result()

        return {
            'Id': result.id,
            'Name': result.name,
            'Type': result.type,
            'Location': result.location
        }

    def delete_network_networkinterface(self, resource_properties):
        network_client = NetworkManagementClient(self.credentials, self.subscription_id)

        op = network_client.network_interfaces.delete(
            resource_group_name=resource_properties['ResourceGroupName'],
            network_interface_name=resource_properties['Name']
        )
        op.wait()

        return {}

    def create_compute_virtualmachine(self, resource_properties):
        compute_client = ComputeManagementClient(self.credentials, self.subscription_id)

        network_interfaces = []
        for network_interface in resource_properties['NetworkProfile']['NetworkInterfaces']:
            network_interfaces.append({
                'id': network_interface['Id']
            })

        op = compute_client.virtual_machines.create_or_update(
            resource_properties['ResourceGroupName'],
            resource_properties['Name'],
            {
                'location': resource_properties['Location'],
                'os_profile': {
                    'computer_name': resource_properties['OsProfile']['ComputerName'],
                    'admin_username': resource_properties['OsProfile']['AdminUsername'],
                    'admin_password': resource_properties['OsProfile']['AdminPassword']
                },
                'hardware_profile': {
                    'vm_size': resource_properties['HardwareProfile']['VmSize']
                },
                'storage_profile': {
                    'image_reference': {
                        'publisher': resource_properties['StorageProfile']['ImageReference']['Publisher'],
                        'offer': resource_properties['StorageProfile']['ImageReference']['Offer'],
                        'sku': resource_properties['StorageProfile']['ImageReference']['Sku'],
                        'version': resource_properties['StorageProfile']['ImageReference']['Version']
                    },
                },
                'network_profile': {
                    'network_interfaces': network_interfaces
                }
            }
        )
        result = op.result()

        op = compute_client.virtual_machines.start(
            resource_properties['ResourceGroupName'],
            resource_properties['Name']
        )
        op.wait()

        return {
            'Id': result.id,
            'Name': result.name,
            'Type': result.type,
            'Location': result.location
        }

    def delete_compute_virtualmachine(self, resource_properties):
        compute_client = ComputeManagementClient(self.credentials, self.subscription_id)

        op = compute_client.virtual_machines.delete(
            resource_group_name=resource_properties['ResourceGroupName'],
            vm_name=resource_properties['Name']
        )
        op.wait()

        return {}
    
    def create_storage_storageaccount(self, resource_properties):
        storage_client = StorageManagementClient(self.credentials, self.subscription_id)

        op = storage_client.storage_accounts.create(
            resource_properties['ResourceGroupName'],
            resource_properties['Name'],
            {
                'sku': {
                    'name': resource_properties['Sku']['Name']
                },
                'kind': resource_properties['Kind'],
                'location': resource_properties['Location']#,
                #'accessTier': resource_properties['AccessTier']
            }
        )
        result = op.result()

        return {
            'Id': result.id,
            'Name': result.name,
            'Type': result.type
        }
    
    def delete_storage_storageaccount(self, resource_properties):
        storage_client = StorageManagementClient(self.credentials, self.subscription_id)

        storage_client.storage_accounts.delete(
            resource_group_name=resource_properties['ResourceGroupName'],
            account_name=resource_properties['Name']
        )

        return {}
    
    def create_storage_queue(self, resource_properties):
        storage_client = StorageManagementClient(self.credentials, self.subscription_id)

        key = storage_client.storage_accounts.list_keys(resource_properties['ResourceGroupName'], resource_properties['AccountName']).keys[0].value

        queue_service = QueueService(account_name=resource_properties['AccountName'], account_key=key)

        queue_service.create_queue(resource_properties['Name'])

        return {
            'Name': resource_properties['Name']
        }
    
    def delete_storage_queue(self, resource_properties):
        storage_client = StorageManagementClient(self.credentials, self.subscription_id)

        key = storage_client.storage_accounts.list_keys(resource_properties['ResourceGroupName'], resource_properties['AccountName']).keys[0].value

        queue_service = QueueService(account_name=resource_properties['AccountName'], account_key=key)

        queue_service.delete_queue(resource_properties['Name'])

        return {}
