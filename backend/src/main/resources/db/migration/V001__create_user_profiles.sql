CREATE TABLE user_profiles (
   id UUID PRIMARY KEY,
   keycloak_subject VARCHAR(255) NOT NULL UNIQUE,
   email VARCHAR(255),
   username VARCHAR(255),
   full_name VARCHAR(255),
   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
   updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_user_profiles_keycloak_subject
    ON user_profiles (keycloak_subject);