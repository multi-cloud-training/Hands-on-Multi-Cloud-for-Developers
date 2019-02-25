
echo "Starting Tomcat Standalone Server"
echo "brew services start tomcat"
brew services start tomcat


./gradlew clean build

cp build/libs/multi-deployment-0.0.1-SNAPSHOT.war /usr/local/Cellar/tomcat/9.0.14/libexec/webapps/ROOT.war
