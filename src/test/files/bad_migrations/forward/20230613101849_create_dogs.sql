
CREATE TABLE dogs (
  id BIGSERIAL PRIMARY KEY,
  name CHARACTER VARYING  NOT NULL,
  age INTEGER
);

CREATE UNIQUE INDEX dogs_name ON dogs (name);
    