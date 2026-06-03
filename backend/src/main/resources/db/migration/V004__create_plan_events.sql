CREATE TABLE plan_events (
     id UUID PRIMARY KEY,
     user_id UUID NOT NULL,
     plan_id UUID NOT NULL,

     event_type VARCHAR(40) NOT NULL,
     from_status VARCHAR(30) NULL,
     to_status VARCHAR(30) NULL,
     description VARCHAR(1000) NULL,
     metadata JSONB NULL,

     created_at TIMESTAMPTZ NOT NULL,

     CONSTRAINT fk_plan_events_user_profiles
         FOREIGN KEY (user_id) REFERENCES user_profiles(id),

     CONSTRAINT fk_plan_events_plans
         FOREIGN KEY (plan_id) REFERENCES plans(id),

     CONSTRAINT ck_plan_events_event_type
         CHECK (event_type IN (
                               'CREATED',
                               'UPDATED',
                               'STARTED',
                               'COMPLETED',
                               'PARTIAL_COMPLETED',
                               'POSTPONED',
                               'IGNORED',
                               'CANCELED',
                               'MISSED',
                               'DUE',
                               'NOTE_ADDED',
                               'MODIFIED'
             ))
);

CREATE INDEX idx_plan_events_user_id ON plan_events(user_id);
CREATE INDEX idx_plan_events_plan_id_created_at ON plan_events(plan_id, created_at ASC);