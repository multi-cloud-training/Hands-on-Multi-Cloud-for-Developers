output "NextSteps" {
  value = <<EOF
ssh ubuntu@${aws_instance.halyard_and_spinnaker_server.public_ip}
 
hal config features edit --artifacts true
hal config version edit --version 1.7.6

hal config deploy edit --type localdebian

## To run without worrying about passwords and auth - VERY dangerous, not recommended particularly with a public IP.  Would be far better to either enable auth or run spinnaker via a bastion or similar.
echo "host: 0.0.0.0" | tee \
    ~/.hal/default/service-settings/gate.yml \
    ~/.hal/default/service-settings/deck.yml

hal config provider aws account add development --account-id 12341234123 --regions us-east-1,us-east-2 --assume-role role/SpinnakerCrossAccountRole
sudo hal deploy apply

## FOR spinnaker to discover the various subnets in any given account (and thus the VPCs), Spinnaker looks for a very specific set of naming conventions and tags.  https://docs.armory.io/install-guide/subnets/ has information on this.


## IF You want to start going a distributed route, that's more complicated.  You'll have to have a shared location to set config files (e.g. below) and setup many more settings.  The above SHOULD get you mostly started.  Note account-id can be the SAME account as you're currently in.  
hal config storage s3 edit --region us-east-1 --bucket cfx-operations-production-state-files --root-folder spinnaker
hal config storage edit --type s3


EOF
}
