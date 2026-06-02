CREATE TABLE directions (
    id UUID PRIMARY KEY,
    user_profile_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    color VARCHAR(20),
    icon VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    identity_phrase VARCHAR(200),
    archived_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_directions_user_profile
        FOREIGN KEY (user_profile_id)
            REFERENCES user_profiles (id)
);

CREATE INDEX idx_directions_user_profile_id
    ON directions (user_profile_id);

CREATE INDEX idx_directions_user_profile_status
    ON directions (user_profile_id, status);
