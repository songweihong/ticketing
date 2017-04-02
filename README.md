# Acme Ticket Service
The ticket service is a REST service which facilitates the discovery, temporary hold, and final reservation of seats within a high-demand performance venue.

# Assumptions:
* Venue size is configurable by row and column.
* There are 2 seat types in the venue: VIP and NORMAL. The VIP seat range are configurable by row and column.
* Any seat at a time is in one of these three states: AVAILABLE, HOLD or SOLD. Only seats in AVAILABLE state can be put on hold by a customer.
* If a reservation is not made within a certain time period after the on hold order is made, the seats on hold will be released and become available again.
* Reservations can only be made to the existing on hold orders.
* The customer always prefer VIP seats than the NORMAL seats, seat adjacency is a second priority.
* To simplify the implementation, all data are held in memory, and transactions are not considered.

# How to run:
* Build: gradlew.bat build
* Run: gradlew.bat build && java -jar build/libs/ticketing-1.0.0.jar
* Unit test: gradlew.bat test
* Unit & Integration test: gradlew.bat allTest

# How to configure:
* /resources/application.properties

# How to use:
The service is run on default port 8080. A swagger UI is provided for easy use: http://<host_name>:8080/ticketing/swagger-ui.html. The ticketing-controller handles all ticket service requests.
You may also use other tools, such as postman or curl, to access the service

# Examples:
* retrieve number of seats available: http://localhost:8080/ticketing/seats/count (GET)
* hold seats: http://localhost:8080/ticketing/seats/hold?numOfSeats=1&customerEmail=test@acme.com (POST)
* reserve seats: http://localhost:8080/ticketing/seats/reserve?seatHoldId=1&customerEmail=test@acme.com (POST)





