CREATE TABLE cart (
                      id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                      user_id         BIGINT       NOT NULL,
                      created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                      CONSTRAINT uq_cart_user_id UNIQUE (user_id),
                      CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);