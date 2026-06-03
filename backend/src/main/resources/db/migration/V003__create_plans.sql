CREATE TABLE plans (
   id UUID PRIMARY KEY,
   user_id UUID NOT NULL,
   direction_id UUID NULL,

   title VARCHAR(200) NOT NULL,
   description VARCHAR(1000) NULL,
   type VARCHAR(100) NULL,
   priority VARCHAR(20) NOT NULL,
   status VARCHAR(30) NOT NULL,

   planned_date DATE NULL,
   planned_start_at TIMESTAMPTZ NULL,
   planned_end_at TIMESTAMPTZ NULL,
   estimated_minutes INTEGER NULL,

   notify BOOLEAN NOT NULL DEFAULT FALSE,
   notification_date_time TIMESTAMPTZ NULL,

   started_at TIMESTAMPTZ NULL,
   finished_at TIMESTAMPTZ NULL,
   actual_minutes INTEGER NULL,
   reason VARCHAR(1000) NULL,
   tags JSONB NULL,

   created_at TIMESTAMPTZ NOT NULL,
   updated_at TIMESTAMPTZ NOT NULL,
   last_status_changed_at TIMESTAMPTZ NULL,

   CONSTRAINT fk_plans_user_profiles
       FOREIGN KEY (user_id) REFERENCES user_profiles(id),

   CONSTRAINT fk_plans_directions
       FOREIGN KEY (direction_id) REFERENCES directions(id),

   CONSTRAINT ck_plans_priority
       CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

   CONSTRAINT ck_plans_status
       CHECK (status IN (
                         'DRAFT',
                         'SCHEDULED',
                         'PENDING',
                         'DUE',
                         'IN_PROGRESS',
                         'COMPLETED',
                         'PARTIAL',
                         'POSTPONED',
                         'IGNORED',
                         'CANCELED',
                         'MISSED'
           )),

   CONSTRAINT ck_plans_estimated_minutes
       CHECK (estimated_minutes IS NULL OR estimated_minutes >= 1)
);

CREATE INDEX idx_plans_user_id ON plans(user_id);
CREATE INDEX idx_plans_user_status ON plans(user_id, status);
CREATE INDEX idx_plans_user_direction ON plans(user_id, direction_id);
CREATE INDEX idx_plans_user_planned_date ON plans(user_id, planned_date);