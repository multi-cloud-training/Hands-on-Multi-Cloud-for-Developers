import os
import re
import json
import inspect
from collections import defaultdict

import yaml
from fabric.api import local, settings, hide
from libcloud.compute.types import Provider, OLD_CONSTANT_TO_NEW_MAPPING
from libcloud.compute.providers import get_driver as get_libcloud_driver

from .utils import render_template, get_templates_vars, render_json_to_template


BASE_PATH = os.path.dirname(os.path.realpath(__file__))

with open(os.path.join(BASE_PATH, 'config.yaml'), 'r') as conf_file:
    CONFIG = yaml.load(conf_file)


class Service:
    def __init__(self, node_name, service_type):
        self.config = CONFIG['services']
        self.name = '{prefix}-{node_name}-{type}.service'.format(
            prefix=self.config['prefix'],
            node_name=node_name,
            type=service_type
        )
        self.type = service_type

    @property
    def exist(self):
        with settings(warn_only=True):
            with hide('commands'):
                result = local('systemctl list-unit-files', capture=True)
        for line in result.split('\n'):
            if line.startswith(self.name):
                return True
        return False

    def create(self, template_context):
        render_template(
            template=self.config['templates'][self.type],
            to_file=os.path.join(self.config['systemd_dir'], self.name),
            context=template_context
        )

        # Enable and run compute node service
        self.command('enable')
        self.command('start')

    def delete(self):
        # Disable and stop service
        self.command('disable')
        self.command('stop')

        # Delete service file
        service_file = os.path.join(CONFIG['services']['systemd_dir'], self.name)
        if os.path.exists(service_file):
            os.remove(service_file)

    def command(self, cmd):
        valid_commands = CONFIG['services']['commands']
        if cmd not in valid_commands:
            raise BadServiceCommand('Command "{}" not supported. Valid commands: {}'.format(cmd, valid_commands))

        with settings(warn_only=True):
            response = local('systemctl {cmd} {nova_service}'.format(cmd=cmd, nova_service=self.name), capture=False)
            return response


class Node:
    def __init__(self, name, node_type):
        self.name = name
        self.type = node_type
        self.config = CONFIG['nodes']
        self.services = {service_name: self._get_service(service_name) for service_name in self.config['services']}

    def create(self, template_context):
        for service_name, service in self.services.items():
            if not service:
                service_conf = self.config['services'][service_name]

                template_context['config_file'] = os.path.join(
                    service_conf['dir_dest'], '{prefix}-{name}-{type}.conf'.format(
                        prefix=self.config['prefix'],
                        name=self.name,
                        type=self.type
                    )
                )

                render_template(
                    template=service_conf['template'],
                    to_file=template_context['config_file'],
                    context=template_context
                )

                new_service = Service(node_name=self.name, service_type=service_name)
                new_service.create(template_context=template_context)
                self.services[service_name] = new_service

    def delete(self):
        # Delete all services
        for service_name, service in self.services.items():
            if service:
                service.delete()

            # Delete configs
            service_conf = self.config['services'][service_name]
            config_file = os.path.join(
                service_conf['dir_dest'], '{prefix}-{name}-{type}.conf'.format(
                    prefix=self.config['prefix'],
                    name=self.name,
                    type=self.type
                )
            )
            if os.path.exists(config_file):
                os.remove(config_file)

    def command(self, cmd):
        response = []
        for service_name, service in self.services.items():
            if service:
                response.append(service.command(cmd))
        return response

    def _get_service(self, service_type):
        service = Service(node_name=self.name, service_type=service_type)
        return service if service.exist else None


class NodeManager:
    def __init__(self):
        self.valid_node_types = self._get_valid_node_types()

    def node_create(self, node_name, node_type, **kwargs):
        # Check node type
        if node_type not in self.valid_node_types['providers']:
            raise NodeTypeNotFound('Node type "{}" not found. Valid types: {}'.format(node_type,
                                                                                      self.valid_node_types.keys()))

        # Check if node already exist
        enabled_nodes = self.node_list()
        if node_name in [node.name for node in enabled_nodes]:
            raise NodeAlreadyExist('Node "{}" already exist'.format(node_name))

        kwargs['hostname'] = node_name
        kwargs['node_type'] = node_type

        # Check if we got all necessary params in kwargs
        templates_vars = self.get_node_params(node_type)
        if not all([var in kwargs for var in templates_vars]):
            raise AttributeError('Too few arguments to create "{}" node. Need to provide: {}'
                                 .format(node_type, templates_vars.keys()))

        kwargs['provider_config'] = render_json_to_template(
            provider=self.valid_node_types['providers'][node_type],
            token_values=kwargs
        )

        new_node = Node(name=node_name, node_type=node_type)
        new_node.create(template_context=kwargs)

    def node_delete(self, node_name):
        node = self.node_get(node_name)
        node.delete()

    def node_get(self, node_name):
        all_nodes = self.node_list()

        for node in all_nodes:
            if node.name == node_name:
                return node

    @staticmethod
    def node_list():
        nodes = []
        nodes_conf = CONFIG['nodes']

        for filename in os.listdir(nodes_conf['services']['nova']['dir_dest']):
            match = re.search(r'{}-(?P<name>.+)-(?P<type>.+)\.conf'.format(nodes_conf['prefix']), filename)
            if match:
                node = Node(
                    name=match.groupdict().get('name'),
                    node_type=match.groupdict().get('type')
                )
                nodes.append(node)
        return nodes

    def get_node_params(self, node_type=None):
        # Check if we got all necessary params in kwargs
        templates_vars = get_templates_vars(
            templates=[service['template'] for service_name, service in CONFIG['nodes']['services'].items()]
        )
        # Remove hostname and provider_config form vars, because hostname == node_name
        templates_vars.remove('hostname')
        templates_vars.remove('provider_config')

        all_node_params = {}

        for n_type_name, n_type_params in self.valid_node_types['providers'].items():
            node_params = defaultdict(lambda: 'Description not provided')
            node_params.update(n_type_params.get('tokens', {}))

            for token in templates_vars:
                if token in self.valid_node_types['basic_tokens']:
                    node_params[token] = self.valid_node_types['basic_tokens'][token]
                else:
                    node_params[token] = 'Description not provided'
            if n_type_name == node_type:
                return node_params
            all_node_params[n_type_name] = node_params

        return all_node_params

    @staticmethod
    def _get_libcloud_providers():
        providers = {}
        for provider_name in [item for item in vars(Provider) if not item.startswith('_')]:
            if provider_name.lower() in OLD_CONSTANT_TO_NEW_MAPPING:
                continue
            try:
                provider_cls = get_libcloud_driver(getattr(Provider, provider_name))
            except Exception as e:
                continue

            provider_cls_info = inspect.getargspec(provider_cls)

            node_params = defaultdict(lambda: 'Description not provided')
            for arg in provider_cls_info.args:
                if arg not in ['cls', 'self']:
                    node_params[arg] = {
                        'description': {
                            'en': '',
                            'ru': ''
                        },
                        'type': 'str'
                    }

            providers[provider_name] = {
                'section_name': 'kozinaki_GCP',
                'tokens': node_params
            }

        return providers

    def _get_valid_node_types(self):
        with open(os.path.join(BASE_PATH, 'providers.json'), 'r') as f:
            providers_data = json.load(f)

        # libcloud_providers = self._get_libcloud_providers()
        # providers_data['providers'].update(libcloud_providers)

        return providers_data


# Compute node manager exceptions
ComputeNodeManager = type('ComputeNodeManager', (Exception,), {})
BadServiceCommand = type('BadServiceCommand', (ComputeNodeManager,), {})
NodeNotFound = type('NodeNotFound', (ComputeNodeManager,), {})
NodeAlreadyExist = type('NodeAlreadyExist', (ComputeNodeManager,), {})
NodeTypeNotFound = type('NodeTypeNotFound', (ComputeNodeManager,), {})
