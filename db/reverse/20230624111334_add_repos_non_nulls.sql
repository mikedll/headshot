ALTER TABLE repositories ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE repositories ALTER COLUMN github_id DROP NOT NULL;

ALTER TABLE repositories ALTER COLUMN name DROP NOT NULL;

ALTER TABLE repositories ALTER COLUMN is_private DROP NOT NULL;

ALTER TABLE repositories ALTER COLUMN description DROP NOT NULL;

ALTER TABLE repositories ALTER COLUMN github_created_at DROP NOT NULL;
