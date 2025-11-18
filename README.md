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

## Getting Started

### Database Setup

#### Option 1: Start New Database

If this is your first time or you want a fresh database:

```bash
docker run --name password-reset-db \
  -e POSTGRES_DB=password_reset \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin123 \
  -p 5432:5432 \
  -d postgres:15-alpine
```

#### Option 2: Start Existing Database

If you already have the database container:

```bash
# Check if database is running
docker ps | grep password-reset-db

# If not running, start it
docker start password-reset-db
```

#### Option 3: Fresh Start - Clean Database

If you want to completely reset and start fresh:

```bash
# Stop and remove the database container and remove all data
docker stop password-reset-db
docker rm password-reset-db

# Start fresh database
docker run --name password-reset-db \
  -e POSTGRES_DB=password_reset \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin123 \
  -p 5432:5432 \
  -d postgres:15-alpine
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