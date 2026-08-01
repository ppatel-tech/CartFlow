CREATE TABLE product (
                         id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name                VARCHAR(200)    NOT NULL,
                         sku                 VARCHAR(50)     NOT NULL,
                         description         TEXT,
                         price               DECIMAL(10,2)   NOT NULL,
                         discount_price      DECIMAL(10,2),
                         average_rating      DECIMAL(3,2)    NOT NULL DEFAULT 0.00,
                         total_reviews       INT             NOT NULL DEFAULT 0,
                         is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
                         category_id         BIGINT          NOT NULL,
                         brand_id            BIGINT          NOT NULL,
                         created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT uq_product_sku UNIQUE (sku),
                         CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
                         CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand (id),
                         CONSTRAINT chk_product_price_positive CHECK (price >= 0),
                         CONSTRAINT chk_product_discount_valid CHECK (discount_price IS NULL OR discount_price < price)
);

CREATE INDEX idx_product_name ON product (name);
CREATE INDEX idx_product_category_id ON product (category_id);
CREATE INDEX idx_product_brand_id ON product (brand_id);
CREATE INDEX idx_product_price ON product (price);