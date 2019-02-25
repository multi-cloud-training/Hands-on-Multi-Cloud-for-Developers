function setcloudname() {
  var host = location.hostname ;
  var cloud_name = 'Cloud' ;

  var result_aws = host.indexOf( 'elasticbeanstalk' );
  var result_azure = host.indexOf( 'azure' );
  var result_gcp = host.indexOf( 'appspot' );
  var result_predix = host.indexOf( 'predix' );
  var result_ibm = host.indexOf( 'bluemix' );

  if( result_aws > 0 ) cloud_name = 'AWS';
  if( result_azure > 0 ) cloud_name = 'Azure';
  if( result_gcp > 0 ) cloud_name = 'GCP';
  if( result_predix > 0 ) cloud_name = 'Predix';
  if( result_ibm > 0 )  cloud_name = 'Bluemix';

  document.getElementById("area_cn").innerHTML = '<span data-feather="cloud"></span>' + cloud_name ;
}

setcloudname();
