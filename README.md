# Password Reset Service

A secure RESTful API service built with Kotlin and Spring Boot for handling password reset requests with time-limited, single-use tokens.

## Technology Stack

- Kotlin 1.9.25 + Spring Boot 3.5.7
- PostgreSQL 15 + Spring Data JPA
- BCrypt password hashing
- Flyway migrations
- Gradle (Kotlin DSL)

## Prerequisites

- Java 17+
- Docker (for PostgreSQL)

## Quick Start

### 1. Start PostgreSQL
```bash
docker run --name password-reset-db \
  -e POSTGRES_DB=password_reset \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin123 \
  -p 5432:5432 \
  -d postgres:15-alpine
```

### 2. Run Application
```bash
./gradlew bootRun
```

### 3. Create Test User
```bash
docker exec password-reset-db psql -U admin -d password_reset -c \
  "INSERT INTO users (email, password_hash, created_at, updated_at) VALUES ('user@example.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);"
```

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

**Response** (200):
```json
{"message": "Password successfully reset"}
```

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
