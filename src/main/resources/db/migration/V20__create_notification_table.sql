CREATE TABLE notification (
                              id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id             BIGINT       NOT NULL,
                              title               VARCHAR(200) NOT NULL,
                              message             TEXT         NOT NULL,
                              notification_type   VARCHAR(20)  NOT NULL,
                              is_read             BOOLEAN      NOT NULL DEFAULT FALSE,
                              created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_user_id ON notification (user_id);