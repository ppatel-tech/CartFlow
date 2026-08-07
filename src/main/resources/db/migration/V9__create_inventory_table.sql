CREATE TABLE inventory (
                           id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
                           product_id            BIGINT       NOT NULL,
                           available_quantity    INT          NOT NULL DEFAULT 0,
                           reserved_quantity     INT          NOT NULL DEFAULT 0,
                           low_stock_threshold   INT          NOT NULL DEFAULT 10,
                           created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                           CONSTRAINT uq_inventory_product_id UNIQUE (product_id),
                           CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
                           CONSTRAINT chk_inventory_available_non_negative CHECK (available_quantity >= 0),
                           CONSTRAINT chk_inventory_reserved_non_negative CHECK (reserved_quantity >= 0)
);