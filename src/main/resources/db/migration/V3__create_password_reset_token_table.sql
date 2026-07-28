CREATE TABLE password_reset_token (
                                      id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      token           VARCHAR(255) NOT NULL,
                                      user_id         BIGINT       NOT NULL,
                                      expiry_date     TIMESTAMP    NOT NULL,
                                      created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT uq_password_reset_token_token UNIQUE (token),
                                      CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token_user_id ON password_reset_token (user_id);