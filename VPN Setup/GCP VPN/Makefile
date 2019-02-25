SHELL := /bin/bash

TFVARS_FILE := terraform.tfvars
TFSTATE_FILE := terraform.tfstate
VPN_CONFIG_FILE := .customer_config.xml

TERRAFORM_BIN := $(shell command -v terraform 2>/dev/null)
JQ_BIN := $(shell command -v jq 2> /dev/null)
XMLLINT_BIN := $(shell command -v xmllint 2> /dev/null)

all:
	@make apply
	@make update_vars
	@make apply

local_deps:
ifndef TERRAFORM_BIN
	$(error missing terraform, please install)
endif
ifndef JQ_BIN
	$(error missing jq, please install)
endif
ifndef XMLLINT_BIN
	$(error missing xmllint, please install)
endif

update_vars: local_deps $(VPN_CONFIG_FILE)
	@echo "INFO: Updating tfvars file"
	@touch $(TFVARS_FILE)
	@KEY=TUN1_VPN_GW_ASN VALUE="$(shell xmllint --xpath "//ipsec_tunnel[1]/vpn_gateway/bgp/asn/text()" $(VPN_CONFIG_FILE))" && \
		grep -q $$KEY $(TFVARS_FILE) && sed -i -e 's/^\('$$KEY'.*=\)\(.*\)/\1 "'$$VALUE'"/g' $(TFVARS_FILE) || echo "$$KEY = \"$$VALUE\"" >> $(TFVARS_FILE)
	
	@KEY=TUN1_CUSTOMER_GW_INSIDE_IP VALUE="$(shell xmllint --xpath "//ipsec_tunnel[1]/customer_gateway/tunnel_inside_address/ip_address/text()" $(VPN_CONFIG_FILE))" && \
		grep -q $$KEY $(TFVARS_FILE) && sed -i -e 's/^\('$$KEY'.*=\)\(.*\)/\1 "'$$VALUE'"/g' $(TFVARS_FILE) || echo "$$KEY = \"$$VALUE\"" >> $(TFVARS_FILE)
	
	@KEY=TUN1_CUSTOMER_GW_INSIDE_NETWORK_CIDR VALUE="$(shell xmllint --xpath "//ipsec_tunnel[1]/customer_gateway/tunnel_inside_address/network_cidr/text()" $(VPN_CONFIG_FILE))" && \
		grep -q $$KEY $(TFVARS_FILE) && sed -i -e 's/^\('$$KEY'.*=\)\(.*\)/\1 "'$$VALUE'"/g' $(TFVARS_FILE) || echo "$$KEY = \"$$VALUE\"" >> $(TFVARS_FILE)
	
	@KEY=TUN1_VPN_GW_INSIDE_IP VALUE="$(shell xmllint --xpath "//ipsec_tunnel[1]/vpn_gateway/tunnel_inside_address/ip_address/text()" $(VPN_CONFIG_FILE))" && \
		grep -q $$KEY $(TFVARS_FILE) && sed -i -e 's/^\('$$KEY'.*=\)\(.*\)/\1 "'$$VALUE'"/g' $(TFVARS_FILE) || echo "$$KEY = \"$$VALUE\"" >> $(TFVARS_FILE)


	@KEY=TUN2_VPN_GW_ASN VALUE="$(shell xmllint --xpath "//ipsec_tunnel[2]/vpn_gateway/bgp/asn/text()" $(VPN_CONFIG_FILE))" && \
		grep -q $$KEY $(TFVARS_FILE) && sed -i -e 's/^\('$$KEY'.*=\)\(.*\)/\1 "'$$VALUE'"/g' $(TFVARS_FILE) || echo "$$KEY = \"$$VALUE\"" >> $(TFVARS_FILE)
	
	@KEY=TUN2_CUSTOMER_GW_INSIDE_IP VALUE="$(shell xmllint --xpath "//ipsec_tunnel[2]/customer_gateway/tunnel_inside_address/ip_address/text()" $(VPN_CONFIG_FILE))" && \
		grep -q $$KEY $(TFVARS_FILE) && sed -i -e 's/^\('$$KEY'.*=\)\(.*\)/\1 "'$$VALUE'"/g' $(TFVARS_FILE) || echo "$$KEY = \"$$VALUE\"" >> $(TFVARS_FILE)
	
	@KEY=TUN2_CUSTOMER_GW_INSIDE_NETWORK_CIDR VALUE="$(shell xmllint --xpath "//ipsec_tunnel[2]/customer_gateway/tunnel_inside_address/network_cidr/text()" $(VPN_CONFIG_FILE))" && \
		grep -q $$KEY $(TFVARS_FILE) && sed -i -e 's/^\('$$KEY'.*=\)\(.*\)/\1 "'$$VALUE'"/g' $(TFVARS_FILE) || echo "$$KEY = \"$$VALUE\"" >> $(TFVARS_FILE)
	
	@KEY=TUN2_VPN_GW_INSIDE_IP VALUE="$(shell xmllint --xpath "//ipsec_tunnel[2]/vpn_gateway/tunnel_inside_address/ip_address/text()" $(VPN_CONFIG_FILE))" && \
		grep -q $$KEY $(TFVARS_FILE) && sed -i -e 's/^\('$$KEY'.*=\)\(.*\)/\1 "'$$VALUE'"/g' $(TFVARS_FILE) || echo "$$KEY = \"$$VALUE\"" >> $(TFVARS_FILE)

$(VPN_CONFIG_FILE): local_deps
	@rm -f $@
	@echo "Extracting config from $(TFSTATE_FILE)"
	@jq -r '.modules[].resources["aws_vpn_connection.aws-vpn-connection1"].primary.attributes.customer_gateway_configuration' $(TFSTATE_FILE) > $@
	@xmllint --format $@ >/dev/null 2>&1 ; if [[ $$? -ne 0 ]]; then echo "ERROR: Failed to extract customer xml config from $(TFSTATE_FILE), did 'terraform apply' run completely?" && exit 1; fi

plan: local_deps
	terraform plan

apply: local_deps
	terraform apply

.PHONY: apply local_deps update_vars 

clean:
	rm -f $(VPN_CONFIG_FILE)