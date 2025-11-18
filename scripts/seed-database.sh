#!/bin/bash

# Database Seeding Script
# Seeds the database with test users for development/testing

set -e  # Exit on error

# Configuration
DB_CONTAINER="password-reset-db"
DB_NAME="password_reset"
DB_USER="admin"
SEED_FILE="src/main/resources/db/seed/test-users.sql"

echo "🌱 Seeding database with test users..."

# Check if Docker container is running
if ! docker ps | grep -q "$DB_CONTAINER"; then
    echo "❌ Error: Database container '$DB_CONTAINER' is not running"
    echo "   Start it with: docker start $DB_CONTAINER"
    exit 1
fi

# Check if seed file exists
if [ ! -f "$SEED_FILE" ]; then
    echo "❌ Error: Seed file not found: $SEED_FILE"
    exit 1
fi

# Copy seed file to container and execute
docker cp "$SEED_FILE" "$DB_CONTAINER:/tmp/seed.sql"
docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -f /tmp/seed.sql

if [ $? -eq 0 ]; then
    echo "✅ Database seeded successfully!"
    echo ""
    echo "Test user created:"
    echo "  📧 user@example.com / password123"
else
    echo "❌ Error: Failed to seed database"
    exit 1
fi
