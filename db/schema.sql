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
-- Name: users id; Type: DEFAULT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: mrmike
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users_github_id; Type: INDEX; Schema: public; Owner: mrmike
--

CREATE UNIQUE INDEX users_github_id ON public.users USING btree (github_id);


--
-- PostgreSQL database dump complete
--

