-- Create password_reset_requests table
CREATE TABLE password_reset_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for token lookup (most common query)
CREATE INDEX idx_password_reset_token ON password_reset_requests(token);

-- Create composite index for checking active requests per user
CREATE INDEX idx_password_reset_user_active ON password_reset_requests(user_id, used, expires_at);

-- Add comments for documentation
COMMENT ON TABLE password_reset_requests IS 'Stores password reset tokens with expiration and usage tracking';
COMMENT ON COLUMN password_reset_requests.token IS 'UUID-based unique reset token';
COMMENT ON COLUMN password_reset_requests.expires_at IS 'Token expiration timestamp (30 minutes from creation)';
COMMENT ON COLUMN password_reset_requests.used IS 'Flag indicating if token has been used (single-use enforcement)';
COMMENT ON COLUMN password_reset_requests.used_at IS 'Timestamp when token was used for password reset';
