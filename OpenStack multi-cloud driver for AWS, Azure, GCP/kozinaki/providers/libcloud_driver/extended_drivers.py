from libcloud.utils.py3 import httplib
from libcloud.compute.types import Provider
from libcloud.compute.providers import get_driver
from libcloud.compute.base import NodeSize
from libcloud.common.linode import API_ROOT


def get_extended_driver(driver_cls, nova_config=None):
    extended_drivers = {
        'Vultr': VultrNodeDriverExt,
        'Linode': LinodeNodeDriverExt,
        'Packet': PacketNodeDriverExt,
    }

    driver = extended_drivers[driver_cls.name] if driver_cls.name in extended_drivers.keys() else driver_cls
    driver.nova_config = nova_config
    return driver


class VultrNodeDriverExt(get_driver(Provider.VULTR)):

    def ex_shutdown_node(self, node):
        params = {'SUBID': node.id}
        res = self.connection.post('/v1/server/halt', params)

        return res.status == httplib.OK

    def ex_power_on_node(self, node):
        params = {'SUBID': node.id}
        res = self.connection.post('/v1/server/start', params)

        return res.status == httplib.OK


class LinodeNodeDriverExt(get_driver(Provider.LINODE)):

    def ex_shutdown_node(self, node):
        params = {"api_action": "linode.shutdown", "LinodeID": node.id}
        self.connection.request(API_ROOT, params=params)
        return True

    def ex_power_on_node(self, node):
        params = {"api_action": "linode.boot", "LinodeID": node.id}
        self.connection.request(API_ROOT, params=params)
        return True


class PacketNodeDriverExt(get_driver(Provider.PACKET)):

    def ex_shutdown_node(self, node):
        params = {'type': 'power_off'}
        res = self.connection.request('/devices/%s/actions' % (node.id),
                                      params=params, method='POST')
        return res.status == httplib.OK

    def ex_power_on_node(self, node):
        params = {'type': 'power_on'}
        res = self.connection.request('/devices/%s/actions' % (node.id),
                                      params=params, method='POST')
        return res.status == httplib.OK

    def list_nodes(self):
        data = self.connection.request('/projects/%s/devices' % (self.nova_config.get('project_id')),
                                       params={'include': 'plan'}).object['devices']
        return list(map(self._to_node, data))

    def _to_size(self, data):
        extra = {'description': data['description'], 'line': data['line']}

        ram = data['specs'].get('memory', {}).get('total')
        if ram:
            ram = ram.lower()
            if 'mb' in ram:
                ram = int(ram.replace('mb', ''))
            elif 'gb' in ram:
                ram = int(ram.replace('gb', '')) * 1024

        disk = 0
        for disks in data['specs'].get('drives', []):
            if 'GB' in disks['size']:
                disk_size = int(disks['size'].replace('GB', ''))
            elif 'TB' in disks['size']:
                size = disks['size'].replace('TB', '')
                disk_size = (float(size) if '.' in size else int(size)) * 1024
            else:
                raise Exception('Unknown disk size metric "{}"'.format(disks['size']))
            disk += disks['count'] * disk_size

        price = data['pricing']['hour']

        return NodeSize(id=data['slug'], name=data['name'], ram=ram, disk=disk,
                        bandwidth=0, price=price, extra=extra, driver=self)

    def create_node(self, name, size, image, location):
        """
        Create a node.
        :return: The newly created node.
        :rtype: :class:`Node`
        """

        params = {'hostname': name, 'plan': size.id,
                  'operating_system': image.id, 'facility': location.id,
                  'include': 'plan', 'billing_cycle': 'hourly'}

        data = self.connection.request('/projects/%s/devices' %
                                       (self.nova_config.get('project_id')),
                                       params=params, method='POST')

        status = data.object.get('status', 'OK')
        if status == 'ERROR':
            message = data.object.get('message', None)
            error_message = data.object.get('error_message', message)
            raise ValueError('Failed to create node: %s' % (error_message))
        return self._to_node(data=data.object)