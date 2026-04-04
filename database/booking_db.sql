--
-- PostgreSQL database dump
--

\restrict J9iPRaw1BcnBfJIWh7DB6yKWglups4hjMxWf8Txrwj1GBYxeGgg7Cy4xh0y6Y0q

-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

-- Started on 2026-04-02 19:51:50

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 16413)
-- Name: btree_gist; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA public;


--
-- TOC entry 5316 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION btree_gist; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION btree_gist IS 'support for indexing common datatypes in GiST';


--
-- TOC entry 315 (class 1255 OID 17199)
-- Name: update_updated_at(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_updated_at() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 221 (class 1259 OID 17170)
-- Name: bookings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bookings (
    booking_id integer NOT NULL,
    room_number character varying(10) NOT NULL,
    customer_name character varying(100) NOT NULL,
    email character varying(100),
    phone character varying(20),
    total_guests integer NOT NULL,
    adults integer NOT NULL,
    children integer DEFAULT 0,
    check_in date NOT NULL,
    check_out date NOT NULL,
    status character varying(20) DEFAULT 'da_dat'::character varying NOT NULL,
    payment_status character varying(20) DEFAULT 'cho_thanh_toan'::character varying NOT NULL,
    payment_method character varying(50),
    total_amount numeric(10,2),
    note text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_dates CHECK ((check_out > check_in)),
    CONSTRAINT check_guests CHECK (((adults + children) = total_guests)),
    CONSTRAINT check_payment_logic CHECK ((NOT (((status)::text = 'da_tra_phong'::text) AND ((payment_status)::text = 'cho_thanh_toan'::text)))),
    CONSTRAINT check_payment_status CHECK (((payment_status)::text = ANY ((ARRAY['cho_thanh_toan'::character varying, 'da_thanh_toan'::character varying, 'da_hoan_tien'::character varying, 'thanh_toan_that_bai'::character varying])::text[]))),
    CONSTRAINT check_status CHECK (((status)::text = ANY ((ARRAY['da_dat'::character varying, 'da_xac_nhan'::character varying, 'da_nhan_phong'::character varying, 'da_tra_phong'::character varying, 'da_huy'::character varying])::text[])))
);


ALTER TABLE public.bookings OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 17169)
-- Name: bookings_booking_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.bookings_booking_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.bookings_booking_id_seq OWNER TO postgres;

--
-- TOC entry 5317 (class 0 OID 0)
-- Dependencies: 220
-- Name: bookings_booking_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.bookings_booking_id_seq OWNED BY public.bookings.booking_id;


--
-- TOC entry 5146 (class 2604 OID 17173)
-- Name: bookings booking_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bookings ALTER COLUMN booking_id SET DEFAULT nextval('public.bookings_booking_id_seq'::regclass);


--
-- TOC entry 5310 (class 0 OID 17170)
-- Dependencies: 221
-- Data for Name: bookings; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.bookings VALUES (26, '101', 'John Smith', 'john.smith@gmail.com', '+12025550101', 2, 2, 0, '2026-03-01', '2026-03-03', 'da_tra_phong', 'da_thanh_toan', 'Credit', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (27, '101', 'Le Thi Huong', 'huong.le@gmail.com', '0988776655', 3, 2, 1, '2026-03-05', '2026-03-07', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (28, '101', 'Michael Johnson', 'mjohnson88@yahoo.com', '+447911123456', 1, 1, 0, '2026-03-09', '2026-03-10', 'da_tra_phong', 'da_thanh_toan', 'Credit', 1200000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (29, '101', 'Pham Thu Trang', 'trang.pham@gmail.com', '0933445566', 4, 2, 2, '2026-03-12', '2026-03-15', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (30, '101', 'Hoang Gia Bao', 'bao.hoang@gmail.com', '0977889900', 2, 2, 0, '2026-03-17', '2026-03-20', 'da_tra_phong', 'da_thanh_toan', 'Cash', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (31, '101', 'Emily Davis', 'emily.davis@hotmail.com', '+61491570156', 2, 1, 1, '2026-03-22', '2026-03-24', 'da_tra_phong', 'da_thanh_toan', 'Credit', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (32, '101', 'Do Hoang Son', 'son.do@gmail.com', '0944556677', 3, 3, 0, '2026-03-26', '2026-03-29', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (33, '102', 'Ngo Mai Lan', 'lan.ngo@gmail.com', '0911223344', 2, 2, 0, '2026-03-01', '2026-03-04', 'da_tra_phong', 'da_thanh_toan', 'Cash', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (34, '102', 'David Wilson', 'dwilson.travel@gmail.com', '+14155552671', 4, 2, 2, '2026-03-06', '2026-03-08', 'da_tra_phong', 'da_thanh_toan', 'Credit', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (35, '102', 'Bui Thuy Hanh', 'hanh.bui@gmail.com', '0905667788', 1, 1, 0, '2026-03-10', '2026-03-12', 'da_tra_phong', 'da_thanh_toan', 'Credit', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (36, '102', 'Ho Dang Khoa', 'khoa.ho@gmail.com', '0932112233', 3, 2, 1, '2026-03-14', '2026-03-16', 'da_tra_phong', 'da_thanh_toan', 'Cash', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (37, '102', 'Sarah Brown', 'sarah.b99@gmail.com', '+13125550928', 2, 2, 0, '2026-03-18', '2026-03-21', 'da_tra_phong', 'da_thanh_toan', 'Credit', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (38, '102', 'Duong Minh Cuong', 'cuong.duong@gmail.com', '0918223344', 4, 3, 1, '2026-03-23', '2026-03-25', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (39, '102', 'Ly Thu Thuy', 'thuy.ly@gmail.com', '0903445566', 2, 2, 0, '2026-03-27', '2026-03-30', 'da_huy', 'da_hoan_tien', 'Transfer', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (40, '103', 'Vo Thanh Luan', 'luan.vo@gmail.com', '0981223344', 2, 2, 0, '2026-03-02', '2026-03-05', 'da_tra_phong', 'da_thanh_toan', 'Cash', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (41, '103', 'Truong Thi Kim', 'kim.truong@gmail.com', '0902334455', 3, 2, 1, '2026-03-07', '2026-03-09', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (42, '103', 'James Taylor', 'jtaylor.dev@yahoo.com', '+447700900123', 1, 1, 0, '2026-03-11', '2026-03-13', 'da_tra_phong', 'da_thanh_toan', 'Credit', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (43, '103', 'Lam Dieu Ly', 'ly.lam@gmail.com', '0971223344', 4, 2, 2, '2026-03-15', '2026-03-18', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (44, '103', 'Trinh Van Hai', 'hai.trinh@gmail.com', '0915667788', 2, 2, 0, '2026-03-20', '2026-03-22', 'da_tra_phong', 'da_thanh_toan', 'Cash', 2400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (45, '103', 'Robert Miller', 'rmiller_85@gmail.com', '+16175550198', 3, 2, 1, '2026-03-24', '2026-03-27', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (46, '201', 'Phung Thanh Tra', 'tra.phung@gmail.com', '0988445566', 2, 2, 0, '2026-04-01', '2026-04-03', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (47, '201', 'Jessica Anderson', 'jessica.anderson@gmail.com', '+12125550155', 1, 1, 0, '2026-04-05', '2026-04-07', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (48, '201', 'Le Phuong Linh', 'linh.le@gmail.com', '0903889900', 4, 2, 2, '2026-04-09', '2026-04-12', 'da_dat', 'da_thanh_toan', 'Credit', 5400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (49, '201', 'Tran Bao Khang', 'khang.tran@gmail.com', '0931223344', 2, 2, 0, '2026-04-14', '2026-04-16', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (50, '201', 'William Thomas', 'wthomas.biz@hotmail.com', '+447811122233', 3, 2, 1, '2026-04-18', '2026-04-20', 'da_huy', 'thanh_toan_that_bai', 'Credit', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (51, '201', 'Hoang Duc Manh', 'manh.hoang@gmail.com', '0919445566', 2, 2, 0, '2026-04-22', '2026-04-25', 'da_dat', 'cho_thanh_toan', NULL, 5400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (52, '202', 'Richard Jackson', 'richard.j@gmail.com', '+61491570200', 2, 2, 0, '2026-04-02', '2026-04-04', 'da_dat', 'da_thanh_toan', 'Credit', 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (53, '202', 'Do Tien Dung', 'dung.do@gmail.com', '0983667788', 3, 2, 1, '2026-04-06', '2026-04-08', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (54, '202', 'Ngo Thanh Hai', 'hai.ngo@gmail.com', '0914778899', 1, 1, 0, '2026-04-10', '2026-04-12', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (55, '202', 'Mary White', 'mary.white@yahoo.com', '+13055550123', 4, 2, 2, '2026-04-14', '2026-04-17', 'da_dat', 'cho_thanh_toan', NULL, 5400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (56, '202', 'Bui Ngoc Truong', 'truong.bui@gmail.com', '0976112233', 2, 2, 0, '2026-04-19', '2026-04-21', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (57, '202', 'Ho Minh Tuan', 'tuan.ho@gmail.com', '0908223344', 3, 2, 1, '2026-04-23', '2026-04-25', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (58, '203', 'Phan Cam Yen', 'yen.phan@gmail.com', '0989334455', 2, 2, 0, '2026-04-03', '2026-04-05', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (59, '203', 'Thomas Harris', 'tharris.photo@gmail.com', '+12065550188', 1, 1, 0, '2026-04-07', '2026-04-09', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (60, '203', 'Ly Tan Dat', 'dat.ly@gmail.com', '0937556677', 4, 2, 2, '2026-04-11', '2026-04-14', 'da_dat', 'cho_thanh_toan', NULL, 5400000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (61, '203', 'Vo Kieu Loan', 'loan.vo@gmail.com', '0979667788', 2, 2, 0, '2026-04-16', '2026-04-18', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (62, '203', 'Charles Martin', 'cmartin.uk@gmail.com', '+447911133445', 3, 2, 1, '2026-04-20', '2026-04-22', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (63, '203', 'Doan Thu Ha', 'ha.doan@gmail.com', '0985889900', 2, 2, 0, '2026-04-24', '2026-04-26', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (64, '301', 'Lam Gia Hung', 'hung.lam@gmail.com', '0916990011', 2, 2, 0, '2026-05-01', '2026-05-03', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (65, '301', 'Christopher Lee', 'chris.lee@yahoo.com', '+14165550199', 3, 2, 1, '2026-05-05', '2026-05-07', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (66, '301', 'Mai Quoc Khanh', 'khanh.mai@gmail.com', '0973223344', 4, 2, 2, '2026-05-09', '2026-05-12', 'da_dat', 'cho_thanh_toan', NULL, 7500000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (67, '301', 'Phung Van Ngoc', 'ngoc.phung@gmail.com', '0901334455', 1, 1, 0, '2026-05-14', '2026-05-16', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (68, '301', 'Patricia Garcia', 'pgarcia.spain@gmail.com', '+34915550144', 2, 2, 0, '2026-05-18', '2026-05-20', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (69, '301', 'Le Bich Ngoc', 'ngoc.le@gmail.com', '0913556677', 3, 2, 1, '2026-05-22', '2026-05-25', 'da_dat', 'cho_thanh_toan', NULL, 7500000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (70, '302', 'Daniel Martinez', 'dmartinez.mex@gmail.com', '+525555550188', 2, 2, 0, '2026-05-02', '2026-05-04', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (71, '302', 'Pham Hong Son', 'son.pham@gmail.com', '0974778899', 4, 3, 1, '2026-05-06', '2026-05-08', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (72, '302', 'Hoang Minh Khue', 'khue.hoang@gmail.com', '0906889900', 1, 1, 0, '2026-05-10', '2026-05-12', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (73, '302', 'Matthew Robinson', 'mrobinson.au@gmail.com', '+61491570333', 2, 2, 0, '2026-05-14', '2026-05-17', 'da_dat', 'cho_thanh_toan', NULL, 7500000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (74, '302', 'Do Thi Mong', 'mong.do@gmail.com', '0917112233', 3, 2, 1, '2026-05-19', '2026-05-21', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (75, '302', 'Ngo Xuan Bach', 'bach.ngo@gmail.com', '0934223344', 2, 2, 0, '2026-05-23', '2026-05-26', 'da_dat', 'cho_thanh_toan', NULL, 7500000.00, NULL, '2026-04-02 17:12:44.051347', '2026-04-02 17:12:44.051347');
INSERT INTO public.bookings VALUES (76, '101', 'An Nguyen', 'an@gmail.com', '0901000001', 2, 2, 0, '2026-06-01', '2026-06-03', 'da_tra_phong', 'da_thanh_toan', 'Cash', 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (77, '101', 'Binh Tran', 'binh@gmail.com', '0901000002', 3, 2, 1, '2026-06-04', '2026-06-06', 'da_tra_phong', 'da_thanh_toan', 'Credit', 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (78, '101', 'Chi Le', 'chi@gmail.com', '0901000003', 2, 2, 0, '2026-06-07', '2026-06-09', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (79, '101', 'Dung Pham', 'dung@gmail.com', '0901000004', 1, 1, 0, '2026-06-10', '2026-06-11', 'da_tra_phong', 'da_thanh_toan', 'Cash', 1200000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (80, '101', 'Huy Vo', 'huy@gmail.com', '0901000005', 2, 2, 0, '2026-06-12', '2026-06-14', 'da_dat', 'cho_thanh_toan', NULL, 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (81, '102', 'Khanh Bui', 'khanh@gmail.com', '0901000006', 2, 2, 0, '2026-06-01', '2026-06-03', 'da_tra_phong', 'da_thanh_toan', 'Cash', 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (82, '102', 'Lam Ho', 'lam@gmail.com', '0901000007', 4, 2, 2, '2026-06-04', '2026-06-06', 'da_tra_phong', 'da_thanh_toan', 'Credit', 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (83, '102', 'Minh Ngo', 'minh@gmail.com', '0901000008', 2, 2, 0, '2026-06-07', '2026-06-09', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (84, '102', 'Nam Phan', 'nam@gmail.com', '0901000009', 3, 2, 1, '2026-06-10', '2026-06-12', 'da_dat', 'cho_thanh_toan', NULL, 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (85, '102', 'Oanh Truong', 'oanh@gmail.com', '0901000010', 2, 2, 0, '2026-06-13', '2026-06-15', 'da_dat', 'cho_thanh_toan', NULL, 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (86, '201', 'Peter Tran', 'peter@gmail.com', '0901000011', 2, 2, 0, '2026-06-01', '2026-06-03', 'da_tra_phong', 'da_thanh_toan', 'Credit', 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (87, '201', 'Quang Le', 'quang@gmail.com', '0901000012', 3, 2, 1, '2026-06-04', '2026-06-07', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 5400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (88, '201', 'Rita Pham', 'rita@gmail.com', '0901000013', 2, 2, 0, '2026-06-08', '2026-06-10', 'da_dat', 'da_thanh_toan', 'Credit', 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (89, '201', 'Son Do', 'son@gmail.com', '0901000014', 4, 2, 2, '2026-06-11', '2026-06-14', 'da_dat', 'cho_thanh_toan', NULL, 5400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (90, '201', 'Trang Vu', 'trang@gmail.com', '0901000015', 2, 2, 0, '2026-06-15', '2026-06-17', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (91, '202', 'Uyen Ho', 'uyen@gmail.com', '0901000016', 2, 2, 0, '2026-06-01', '2026-06-03', 'da_tra_phong', 'da_thanh_toan', 'Cash', 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (92, '202', 'Vinh Nguyen', 'vinh@gmail.com', '0901000017', 3, 2, 1, '2026-06-04', '2026-06-06', 'da_tra_phong', 'da_thanh_toan', 'Credit', 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (93, '202', 'Wyatt Lee', 'wyatt@gmail.com', '0901000018', 2, 2, 0, '2026-06-07', '2026-06-09', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (94, '202', 'Xuan Tran', 'xuan@gmail.com', '0901000019', 4, 2, 2, '2026-06-10', '2026-06-13', 'da_dat', 'cho_thanh_toan', NULL, 5400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (95, '202', 'Yen Pham', 'yen@gmail.com', '0901000020', 2, 2, 0, '2026-06-14', '2026-06-16', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (96, '301', 'Alex Kim', 'alex@gmail.com', '0901000021', 2, 2, 0, '2026-06-01', '2026-06-03', 'da_tra_phong', 'da_thanh_toan', 'Credit', 5000000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (97, '301', 'Brian Ho', 'brian@gmail.com', '0901000022', 3, 2, 1, '2026-06-04', '2026-06-06', 'da_tra_phong', 'da_thanh_toan', 'Transfer', 5000000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (98, '301', 'Chris Le', 'chris@gmail.com', '0901000023', 4, 2, 2, '2026-06-07', '2026-06-10', 'da_dat', 'cho_thanh_toan', NULL, 7500000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (99, '301', 'David Tran', 'david@gmail.com', '0901000024', 2, 2, 0, '2026-06-11', '2026-06-13', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (100, '301', 'Ethan Pham', 'ethan@gmail.com', '0901000025', 3, 2, 1, '2026-06-14', '2026-06-17', 'da_dat', 'cho_thanh_toan', NULL, 7500000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (101, '302', 'Fiona Vo', 'fiona@gmail.com', '0901000026', 2, 2, 0, '2026-06-01', '2026-06-03', 'da_tra_phong', 'da_thanh_toan', 'Cash', 5000000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (102, '302', 'George Bui', 'george@gmail.com', '0901000027', 4, 3, 1, '2026-06-04', '2026-06-06', 'da_tra_phong', 'da_thanh_toan', 'Credit', 5000000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (103, '302', 'Hannah Tran', 'hannah@gmail.com', '0901000028', 2, 2, 0, '2026-06-07', '2026-06-09', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (104, '302', 'Ivan Ho', 'ivan@gmail.com', '0901000029', 3, 2, 1, '2026-06-10', '2026-06-12', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (105, '302', 'Jack Pham', 'jack@gmail.com', '0901000030', 2, 2, 0, '2026-06-13', '2026-06-16', 'da_dat', 'cho_thanh_toan', NULL, 7500000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (106, '101', 'Khoa Nguyen', 'khoa@gmail.com', '0901000031', 2, 2, 0, '2026-07-01', '2026-07-03', 'da_dat', 'cho_thanh_toan', NULL, 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (107, '102', 'Linh Tran', 'linh@gmail.com', '0901000032', 3, 2, 1, '2026-07-01', '2026-07-04', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (108, '103', 'Minh Hoang', 'minh@gmail.com', '0901000033', 2, 2, 0, '2026-07-02', '2026-07-04', 'da_dat', 'cho_thanh_toan', NULL, 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (109, '201', 'Nam Nguyen', 'nam@gmail.com', '0901000034', 2, 2, 0, '2026-07-02', '2026-07-05', 'da_dat', 'cho_thanh_toan', NULL, 5400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (110, '202', 'Oanh Le', 'oanh@gmail.com', '0901000035', 3, 2, 1, '2026-07-03', '2026-07-05', 'da_dat', 'cho_thanh_toan', NULL, 3600000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (111, '203', 'Phuc Tran', 'phuc@gmail.com', '0901000036', 2, 2, 0, '2026-07-03', '2026-07-06', 'da_dat', 'cho_thanh_toan', NULL, 5400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (112, '301', 'Quynh Pham', 'quynh@gmail.com', '0901000037', 2, 2, 0, '2026-07-04', '2026-07-06', 'da_dat', 'cho_thanh_toan', NULL, 5000000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (113, '302', 'Son Vo', 'son@gmail.com', '0901000038', 3, 2, 1, '2026-07-04', '2026-07-07', 'da_dat', 'cho_thanh_toan', NULL, 7500000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (114, '101', 'Trang Le', 'trang@gmail.com', '0901000039', 2, 2, 0, '2026-07-05', '2026-07-07', 'da_dat', 'cho_thanh_toan', NULL, 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');
INSERT INTO public.bookings VALUES (115, '102', 'Uyen Tran', 'uyen@gmail.com', '0901000040', 2, 2, 0, '2026-07-06', '2026-07-08', 'da_dat', 'cho_thanh_toan', NULL, 2400000.00, NULL, '2026-04-02 17:12:53.725958', '2026-04-02 17:12:53.725958');


--
-- TOC entry 5318 (class 0 OID 0)
-- Dependencies: 220
-- Name: bookings_booking_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.bookings_booking_id_seq', 115, true);


--
-- TOC entry 5158 (class 2606 OID 17191)
-- Name: bookings bookings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT bookings_pkey PRIMARY KEY (booking_id);


--
-- TOC entry 5160 (class 2606 OID 17197)
-- Name: bookings no_overlap; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT no_overlap EXCLUDE USING gist (room_number WITH =, daterange(check_in, check_out, '[)'::text) WITH &&);


--
-- TOC entry 5161 (class 2620 OID 17200)
-- Name: bookings trigger_update_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_updated_at BEFORE UPDATE ON public.bookings FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();


-- Completed on 2026-04-02 19:51:51

--
-- PostgreSQL database dump complete
--

\unrestrict J9iPRaw1BcnBfJIWh7DB6yKWglups4hjMxWf8Txrwj1GBYxeGgg7Cy4xh0y6Y0q

