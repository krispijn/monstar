MonSTAR
=======
Monitoring Ship Traffic Automatically Remotely: a prototype for monitoring vessels and detecting suspicious behavior.

Description
-----------
This program is the prototype implentation of my the work done for my MSc Thesis at the Delft University of Technology titled:

	Detecting Suspicious Behavior in Marine Traffic using the Automatic Identification System
	
	
Keywords: 

* Suspicious behavior detection, 
* AIS, 
* Big data, 
* Cognitive modeling, 
* Operator support, 
* Marime traffic monitoring, 
* Bayesian network.

Requirements
------------
The program requires the following libraries:

* [jSMILE](http://genie.sis.pitt.edu/wiki/Introduction_to_jSMILE) - for Bayesian inference
* [Common Math](http://commons.apache.org/proper/commons-math/) - for some Math
* [MySQL JDBC Driver](http://dev.mysql.com/downloads/connector/j/) - for access to database with AIS information
* [PostgreSQL JDBC Driver](http://jdbc.postgresql.org/) - for access to geospatial database
* [Joda-Time](http://www.joda.org/joda-time/) - for time modeling in Java

License
-------
This program without its dependencies and its source code are released under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)