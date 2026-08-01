CREATE TABLE product_image (
                               id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                               product_id      BIGINT       NOT NULL,
                               image_url       VARCHAR(500) NOT NULL,
                               display_order   INT          NOT NULL DEFAULT 0,
                               created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

CREATE INDEX idx_product_image_product_id ON product_image (product_id);