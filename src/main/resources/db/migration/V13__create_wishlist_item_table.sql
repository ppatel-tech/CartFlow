CREATE TABLE wishlist_item (
                               id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                               wishlist_id     BIGINT       NOT NULL,
                               product_id      BIGINT       NOT NULL,
                               created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uq_wishlist_item_wishlist_product UNIQUE (wishlist_id, product_id),
                               CONSTRAINT fk_wishlist_item_wishlist FOREIGN KEY (wishlist_id) REFERENCES wishlist (id) ON DELETE CASCADE,
                               CONSTRAINT fk_wishlist_item_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE INDEX idx_wishlist_item_wishlist_id ON wishlist_item (wishlist_id);