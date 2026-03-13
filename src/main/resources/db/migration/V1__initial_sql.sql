-- src/main/resources/db/migration/V1__init.sql

CREATE TABLE users (
       id BIGINT NOT NULL AUTO_INCREMENT,
       email VARCHAR(255) NOT NULL,
       nickname VARCHAR(100) NOT NULL,
       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
       PRIMARY KEY (id),
       UNIQUE KEY uk_users_email (email)
);