ALTER TABLE repositories ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE repositories ALTER COLUMN github_id SET NOT NULL;

ALTER TABLE repositories ALTER COLUMN name SET NOT NULL;

ALTER TABLE repositories ALTER COLUMN is_private SET NOT NULL;

UPDATE repositories SET description = '' WHERE description IS NULL;

ALTER TABLE repositories ALTER COLUMN description SET NOT NULL;

ALTER TABLE repositories ALTER COLUMN github_created_at SET NOT NULL;
