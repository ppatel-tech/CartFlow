CREATE TABLE brand (
                       id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name            VARCHAR(100) NOT NULL,
                       description     VARCHAR(500),
                       is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT uq_brand_name UNIQUE (name)
);