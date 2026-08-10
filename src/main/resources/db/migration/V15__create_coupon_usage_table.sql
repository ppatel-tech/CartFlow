CREATE TABLE coupon_usage (
                              id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                              coupon_id       BIGINT       NOT NULL,
                              user_id         BIGINT       NOT NULL,
                              used_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uq_coupon_usage_coupon_user UNIQUE (coupon_id, user_id),
                              CONSTRAINT fk_coupon_usage_coupon FOREIGN KEY (coupon_id) REFERENCES coupon (id) ON DELETE CASCADE,
                              CONSTRAINT fk_coupon_usage_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE INDEX idx_coupon_usage_user_id ON coupon_usage (user_id);