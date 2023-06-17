
CREATE TABLE repositories (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  github_id BIGINT,
  name CHARACTER VARYING,
  is_private BOOLEAN,
  description CHARACTER VARYING,
  github_created_at TIMESTAMP(6)
);

ALTER TABLE repositories ADD CONSTRAINT fk_repositories_users FOREIGN KEY (user_id) REFERENCES users (id);

CREATE UNIQUE INDEX repositories_user_id_github_id ON repositories (user_id, github_id);
