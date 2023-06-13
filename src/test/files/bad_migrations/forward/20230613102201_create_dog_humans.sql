
CREATE TABLE dog_humans (
  id BIGSERIAL PRIMARY KEY,
  human_id BIGINT,
  dog_id BIGINT
);


ALTER TABLE dog_humans ADD CONSTRAINT fk_dog_humans_humans FOREIGN KEY (human_id) REFERENCES humans (id);
ALTER TABLE dog_humans ADD CONSTRAINT fk_dog_humans_dogs FOREIGN KEY (dog_id) REFERENCES dogs (id);

CREATE UNIQUE INDEX dog_humans_pairs ON users (person_id, dog_id);

INSERT INTO humans (name, age) VALUES ('John', 40);
INSERT INTO humans (name, age) VALUES ('Tom', 35);


INSERT INTO dogs (name, age) VALUES ('Rex', 6);
INSERT INTO dogs (name, age) VALUES ('Brady', 4);
  
INSERT INTO dog_humans (human_id, dog_id) VALUES
  ((SELECT id FROM humans WHERE name = 'John' LIMIT 1),
  (SELECT id FROM humans WHERE name = 'Rex' LIMIT 1));

-- INSERT INTO dog_humans (human_id, dog_id) VALUES
--   ((SELECT id FROM humans WHERE name = 'John' LIMIT 1),
--   (SELECT id FROM humans WHERE name = 'Rex' LIMIT 1));

  