ALTER TABLE user_profiles
    ADD COLUMN timezone VARCHAR(80) NOT NULL DEFAULT 'America/Sao_Paulo',
    ADD COLUMN theme VARCHAR(20) NOT NULL DEFAULT 'system',
    ADD COLUMN default_plan_duration INTEGER NOT NULL DEFAULT 60,
    ADD COLUMN notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notification_lead_minutes INTEGER NOT NULL DEFAULT 10,
    ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;