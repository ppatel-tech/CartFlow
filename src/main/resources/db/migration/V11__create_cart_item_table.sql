CREATE TABLE cart_item (
                           id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                           cart_id         BIGINT         NOT NULL,
                           product_id      BIGINT         NOT NULL,
                           quantity        INT            NOT NULL,
                           unit_price      DECIMAL(10,2)  NOT NULL,
                           created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                           CONSTRAINT uq_cart_item_cart_product UNIQUE (cart_id, product_id),
                           CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id) ON DELETE CASCADE,
                           CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES product (id),
                           CONSTRAINT chk_cart_item_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_cart_item_cart_id ON cart_item (cart_id);