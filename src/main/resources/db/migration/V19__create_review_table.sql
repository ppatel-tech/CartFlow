CREATE TABLE review (
                        id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                        product_id      BIGINT       NOT NULL,
                        user_id         BIGINT       NOT NULL,
                        order_id        BIGINT       NOT NULL,
                        rating          INT          NOT NULL,
                        review          TEXT,
                        created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT uq_review_product_user UNIQUE (product_id, user_id),
                        CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product (id),
                        CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
                        CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES `order` (id),
                        CONSTRAINT chk_review_rating_range CHECK (rating >= 1 AND rating <= 5)
);

CREATE INDEX idx_review_product_id ON review (product_id);