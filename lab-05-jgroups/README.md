#### Requirements:
Maven 3.3+ and Java 1.8

#### How to run it? 
######1) Make it impossible for the application to communicate with IPv6 only hosts
    * Windows:  set MAVEN_OPTS="-Djava.net.preferIPv4Stack=true"
    * Linux: export MAVEN_OPTS="-Djava.net.preferIPv4Stack=true"
######2) run the application
    mvn exec:java