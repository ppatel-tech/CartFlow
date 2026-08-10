CREATE TABLE coupon (
                        id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                        code                VARCHAR(30)     NOT NULL,
                        discount_type       VARCHAR(20)     NOT NULL,
                        discount_value      DECIMAL(10,2)   NOT NULL,
                        minimum_purchase    DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
                        maximum_discount    DECIMAL(10,2),
                        usage_limit         INT             NOT NULL,
                        used_count          INT             NOT NULL DEFAULT 0,
                        expiry_date         TIMESTAMP       NOT NULL,
                        is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
                        created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT uq_coupon_code UNIQUE (code),
                        CONSTRAINT chk_coupon_discount_value_positive CHECK (discount_value > 0),
                        CONSTRAINT chk_coupon_usage_limit_positive CHECK (usage_limit > 0)
);