
# Currency Exchange Rates API

This project is a Spring Boot application that provides a REST API for managing currencies and retrieving their exchange rates. The project demonstrates integration with an external exchange rate provider, uses PostgreSQL for data storage, and includes scheduled updates of exchange rates.

## Features

- **Get list of currencies**: Retrieve a list of all currencies and their exchange rates.
- **Get exchange rate for a specific currency**: Retrieve the exchange rate for a specific currency code.
- **Add a new currency**: Add a new currency and its exchange rate to the system (requires user identification).
- **Scheduled exchange rate updates**: Exchange rates are automatically updated based on a cron schedule.

## Technologies Used

- **Java**: Version 17 (configured via toolchain).
- **Spring Boot**: For application setup and REST API.
- **PostgreSQL**: Database for storing exchange rates.
- **Liquibase**: To manage database schema changes.
- **Gradle**: For building the project.
- **Docker**: PostgreSQL is run in a Docker container.
- **Docker Compose**: To orchestrate PostgreSQL and other services.
- **JUnit 5 & Spring Test Framework**: For unit and functional testing.
- **Scheduled Tasks**: Implemented with Spring's `@Scheduled` annotation.

## Requirements

- **Java 17+** (configured via the Gradle toolchain)
- **Gradle**
- **Docker & Docker Compose**

## Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd <repository-directory>
   ```

2. **Build the project**:
   ```bash
   ./gradlew build
   ```

3. **Run Docker Compose to start PostgreSQL**:
   ```bash
   docker-compose up -d
   ```

4. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

5. **API Endpoints**:

   - **GET /api/currencies**: Retrieve the list of all available currencies and their exchange rates.
   - **GET /api/currencies/{code}**: Retrieve the exchange rate for a specific currency (returns a `404` error if the currency is not found).
   - **POST /api/currencies**: Add a new currency and its exchange rate (requires `code`, `rate`, and `userId` parameters).

### Example Responses

- **Get all currencies**:
   ```json
   {
      "FJD": 2.456283,
      "MXN": 21.977483,
      "LVL": 0.667992
      ...
   }
   ```

- **Get exchange rate for a specific currency**:
   ```json
   {
      "USD": 1.00
   }
   ```

6. **Testing**:
   To run the tests:
   ```bash
   ./gradlew test
   ```

## Configuration

- **External Exchange Rate API**: The application integrates with `exchangeratesapi.io` to fetch exchange rates. You will need an API key.
- Add the API key in the `application.yml` file:
   ```yaml
   exchangerate:
     api-key: YOUR_API_KEY
   ```

## Gradle Configuration

This project uses Gradle for building and managing dependencies. Below is a brief overview of the main sections of the `build.gradle` file:

```groovy
plugins {
	id 'java'
	id 'org.springframework.boot' version '3.3.2'
	id 'io.spring.dependency-management' version '1.1.6'
}

group = 'com.task'
version = '0.0.1-SNAPSHOT'

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jdbc'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.liquibase:liquibase-core'
	implementation 'org.postgresql:postgresql'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

	compileOnly 'org.projectlombok:lombok:1.18.34'
	annotationProcessor 'org.projectlombok:lombok:1.18.34'
	testCompileOnly 'org.projectlombok:lombok:1.18.34'
	testAnnotationProcessor 'org.projectlombok:lombok:1.18.34'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

### Main Points

- **Java Toolchain**: Configured to use Java 17.
- **Spring Boot Starter Dependencies**: Includes `spring-boot-starter-data-jdbc` for database access and `spring-boot-starter-web` for REST API support.
- **Liquibase**: Used for managing database migrations.
- **PostgreSQL**: Driver for PostgreSQL database integration.
- **JUnit 5**: Configured for testing.
- **Lombok**: For reducing boilerplate code in models and services.

## Scheduled Tasks

The application schedules a task to retrieve exchange rates from the external API every hour using the cron job. The results are logged in the database and stored in an in-memory `Map` for fast access.

Cron schedule can be configured via `application.yml`:
```yaml
scheduling:
  currency-update:
    cron: "0 0 * * * *"  # Runs every hour
```

Example of the scheduled task implementation in the service:

```java
@Override
@Scheduled(cron = "${scheduling.currency-update.cron}")
@Transactional
public void updateExchangeRates() {
    // Logic for updating exchange rates
}
```

## Exception Handling

- **404 - Resource Not Found**: If a currency is not found when retrieving an exchange rate, the application throws a `ResourceNotFoundException`.

## Notes

- No input/output validation is implemented, and error handling is done through runtime exceptions.
- The project does not use Spring Security Framework.
