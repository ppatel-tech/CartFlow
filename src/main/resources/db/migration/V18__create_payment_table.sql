CREATE TABLE payment (
                         id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
                         order_id                BIGINT          NOT NULL,
                         payment_method          VARCHAR(20)     NOT NULL,
                         payment_status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
                         transaction_reference   VARCHAR(50)     NOT NULL,
                         paid_amount             DECIMAL(10,2),
                         paid_at                 TIMESTAMP       NULL,
                         created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT uq_payment_order_id UNIQUE (order_id),
                         CONSTRAINT uq_payment_transaction_reference UNIQUE (transaction_reference),
                         CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES `order` (id)
);