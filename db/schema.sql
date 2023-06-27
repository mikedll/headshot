
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

CREATE TABLE public.pages (
    id bigint NOT NULL,
    tour_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    filename character varying NOT NULL,
    line_number integer NOT NULL,
    language character varying NOT NULL,
    narration character varying NOT NULL
);

CREATE SEQUENCE public.pages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.pages_id_seq OWNED BY public.pages.id;

CREATE TABLE public.repositories (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    github_id bigint NOT NULL,
    name character varying NOT NULL,
    is_private boolean NOT NULL,
    description character varying,
    github_created_at timestamp(6) without time zone NOT NULL
);

CREATE SEQUENCE public.repositories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.repositories_id_seq OWNED BY public.repositories.id;

CREATE TABLE public.schema_migrations (
    id bigint NOT NULL,
    version character varying
);

CREATE SEQUENCE public.schema_migrations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.schema_migrations_id_seq OWNED BY public.schema_migrations.id;

CREATE TABLE public.tours (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    name character varying NOT NULL
);

CREATE SEQUENCE public.tours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.tours_id_seq OWNED BY public.tours.id;

CREATE TABLE public.users (
    id bigint NOT NULL,
    name character varying NOT NULL,
    github_id bigint NOT NULL,
    github_login character varying NOT NULL,
    url character varying NOT NULL,
    html_url character varying NOT NULL,
    repos_url character varying NOT NULL,
    access_token character varying NOT NULL
);

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;

ALTER TABLE ONLY public.pages ALTER COLUMN id SET DEFAULT nextval('public.pages_id_seq'::regclass);

ALTER TABLE ONLY public.repositories ALTER COLUMN id SET DEFAULT nextval('public.repositories_id_seq'::regclass);

ALTER TABLE ONLY public.schema_migrations ALTER COLUMN id SET DEFAULT nextval('public.schema_migrations_id_seq'::regclass);

ALTER TABLE ONLY public.tours ALTER COLUMN id SET DEFAULT nextval('public.tours_id_seq'::regclass);

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);

ALTER TABLE ONLY public.pages
    ADD CONSTRAINT pages_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.repositories
    ADD CONSTRAINT repositories_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.schema_migrations
    ADD CONSTRAINT schema_migrations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT tours_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

CREATE UNIQUE INDEX repositories_user_id_github_id ON public.repositories USING btree (user_id, github_id);

CREATE UNIQUE INDEX schema_migrations_version ON public.schema_migrations USING btree (version);

CREATE UNIQUE INDEX users_github_id ON public.users USING btree (github_id);

ALTER TABLE ONLY public.pages
    ADD CONSTRAINT fk_pages_tours FOREIGN KEY (tour_id) REFERENCES public.tours(id);

ALTER TABLE ONLY public.repositories
    ADD CONSTRAINT fk_repositories_users FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT fk_tours_users FOREIGN KEY (user_id) REFERENCES public.users(id);

