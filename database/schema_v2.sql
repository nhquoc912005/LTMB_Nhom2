-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.chi_tiet_dat_phong (
  id_ct_dat_phong integer NOT NULL DEFAULT nextval('chi_tiet_dat_phong_id_ct_dat_phong_seq'::regclass),
  id_phong integer,
  ma_dat_phong character varying,
  so_luong_phong integer DEFAULT 1,
  CONSTRAINT chi_tiet_dat_phong_pkey PRIMARY KEY (id_ct_dat_phong),
  CONSTRAINT chi_tiet_dat_phong_id_phong_fkey FOREIGN KEY (id_phong) REFERENCES public.phong(id_phong),
  CONSTRAINT chi_tiet_dat_phong_ma_dat_phong_fkey FOREIGN KEY (ma_dat_phong) REFERENCES public.dat_phong(ma_dat_phong)
);
CREATE TABLE public.dat_phong (
  ma_dat_phong character varying NOT NULL,
  email character varying,
  ngay_nhan timestamp without time zone NOT NULL,
  ngay_tra timestamp without time zone NOT NULL,
  phuong_thuc_thanh_toan character varying,
  sdt_nguoi_dat character varying,
  so_nguoi_lon integer,
  so_phong character varying,
  so_tre_em integer,
  ten_nguoi_dat character varying,
  tien_coc numeric,
  tong_so_nguoi integer,
  tong_thanh_toan numeric,
  trang_thai character varying,
  id_kh integer,
  ghi_chu text,
  CONSTRAINT dat_phong_pkey PRIMARY KEY (ma_dat_phong),
  CONSTRAINT fk9vm0pybe6y9f3acv39vm75lsk FOREIGN KEY (id_kh) REFERENCES public.khach_hang(id_kh)
);
CREATE TABLE public.dich_vu (
  id_dichvu integer NOT NULL DEFAULT nextval('dich_vu_id_dichvu_seq'::regclass),
  ten_dich_vu character varying NOT NULL,
  don_gia numeric NOT NULL,
  CONSTRAINT dich_vu_pkey PRIMARY KEY (id_dichvu)
);
CREATE TABLE public.hoa_don (
  id_hoadon integer NOT NULL DEFAULT nextval('hoa_don_id_hoadon_seq'::regclass),
  tong_tien numeric DEFAULT 0,
  trang_thai character varying,
  ngay_lap timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT hoa_don_pkey PRIMARY KEY (id_hoadon)
);
CREATE TABLE public.khach_hang (
  id_kh integer NOT NULL DEFAULT nextval('khach_hang_id_kh_seq'::regclass),
  ho_ten character varying NOT NULL,
  sdt character varying,
  cccd character varying UNIQUE,
  CONSTRAINT khach_hang_pkey PRIMARY KEY (id_kh)
);
CREATE TABLE public.le_tan (
  id_letan integer NOT NULL DEFAULT nextval('le_tan_id_letan_seq'::regclass),
  id_taikhoan integer,
  CONSTRAINT le_tan_pkey PRIMARY KEY (id_letan),
  CONSTRAINT le_tan_id_taikhoan_fkey FOREIGN KEY (id_taikhoan) REFERENCES public.tai_khoan(id_taikhoan)
);
CREATE TABLE public.luu_tru (
  id_luutru integer NOT NULL DEFAULT nextval('luu_tru_id_luutru_seq'::regclass),
  ma_dat_phong character varying,
  thoi_gian_checkin_thuc_te timestamp without time zone DEFAULT now(),
  thoi_gian_checkout_thuc_te timestamp without time zone,
  so_nguoi_thuc_te integer,
  CONSTRAINT luu_tru_pkey PRIMARY KEY (id_luutru),
  CONSTRAINT luu_tru_ma_dat_phong_fkey FOREIGN KEY (ma_dat_phong) REFERENCES public.dat_phong(ma_dat_phong)
);
CREATE TABLE public.phong (
  id_phong integer NOT NULL DEFAULT nextval('phong_id_phong_seq'::regclass),
  ten_phong character varying NOT NULL,
  loai_phong character varying,
  suc_chua integer,
  gia_phong numeric,
  trang_thai character varying,
  CONSTRAINT phong_pkey PRIMARY KEY (id_phong)
);
CREATE TABLE public.su_dung_dich_vu (
  id_sudung_dv integer NOT NULL DEFAULT nextval('su_dung_dich_vu_id_sudung_dv_seq'::regclass),
  soluong integer DEFAULT 1,
  thoi_gian timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  thanh_tien numeric,
  id_dichvu integer,
  id_luutru integer,
  id_hoadon integer,
  CONSTRAINT su_dung_dich_vu_pkey PRIMARY KEY (id_sudung_dv),
  CONSTRAINT su_dung_dich_vu_id_dichvu_fkey FOREIGN KEY (id_dichvu) REFERENCES public.dich_vu(id_dichvu),
  CONSTRAINT su_dung_dich_vu_id_hoadon_fkey FOREIGN KEY (id_hoadon) REFERENCES public.hoa_don(id_hoadon)
);
CREATE TABLE public.tai_khoan (
  id_taikhoan integer NOT NULL DEFAULT nextval('tai_khoan_id_taikhoan_seq'::regclass),
  ten_dang_nhap character varying NOT NULL UNIQUE,
  mat_khau character varying NOT NULL,
  ho_ten character varying,
  id_vaitro integer,
  id_nhan_vien integer,
  id_vai_tro integer,
  id bigint NOT NULL DEFAULT nextval('tai_khoan_id_seq'::regclass),
  chuc_vu character varying,
  email character varying,
  gioi_tinh character varying,
  ngay_sinh character varying,
  so_dien_thoai character varying,
  CONSTRAINT tai_khoan_pkey PRIMARY KEY (id_taikhoan),
  CONSTRAINT tai_khoan_id_vaitro_fkey FOREIGN KEY (id_vaitro) REFERENCES public.vai_tro(id_vaitro)
);
CREATE TABLE public.tai_san (
  id_taisan integer NOT NULL DEFAULT nextval('tai_san_id_taisan_seq'::regclass),
  ten_tai_san character varying NOT NULL,
  gia_tri_boi_thuong numeric,
  id_phong integer,
  CONSTRAINT tai_san_pkey PRIMARY KEY (id_taisan),
  CONSTRAINT tai_san_id_phong_fkey FOREIGN KEY (id_phong) REFERENCES public.phong(id_phong)
);
CREATE TABLE public.thanh_toan (
  id_thanhtoan integer NOT NULL DEFAULT nextval('thanh_toan_id_thanhtoan_seq'::regclass),
  so_tien numeric NOT NULL,
  phuong_thuc character varying,
  trang_thai character varying,
  id_hoadon integer,
  CONSTRAINT thanh_toan_pkey PRIMARY KEY (id_thanhtoan),
  CONSTRAINT thanh_toan_id_hoadon_fkey FOREIGN KEY (id_hoadon) REFERENCES public.hoa_don(id_hoadon)
);
CREATE TABLE public.thiet_hai (
  id_thie_thai integer NOT NULL DEFAULT nextval('thiet_hai_id_thie_thai_seq'::regclass),
  muc_do character varying,
  so_tien_boi_thuong numeric,
  trang_thai character varying,
  id_taisan integer,
  id_luutru integer,
  CONSTRAINT thiet_hai_pkey PRIMARY KEY (id_thie_thai),
  CONSTRAINT thiet_hai_id_taisan_fkey FOREIGN KEY (id_taisan) REFERENCES public.tai_san(id_taisan)
);
CREATE TABLE public.users (
  id bigint NOT NULL DEFAULT nextval('users_id_seq'::regclass),
  email character varying NOT NULL UNIQUE,
  name character varying NOT NULL,
  CONSTRAINT users_pkey PRIMARY KEY (id)
);
CREATE TABLE public.vai_tro (
  id_vaitro integer NOT NULL DEFAULT nextval('vai_tro_id_vaitro_seq'::regclass),
  ten_vaitro character varying NOT NULL UNIQUE,
  CONSTRAINT vai_tro_pkey PRIMARY KEY (id_vaitro)
);