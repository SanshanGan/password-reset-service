# Password Reset Service

A secure RESTful API service built with Kotlin and Spring Boot for handling password reset requests with time-limited, single-use tokens.

## Technology Stack

- Kotlin 1.9.25 + Spring Boot 3.5.7
- PostgreSQL 15 + Spring Data JPA
- BCrypt password hashing
- Flyway migrations
- Gradle (Kotlin DSL)

## Project Structure

```
src/main/kotlin/com/sanshan/passwordresetservice/
├── config/
│   ├── SecurityConfig.kt              # Security configuration
│   └── OpenApiConfig.kt                # API documentation setup
├── controller/
│   └── PasswordResetController.kt      # REST API endpoints
├── dto/
│   ├── InitiatePasswordResetRequest.kt
│   ├── ExecutePasswordResetRequest.kt
│   ├── PasswordResetResponse.kt
│   └── ErrorResponse.kt
├── entity/
│   ├── User.kt                         # User domain model
│   └── PasswordResetRequest.kt         # Reset request domain model
├── exception/
│   ├── GlobalExceptionHandler.kt       # Centralized error handling
│   └── PasswordResetException.kt       # Custom exceptions
├── repository/
│   ├── UserRepository.kt               # User data access
│   └── PasswordResetRequestRepository.kt
├── service/
│   ├── PasswordResetService.kt         # Business logic interface
│   ├── PasswordResetServiceImpl.kt     # Business logic implementation
│   └── PasswordResetResult.kt          # Service result wrapper
└── util/
    ├── EmailValidator.kt               # Email validation
    └── TokenGenerator.kt               # Token generation
```

**Architecture:**
- **3-Layer Architecture**: Controller → Service → Repository
- **Domain-Driven Design**: Entities represent core business concepts
- **DTO Pattern**: Separate API contracts from domain models
- **Centralized Error Handling**: GlobalExceptionHandler for consistent error responses

## Prerequisites

- Java 17+
- Docker (for PostgreSQL)

## Getting Started

### Database Setup

#### Step 1: Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

The `.env` file contains database credentials (already set with defaults for local development).

#### Step 2: Start Database

Load environment variables and start the database:

```bash
# Load environment variables
source .env

# Start database using environment variables
docker run --name password-reset-db \
  -e POSTGRES_DB=$DB_NAME \
  -e POSTGRES_USER=$DB_USER \
  -e POSTGRES_PASSWORD=$DB_PASSWORD \
  -p $DB_PORT:5432 \
  -d postgres:15-alpine
```

#### Managing the Database

**Start existing database:**
```bash
docker start password-reset-db
```

**Stop database:**
```bash
docker stop password-reset-db
```

**Reset database (clean start):**
```bash
docker stop password-reset-db
docker rm password-reset-db
# Then run Step 2 again
```

### Run Application

Start the application (this will create database tables via Flyway migrations):

```bash
./gradlew bootRun
```

### Seed Database with Test Users

After the application has started at least once, seed the database with test users:

```bash
./scripts/seed-database.sh
```

This creates a test user:
- `user@example.com` / `password123`

The script is **idempotent** - safe to run multiple times without creating duplicates.

## API Endpoints

### POST /api/password-reset/initiate
Generates a reset token for a user.

**Request**:
```json
{"email": "user@example.com"}
```

**Response** (200):
```json
{
  "resetToken": "550e8400-e29b-41d4-a716-446655440000",
  "expiresAt": "2025-11-11T15:30:00"
}
```

### POST /api/password-reset/execute
Resets password using a valid token.

**Request**:
```json
{
  "resetToken": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewSecureP@ssw0rd"
}
```

**Response** (204 No Content):
No response body. Success indicated by 204 status code.

## Testing

Import Postman collection from `postman/Password-Reset-Service.postman_collection.json`

## Running Tests

```bash
./gradlew test
```

## Building

```bash
./gradlew build
```

## Security Features

- **BCrypt password hashing** (10 rounds) for user passwords
- **BCrypt token hashing** - Reset tokens are hashed before storage in the database
    - Raw tokens are returned in API response (for exercise purposes)
    - Only hashed tokens are stored in the database
    - Protects against token theft if database is compromised
- **UUID-based single-use tokens** - Each token can only be used once
- **30-minute token expiration** - Tokens automatically expire after 30 minutes
- **One active reset request per user** - Prevents spam and abuse
- **Email format validation** - Validates email addresses before processing