CREATE TABLE session_logs (
      id UUID PRIMARY KEY,
      user_id UUID NOT NULL,
      plan_id UUID NULL,
      direction_id UUID NULL,
      started_at TIMESTAMPTZ NOT NULL,
      finished_at TIMESTAMPTZ NULL,
      duration_minutes INT NOT NULL,
      result VARCHAR(1000),
      notes TEXT,
      created_at TIMESTAMPTZ NOT NULL,

      CONSTRAINT fk_session_logs_user
          FOREIGN KEY (user_id)
              REFERENCES user_profiles(id),

      CONSTRAINT fk_session_logs_plan
          FOREIGN KEY (plan_id)
              REFERENCES plans(id),

      CONSTRAINT fk_session_logs_direction
          FOREIGN KEY (direction_id)
              REFERENCES directions(id),

      CONSTRAINT chk_session_logs_duration_positive
          CHECK (duration_minutes > 0),

      CONSTRAINT chk_session_logs_finished_after_started
          CHECK (finished_at IS NULL OR finished_at >= started_at)
);

CREATE INDEX idx_session_logs_user_id
    ON session_logs(user_id);

CREATE INDEX idx_session_logs_user_started_at
    ON session_logs(user_id, started_at);

CREATE INDEX idx_session_logs_user_plan
    ON session_logs(user_id, plan_id);

CREATE INDEX idx_session_logs_user_direction
    ON session_logs(user_id, direction_id);

CREATE INDEX idx_session_logs_created_at
    ON session_logs(created_at);