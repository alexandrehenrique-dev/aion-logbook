CREATE TABLE notifications (
    id                  UUID         PRIMARY KEY,
    user_id             UUID         NOT NULL,
    type                VARCHAR(40)  NOT NULL,
    title               VARCHAR(200) NOT NULL,
    message             VARCHAR(1000),
    read                BOOLEAN      NOT NULL DEFAULT FALSE,
    related_entity_type VARCHAR(40),
    related_entity_id   UUID,
    created_at          TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_notifications_user_profiles
        FOREIGN KEY (user_id) REFERENCES user_profiles(id)
);

CREATE INDEX idx_notifications_user_id     ON notifications(user_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read) WHERE NOT read;
CREATE INDEX idx_notifications_dedup       ON notifications(user_id, related_entity_id, type);
