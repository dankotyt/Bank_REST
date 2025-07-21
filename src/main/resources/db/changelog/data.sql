--
-- PostgreSQL database dump
--

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

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.users (
    user_id, name, surname, patronymic, birthday, email,
    phone_number, password, role, created_at, refresh_token, refresh_token_expiry
) VALUES
      (1, 'Max', 'Krivoy', 'Vladlenovich', '2010-12-31', 'USER333@gmail.com',
       '+79991414151', '$2a$12$N688X8CG0cakseO3bay59OGkYt6rVT3N6WxUTyo/F6cZBPX3vP2Iu', 'ADMIN', '2025-07-18 14:22:49.48076', NULL, NULL),

      (2, 'Max', 'Krivoy', 'Vladlenovich', '2010-12-31', 'USER33@gmail.com',
       '+7999141411', '$2a$12$8mFDpZzziu6Q6hP80K8t3.cZPxJG5PFxOTES/8mszM1SQkeZZQkkC', 'ADMIN', '2025-07-18 14:55:47.335385', NULL, NULL),

      (3, 'Max222', 'Krivoy', 'Vladlenovich', '2010-12-31', '2223@gmail.com',
       '+79991414221', '$2a$12$v54hw10t6WCJxoVdqO.YNuuX3yOx85kJLWfP0aqB.8Hh0h5AwRtB2', 'ADMIN', '2025-07-18 14:59:53.418759', NULL, NULL),

      (4, 'Max2', 'Krnoy', 'Vladlenovich', '2010-12-31', '1@gmail.com',
       '+76991414221', '$2a$12$EG27YdhB8s4WADTbcmwgT.Zd2ZfWlWNapTxFiLpc3e6g.WJVbpl5u', 'ADMIN', '2025-07-18 15:09:49.33233', NULL, NULL),

      (5, 'Max2', 'Krnoy', 'Vladlenovich', '2010-12-31', '12@gmail.com',
       '+769911414221', '$2a$12$LZa0.pWXKXJ0t6bc0pBaxuMmuqHi6pjBMnLy0b8G60D01zY4cv7U2', 'ADMIN', '2025-07-18 15:18:51.741407', NULL, NULL),

      (8, 'USerCreatedByAdmin', 'anoy', 'Vladlenovich', '2010-12-31', 'testuserbuadmin@gmail.com',
       '+700911418462', 'password', NULL, '2025-07-18 23:58:38.56179', NULL, NULL),

      (11, 'USerCreatedByAdmin', 'anoy', 'Vladlenovich', '2010-12-31', 'testuserbuadmin1@gmail.com',
       '+710911418462', '$2a$12$N6WYc.DSVekYDvV3n2jo2OKAdKvkEswMqGfhJhY01VghWfl8vb916', 'USER', '2025-07-19 00:04:18.862893', NULL, NULL),

      (6, 'TestUser', 'Krnoy', 'Vladlenovich', '2010-12-31', 'test@gmail.com',
       '+769911418462', '$2a$12$y04mRKCu.bC4.k8yXQ5YJ.PMICGIycEh00MToi7R55wAySh9zpYkm', 'ADMIN', '2025-07-18 23:09:16.76673', NULL, '2025-07-20 04:07:25.009973');


--
-- Data for Name: cards; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.cards (
    card_id, balance, card_holder, card_number, expiry_date, status, user_id
) VALUES
    (1, 0.00, 'USerCreatedByAdmin anoy', '**** **** **** 2709', '2030-07-19', 'ACTIVE', 11);


--
-- Data for Name: saved_cards; Type: TABLE DATA; Schema: public; Owner: postgres
--


--
-- Sequences
--

SELECT pg_catalog.setval('public.cards_card_id_seq', 5, true);
SELECT pg_catalog.setval('public.users_user_id_seq', 13, true);


--
-- PostgreSQL database dump complete
--