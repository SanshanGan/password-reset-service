-- Seed data for test user
-- This script is idempotent - safe to run multiple times

INSERT INTO users (email, password_hash, created_at, updated_at)
VALUES ('user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;
