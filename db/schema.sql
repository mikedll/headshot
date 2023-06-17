--
-- PostgreSQL database dump
--

-- Dumped from database version 14.8 (Homebrew)
-- Dumped by pg_dump version 14.8 (Homebrew)

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

--
-- Name: repositories; Type: TABLE; Schema: public; Owner: mrmike
--

CREATE TABLE public.repositories (
    id bigint NOT NULL,
    user_id bigint,
    name character varying,
    github_id bigint,
    is_private boolean,
    description character varying,
    github_created_at timestamp(6) without time zone
);


ALTER TABLE public.repositories OWNER TO mrmike;

--
-- Name: repositories_id_seq; Type: SEQUENCE; Schema: public; Owner: mrmike
--

CREATE SEQUENCE public.repositories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.repositories_id_seq OWNER TO mrmike;

--
-- Name: repositories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mrmike
--

ALTER SEQUENCE public.repositories_id_seq OWNED BY public.repositories.id;


--
-- Name: schema_migrations; Type: TABLE; Schema: public; Owner: mrmike
--

CREATE TABLE public.schema_migrations (
    id bigint NOT NULL,
    version character varying
);


ALTER TABLE public.schema_migrations OWNER TO mrmike;

--
-- Name: schema_migrations_id_seq; Type: SEQUENCE; Schema: public; Owner: mrmike
--

CREATE SEQUENCE public.schema_migrations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.schema_migrations_id_seq OWNER TO mrmike;

--
-- Name: schema_migrations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mrmike
--

ALTER SEQUENCE public.schema_migrations_id_seq OWNED BY public.schema_migrations.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: mrmike
--

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


ALTER TABLE public.users OWNER TO mrmike;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: mrmike
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO mrmike;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mrmike
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: repositories id; Type: DEFAULT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.repositories ALTER COLUMN id SET DEFAULT nextval('public.repositories_id_seq'::regclass);


--
-- Name: schema_migrations id; Type: DEFAULT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.schema_migrations ALTER COLUMN id SET DEFAULT nextval('public.schema_migrations_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: repositories repositories_pkey; Type: CONSTRAINT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.repositories
    ADD CONSTRAINT repositories_pkey PRIMARY KEY (id);


--
-- Name: schema_migrations schema_migrations_pkey; Type: CONSTRAINT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.schema_migrations
    ADD CONSTRAINT schema_migrations_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: repositories_id_user_id; Type: INDEX; Schema: public; Owner: mrmike
--

CREATE UNIQUE INDEX repositories_id_user_id ON public.repositories USING btree (id, user_id);


--
-- Name: schema_migrations_version; Type: INDEX; Schema: public; Owner: mrmike
--

CREATE UNIQUE INDEX schema_migrations_version ON public.schema_migrations USING btree (version);


--
-- Name: users_github_id; Type: INDEX; Schema: public; Owner: mrmike
--

CREATE UNIQUE INDEX users_github_id ON public.users USING btree (github_id);


--
-- Name: repositories fk_repositories_users; Type: FK CONSTRAINT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.repositories
    ADD CONSTRAINT fk_repositories_users FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

