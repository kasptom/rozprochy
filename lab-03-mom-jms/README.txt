Config:
• openjms-0.7.7-beta-1
• in config directory, modify file openjms.xml so that it contains proper topics and queue (compare with exemplary openjms.xml)
• in config directory - openjms.policy should comply with exemplary .policy file
• check whether Your system's JAVA_HOME and the PATH are correctly defined

How to run it?

First of all start JMS provider - 
go to openjms-0.7.7-beta-1/bin/startup.bat(.sh)

The applicataion is divided into three smaller ones. Each of them should be ran in separate terminals.
Go to bin/ directory and type in the separate terminals
java -jar generators.jar <number_of_generators>
java -jar solvers.jar <number_of_solvers>
java -jar clients.jar <number_of_clients>

To stop the applications type in each terminal /stop
cf. mnk_scheme.png to learn more about the application's structure

