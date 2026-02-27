#!/bin/bash

# POS System Backend Startup Script
# Sets environment variables and runs Spring Boot

export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=pos_db
export DB_USER=postgres
export DB_PASSWORD='Uldcp-im0809'
export SERVER_PORT=8080

cd "$(dirname "$0")/backend"

echo "Starting POS System Backend..."
echo "Database: $DB_USER@$DB_HOST:$DB_PORT/$DB_NAME"
echo "Server Port: $SERVER_PORT"
echo "---"

./mvnw spring-boot:run
