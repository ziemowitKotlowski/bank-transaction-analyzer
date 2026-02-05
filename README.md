# Bank Transaction Analyzer

A Spring Boot REST API for importing and analyzing transaction data using MongoDB's data aggregation framework.

## Overview

This application provides REST APIs to:
- Import transaction data from CSV files
- Track import job status
- Query and analyze transactions
- View transaction statistics

## Technology Stack

- **Java 21**
- **Spring Boot 4.0.2**
- **MongoDB** (for data storage)
- **Gradle** (build tool)
- **Docker & Docker Compose** (containerization)

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java 21**
- **Docker** and **Docker Compose**
- **Gradle** (or use the included Gradle wrapper)

## How to Run

### Option 1: Using Docker Compose (Recommended)

This is the intended way to run the application with all dependencies.

1. **Clone the repository**
   ```
   git clone https://github.com/ziemowitKotlowski/bank-transaction-analyzer
   cd bank-transaction-analyzer
   ```

2. **Set up environment variables**

   Copy the example credentials file and update with your desired values: (currently database is configured to run without authentication to simplify local development)
   ```
   cp secrets\credentials.env.example secrets\credentials.env
   ```

   Edit `secrets/credentials.env` if needed (default values work fine for testing purposes).

3. **Start the application**
   ```
   docker-compose up --build
   ```

   The application will be available at `http://localhost:8080`

4. **Stop the application**
   ```
   docker-compose down
   ```
### Option 2: Local development
   ```
   docker-compose -f docker-compose-local.yml up -d --build
   ```
   Create and populate application-local.yml

## API Endpoints

Once the application is running, you can use the [open-api collection](./doc/open-api-collection.json) to call those requests:

### Import Transactions
Use the sample data or generate your own using Mockaroo (see below) to test the import endpoint.
```
POST http://localhost:8080/api/imports
Content-Type: multipart/form-data
```

### Check Import Status
```
GET http://localhost:8080/api/imports/{importJobId}/status
```

### Statistics/Aggregations Endpoints
#### Most spent per attribute
Implemented filterBy options: YEAR_MONTH, CATEGORY
```
GET http://localhost:8080/api/stats/most-spent?filterBy=YEAR_MONTH&resultSize=3&currency=PLN
```
#### Balance by attribute
Implemented filterBy options: YEAR_MONTH, IBAN
```
GET http://localhost:8080/api/stats/balance?filterBy=YEAR_MONTH&value=2025-01&currency=PLN
```
More filter/aggregate options to be implemented by creating new mongodb aggregations similar to the existing ones.

## Sample Data

Sample CSV files are included in the project for testing:
- `src/main/resources/csv/sample-transactions-10.csv` (10 records) - small file with production-like data
- `src/main/resources/csv/sample-transactions-1000.csv` (1,000 records) - large file for testing with randomized data
- `src/main/resources/csv/sample-transactions-100-000.csv` (100,000 records) - very large file for performance testing

### Generate Custom CSV for Testing
You can generate custom CSV test data using Mockaroo:
- https://www.mockaroo.com/
- Clone this schema: https://www.mockaroo.com/schemas/clone?clone=1769347880002

## Development

### Code Formatting
This project uses Spotless for code formatting:

```
# Check formatting
.\gradlew spotlessCheck

# Apply formatting
.\gradlew spotlessApply
```
IntelliJ Spotless plugin is recommended

### Running Tests
```
.\gradlew test
```

## Configuration

- **Application properties**: `src/main/resources/application.yml`
- **Local profile (if needed)**: `src/main/resources/application-local.yml`
- **Max file upload size**: 10MB (configurable in `application.yml`)
- **MongoDB connection URI**: `secrets/credentials.env` Environment variables for database connection (used by Docker Compose)
