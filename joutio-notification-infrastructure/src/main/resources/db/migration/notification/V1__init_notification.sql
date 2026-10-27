-- Joutio notification — baseline schema (squashed)
-- Generated from a fresh PostgreSQL 16 database after applying the legacy migrations.

--
-- PostgreSQL database dump
--


-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: notification_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification_entry (
    notification_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL,
    type character varying(40) NOT NULL,
    actor_id character varying(255),
    source_id character varying(255),
    topic_id character varying(255),
    game_id character varying(255),
    expires_at timestamp without time zone,
    read_at timestamp without time zone,
    created_at timestamp without time zone NOT NULL
);


--
-- Name: notification_preference_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification_preference_entry (
    user_id character varying(255) NOT NULL,
    category character varying(40) NOT NULL,
    enabled boolean NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


--
-- Name: notification_entry notification_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification_entry
    ADD CONSTRAINT notification_entry_pkey PRIMARY KEY (notification_id);


--
-- Name: notification_preference_entry notification_preference_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification_preference_entry
    ADD CONSTRAINT notification_preference_entry_pkey PRIMARY KEY (user_id, category);


--
-- Name: idx_notification_unread; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notification_unread ON public.notification_entry USING btree (user_id, read_at);


--
-- Name: idx_notification_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notification_user ON public.notification_entry USING btree (user_id, created_at);


--
-- Name: uq_notification_source; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_notification_source ON public.notification_entry USING btree (type, source_id, user_id);


--
-- PostgreSQL database dump complete
--


