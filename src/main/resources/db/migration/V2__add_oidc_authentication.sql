ALTER TABLE users
    ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL' AFTER id,
    ADD COLUMN provider_subject VARCHAR(255) NULL AFTER provider,
    ADD COLUMN last_login_at DATETIME NULL AFTER created_at;

ALTER TABLE users
    MODIFY COLUMN email VARCHAR(255) NULL;

UPDATE users
SET provider = 'LOCAL',
    provider_subject = CONCAT('local:', id),
    last_login_at = created_at
WHERE provider_subject IS NULL;

ALTER TABLE users
    MODIFY COLUMN provider_subject VARCHAR(255) NOT NULL;

ALTER TABLE users
    DROP INDEX uk_users_email;

ALTER TABLE users
    ADD CONSTRAINT uk_users_provider_subject UNIQUE (provider, provider_subject);
