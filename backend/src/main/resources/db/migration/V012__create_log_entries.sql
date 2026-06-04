create table log_entries (
     id uuid primary key,
     user_id uuid not null references user_profiles(id),
     direction_id uuid references directions(id),
     plan_id uuid references plans(id),
     title varchar(200) not null,
     content varchar(10000) not null,
     type varchar(30) not null,
     tags jsonb,
     created_at timestamptz not null,
     updated_at timestamptz not null
);

create index idx_log_entries_user_created_at on log_entries(user_id, created_at);
create index idx_log_entries_user_type on log_entries(user_id, type);
create index idx_log_entries_user_direction on log_entries(user_id, direction_id);
create index idx_log_entries_user_plan on log_entries(user_id, plan_id);
create index idx_log_entries_tags_gin on log_entries using gin(tags);
