#!/usr/bin/env bash

pg_dump --schema-only --no-owner headshot_development | sed -e '/^--/d' > ./db/schema.sql

echo "Dumped PostgreSQL schema"
