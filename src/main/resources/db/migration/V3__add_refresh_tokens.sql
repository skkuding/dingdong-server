CREATE TABLE refresh_tokens (
       id BIGINT NOT NULL AUTO_INCREMENT,
       user_id BIGINT NOT NULL,
       token_hash CHAR(64) NOT NULL,
       expires_at DATETIME NOT NULL,
       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
       revoked_at DATETIME NULL,
       replaced_by_token_hash CHAR(64) NULL,
       PRIMARY KEY (id),
       CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
       CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
       INDEX idx_refresh_tokens_user_id (user_id)
);
