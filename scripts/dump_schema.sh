#!/usr/bin/env bash

pg_dump --schema-only headshot_development > ./db/schema.sql

echo "Dumped PostgreSQL schema"
