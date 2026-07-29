CREATE TABLE address (
                         id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id         BIGINT       NOT NULL,
                         full_name       VARCHAR(100) NOT NULL,
                         phone           VARCHAR(20)  NOT NULL,
                         street          VARCHAR(255) NOT NULL,
                         city            VARCHAR(100) NOT NULL,
                         state           VARCHAR(100) NOT NULL,
                         country         VARCHAR(100) NOT NULL,
                         postal_code     VARCHAR(20)  NOT NULL,
                         address_type    VARCHAR(20)  NOT NULL DEFAULT 'HOME',
                         is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
                         created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE INDEX idx_address_user_id ON address (user_id);