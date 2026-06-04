CREATE TABLE bug_reports (
     id UUID PRIMARY KEY,

     user_id UUID NOT NULL,

     title VARCHAR(200) NOT NULL,
     description VARCHAR(2000) NOT NULL,
     severity VARCHAR(50) NOT NULL,
     page VARCHAR(500),

     metadata JSONB,

     status VARCHAR(50) NOT NULL,

     telegram_sent BOOLEAN NOT NULL DEFAULT FALSE,
     telegram_error VARCHAR(1000),

     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
     updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bug_reports_user_id
    ON bug_reports (user_id);

CREATE INDEX idx_bug_reports_status
    ON bug_reports (status);

CREATE INDEX idx_bug_reports_created_at
    ON bug_reports (created_at);