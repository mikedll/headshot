
  
CREATE TABLE humans (
  id BIGSERIAL PRIMARY KEY,
  name CHARACTER VARYING  NOT NULL,
  age INTEGER
);

CREATE UNIQUE INDEX humans_name ON humans (name);
  