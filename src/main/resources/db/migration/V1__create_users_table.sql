-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for email lookup
CREATE INDEX idx_users_email ON users(email);

-- Add comment for documentation
COMMENT ON TABLE users IS 'Stores user credentials and identity information';
COMMENT ON COLUMN users.email IS 'Unique email address used for user identification';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password, never stored in plaintext';
