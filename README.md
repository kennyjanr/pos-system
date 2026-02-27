# Point of Sale (POS) System

A minimal, clean monorepo for a Point of Sale system built with Spring Boot, React, and PostgreSQL.

## Project Structure

```
pos-system/
├── backend/              # Spring Boot backend (Java 17, Maven)
├── frontend/             # Vite + React + TypeScript + Tailwind frontend
├── docker-compose.yml    # Docker Compose configuration
├── .env.example          # Environment variables template
├── .gitignore           # Git ignore rules
├── .editorconfig        # Editor configuration
└── README.md            # This file
```

## Quick Start

### Prerequisites

- Docker & Docker Compose (recommended)
- OR: Java 17+, Node.js 18+, PostgreSQL 16

### Option 1: Using Docker Compose (Recommended)

```bash
cp .env.example .env
docker-compose up --build
```

Access:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api
- Backend Swagger UI: http://localhost:8080/swagger-ui.html
- Database: localhost:5432

### Option 2: Local Development

#### Backend

```bash
cd backend
cp ../.env.example .env  # Set up environment variables

# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# OR if Maven is installed
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`

#### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`

#### Database

Make sure PostgreSQL is running and accessible at the values specified in `.env`:

```bash
# macOS with Homebrew
brew services start postgresql@16

# or Docker
docker run -d \
  --name postgres-pos \
  -e POSTGRES_DB=pos_db \
  -e POSTGRES_USER=pos_user \
  -e POSTGRES_PASSWORD=pos_password \
  -p 5432:5432 \
  postgres:16
```

## Available Endpoints

### Health Check

```bash
GET http://localhost:8080/api/health
```

Response:
```json
{
  "status": "UP"
}
```

### API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Environment Variables

See `.env.example` for all available configuration options.

Key variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` - Database configuration
- `SERVER_PORT` - Backend server port (default: 8080)
- `VITE_API_BASE_URL` - Frontend API endpoint (default: http://localhost:8080/api)

## Development

### Backend

- **Framework**: Spring Boot 3
- **Language**: Java 17
- **Build Tool**: Maven 3.9.6
- **Database**: PostgreSQL 16

See [backend/README.md](backend/README.md) for more details.

### Frontend

- **Framework**: Vite + React 18
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios

See [frontend/README.md](frontend/README.md) for more details.

## Building for Production

### Backend

```bash
cd backend
./mvnw clean package
java -jar target/pos-system-1.0.0.jar
```

### Frontend

```bash
cd frontend
npm install
npm run build
```

Production build output in `dist/` directory.

## Docker

Build and run individually:

```bash
# Backend
docker build -t pos-backend ./backend
docker run -p 8080:8080 pos-backend

# Frontend
docker build -t pos-frontend ./frontend
docker run -p 5173:5173 pos-frontend
```

Or use Docker Compose for the full stack (recommended).

## Architecture

This is a **Phase 0 scaffold** with minimal configuration and no business logic:

- Backend provides only a health check endpoint and dependency setup
- Frontend has a placeholder home page and Tailwind CSS styling
- Layered architecture ready for feature implementation
  - Controllers for HTTP endpoints
  - Services for business logic
  - Repositories for data access
  - DTOs and Entities for data modeling
  - Config for Spring configuration
  - Exception handlers for error handling

## Next Steps (Phase 1+)

- Product management (CRUD)
- Inventory tracking
- Sales transactions
- User authentication & authorization
- Dashboard & analytics
- Reports

## Contributing

Follow the existing code structure and style conventions defined in `.editorconfig`.

## License

MIT
