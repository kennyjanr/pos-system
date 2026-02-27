# POS System Backend

Spring Boot 3 backend for the Point of Sale System.

## Prerequisites

- Java 17+
- PostgreSQL 16

## Running Locally

### Option 1: Using Maven Wrapper (recommended)

```bash
cd backend
./mvnw spring-boot:run
```

### Option 2: Using Maven (if installed)

```bash
cd backend
mvn spring-boot:run
```

### Option 3: Build and Run JAR

```bash
cd backend
./mvnw clean package
java -jar target/pos-system-1.0.0.jar
```

## Database Setup

Configure these environment variables in `.env` or your environment:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=pos_db
DB_USER=pos_user
DB_PASSWORD=pos_password
SERVER_PORT=8080
```

The application uses Hibernate with `ddl-auto: update`, so tables are created automatically on startup.

## API Endpoints

- Health Check: `GET http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## Running with Docker Compose

From the root directory:

```bash
docker-compose up --build
```

This will start PostgreSQL and the backend service. The backend will wait for the database to be ready before starting.
