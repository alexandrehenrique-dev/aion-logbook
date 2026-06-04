CREATE UNIQUE INDEX IF NOT EXISTS uk_plan_events_scheduler_due_missed
    ON plan_events (plan_id, event_type)
    WHERE event_type IN ('DUE', 'MISSED');
