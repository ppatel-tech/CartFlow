CREATE TABLE order_item (
                            id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                            order_id            BIGINT          NOT NULL,
                            product_id          BIGINT          NOT NULL,
                            product_name        VARCHAR(200)    NOT NULL,
                            quantity             INT             NOT NULL,
                            selling_price        DECIMAL(10,2)   NOT NULL,

                            CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES `order` (id),
                            CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product (id),
                            CONSTRAINT chk_order_item_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);