# Phase 1: Authentication Module

This Phase 1 implementation provides complete authentication and authorization for the POS System backend.

## Features Implemented

✅ **User Registration** - Sign up with username, email, password  
✅ **User Login** - Authenticate with username or email  
✅ **JWT Access Tokens** - 15-minute expiry tokens (configurable)  
✅ **Refresh Tokens** - 30-day expiry, HTTP-only cookies, revocable  
✅ **Role-Based Access Control** - ROLE_ADMIN, ROLE_MANAGER, ROLE_CASHIER  
✅ **Password Hashing** - BCrypt with salt  
✅ **Protected Endpoints** - JWT authentication filter  
✅ **CORS** - Configured for frontend origin  

## Environment Variables Required

Set these in `.env` or export them:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=pos_db
DB_USER=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-at-least-32-character-secret-key-here
ACCESS_TOKEN_EXPIRY_SECONDS=900           # 15 minutes
REFRESH_TOKEN_EXPIRY_DAYS=30              # 30 days

# CORS
FRONTEND_ORIGIN=http://localhost:5173

# Server
SERVER_PORT=8080
```

## API Endpoints

### Public Endpoints

#### 1. Sign Up
```bash
POST /api/auth/signup
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePassword123!"
}

# Response (201 Created)
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_MANAGER"]
}
```

#### 2. Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "SecurePassword123!"
}

# Response (200 OK)
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}

# Cookie Set (HTTP-only)
Set-Cookie: refresh_token=<token>; HttpOnly; Path=/api/auth; Max-Age=2592000
```

#### 3. Refresh Token
```bash
POST /api/auth/refresh
Cookie: refresh_token=<refresh_token>

# Response (200 OK)
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}

# New refresh cookie set
Set-Cookie: refresh_token=<new_token>; HttpOnly; Path=/api/auth; Max-Age=2592000
```

#### 4. Logout
```bash
POST /api/auth/logout
Cookie: refresh_token=<refresh_token>

# Response (200 OK)
```

### Protected Endpoints

#### 5. Get Current User
```bash
GET /api/auth/me
Authorization: Bearer <access_token>

# Response (200 OK)
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_MANAGER"]
}
```

## Testing with curl

### 1. Start Backend
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # macOS
cd /Users/kennyroxas/Projects/pos-system/backend
java -jar target/pos-system-1.0.0.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/pos_db \
  --spring.datasource.username=postgres \
  --spring.datasource.password='Uldcp-im0809' \
  --server.port=8080 \
  --JWT_SECRET='my-super-secret-key-at-least-32-characters-long!'
```

### 2. Sign Up
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@posystem.local",
    "password": "AlicePassword123!"
  }'
```

### 3. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "alice",
    "password": "AlicePassword123!"
  }' \
  -c /tmp/cookies.txt

# Save the accessToken from the response
export ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Access Protected Endpoint
```bash
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8080/api/auth/me
```

### 5. Refresh Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -b /tmp/cookies.txt
```

### 6. Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b /tmp/cookies.txt
```

## Running Tests

###  Unit Tests (AuthService)
```bash
cd backend
bash ./mvnw test -Dtest=AuthServiceTest
```

### Integration Tests (Full Auth Flow)
```bash
cd backend
bash ./mvnw test -Dtest=AuthIntegrationTest
```

### All Tests
```bash
cd backend
bash ./mvnw test
```

## Project Structure

```
backend/
├── pom.xml                                      # Maven config with JWT deps
├── src/main/java/com/example/pos/
│   ├── entity/
│   │   ├── User.java                           # User with roles
│   │   ├── Role.java                           # ROLE_ADMIN, ROLE_MANAGER, ROLE_CASHIER
│   │   └── RefreshToken.java                   # Revocable refresh tokens
│   ├── repository/
│   │   ├── UserRepository.java                 # findByUsername, findByEmail
│   │   ├── RoleRepository.java                 # findByName
│   │   └── RefreshTokenRepository.java         # findByToken, deleteByUserId
│   ├── dto/
│   │   ├── SignupRequest.java
│   │   ├── LoginRequest.java
│   │   ├── AuthResponse.java
│   │   └── UserResponse.java
│   ├── security/
│   │   ├── JwtService.java                     # Generate & validate JWTs
│   │   ├── JwtAuthenticationFilter.java        # Extract token from header
│   │   └── CustomUserDetailsService.java       # Load UserDetails from DB
│   ├── service/
│   │   └── AuthService.java                    # register, login, refresh, logout
│   ├── controller/
│   │   ├── HealthController.java               # /api/health
│   │   └── AuthController.java                 # /api/auth/*
│   └── config/
│       ├── SecurityConfig.java                 # Spring Security chain, BCrypt
│       └── DataInitializer.java                # Create roles at startup
├── src/test/java/com/example/pos/
│   ├── service/
│   │   └── AuthServiceTest.java                # Unit tests
│   └── controller/
│       └── AuthIntegrationTest.java            # E2E auth flow tests
└── src/main/resources/
    └── application.yml                          # Spring config with JWT placeholders
```

## Security Notes

- Passwords are hashed with BCrypt (never stored plain text)
- JWT Secret must be at least 32 characters (environment variable required)
- Refresh tokens are stored in database and can be revoked
- HTTP-only cookies prevent XSS token theft
- CORS restricted to FRONTEND_ORIGIN
- Access tokens expire in 15 minutes (configurable)
- Refresh tokens expire in 30 days (configurable)
- Spring Security secures all endpoints except /api/auth/**, /api/health, and Swagger docs

## First User Special Behavior

The first user registered gets `ROLE_ADMIN`. All subsequent users get `ROLE_MANAGER` by default. This can be changed via a role management endpoint in future phases.

## Next Steps (Future Phases)

- Phase 2: Product Management
- Phase 3: Inventory Tracking
- Phase 4: Sales / Transactions
- Phase 5: Reporting & Analytics
