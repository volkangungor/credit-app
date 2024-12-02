# Credit App

This is a Spring Boot application for managing customer loans.

## Running the Application

To run the application, use the following command:

```bash
./gradlew bootRun
```

To build the application, use the following command:

```bash
./gradlew build
```

To run the tests, use the following command:

```bash
./gradlew test
```

You can check the `src/main/resources/data.sql` file and see that there are two customers created for testing. The IDs of these customers are 100 and 200.
And `src/main/resources/schema.sql` file contains a few constraints for the in memory h2 database.

## API Documentation

You can browse the APIs using Swagger UI. The Swagger UI is available at:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

"Token Generator" APIs does not have authentication. You can use these APIs to generate a token for admin and customers.
These tokens can be used for the "Customer Loan Operations" APIs which require authentication.

All codes related with rest api (Rest Controllers, ControllerAdvice etc.) can be found under the `com.volco.creditapp.api.rest` package
Security related codes and other application specific configurations can be found under the `com.volco.creditapp.application` package
Business models and services can be found under the `com.volco.creditapp.domain` package
All codes related with database (DB Entities, Repositories etc.) can be found under the `com.volco.creditapp.persistence` package

## H2 Database Console

The H2 database console is available at:

[http://localhost:8080/h2-console](http://localhost:8080/h2-console)

Use this console to interact with the in-memory H2 database.
Use `jdbc:h2:mem:testdb` as JDBC URL