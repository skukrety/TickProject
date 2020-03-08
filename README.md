#TickProject

- How to run the project (preferably with maven Spring Boot plugin or alike, or as an executable jar)
The application has been built using Spring Boot and Maven.
Scheduler has been used to compute statistics in background and keep data ready for constant time consumption.

To build the JAR use below command (this will create a jar file):

    mvn clean package

Follow below instructions to run application:
	- Open command prompt
	- Goto TickProject home folder
	- Run below command
	  mvn spring-boot:run
	- Test using SOAP UI or Postman

API urls:
http://localhost:8080/ticks
http://localhost:8080/statistics
http://localhost:8080/statistics/<INSTRUMENT_IDENTIFIER>	


- Which assumptions you made while developing

Ticks are maintained only for the sliding window interval (60 sec)
Both Statistics API will return empty statistics object (avg, max, min & count as 0) if no ticks available in sliding window interval
Unsecured API with no Authentication or Authorization
Exception handling is done for minimum cases
Running scheduler every second to compute statistics in background


- What would you improve if you had more time

Improve code coverage
Better exception handling
Some part of code could have been better optimized


- And, whether you liked the challenge or not 😊

I really liked the challenge its a small but nice project covering many java fundamentals, 
tried to cover many aspects of multi-threading 
by telling to maintain consolidated statistics and statistics per instrument.
In memory and Constant time complexity are good challenges for collections APIs 

