CREATE TABLE wishlist (
                          id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id         BIGINT       NOT NULL,
                          created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT uq_wishlist_user_id UNIQUE (user_id),
                          CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);