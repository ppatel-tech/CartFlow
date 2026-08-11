CREATE TABLE `order` (
                         id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
                         order_number            VARCHAR(30)     NOT NULL,
    customer_id             BIGINT          NOT NULL,

    shipping_full_name      VARCHAR(100)    NOT NULL,
    shipping_phone          VARCHAR(20)     NOT NULL,
    shipping_street         VARCHAR(255)    NOT NULL,
    shipping_city           VARCHAR(100)    NOT NULL,
    shipping_state          VARCHAR(100)    NOT NULL,
    shipping_country        VARCHAR(100)    NOT NULL,
    shipping_postal_code    VARCHAR(20)     NOT NULL,

    subtotal                DECIMAL(10,2)   NOT NULL,
    discount                DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    tax                     DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    shipping_charge         DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    final_amount             DECIMAL(10,2)   NOT NULL,

    coupon_code             VARCHAR(30),

    order_status            VARCHAR(30)     NOT NULL DEFAULT 'CREATED',
    payment_status           VARCHAR(20)     NOT NULL DEFAULT 'PENDING',

    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_order_order_number UNIQUE (order_number),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES user (id)
    );

CREATE INDEX idx_order_customer_id ON `order` (customer_id);
CREATE INDEX idx_order_order_number ON `order` (order_number);
CREATE INDEX idx_order_created_at ON `order` (created_at);