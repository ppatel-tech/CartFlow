CREATE TABLE refresh_token (
                               id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                               token           VARCHAR(255) NOT NULL,
                               user_id         BIGINT       NOT NULL,
                               expiry_date     TIMESTAMP    NOT NULL,
                               created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uq_refresh_token_token UNIQUE (token),
                               CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);