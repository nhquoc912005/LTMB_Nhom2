-- Functional fixes for Android Java + Express + Supabase/PostgreSQL flow.
-- Safe to run repeatedly on Supabase/PostgreSQL.

ALTER TABLE public.hoa_don
  ADD COLUMN IF NOT EXISTS id_luutru integer REFERENCES public.luu_tru(id_luutru);

ALTER TABLE public.hoa_don
  ADD COLUMN IF NOT EXISTS ghi_chu text;

ALTER TABLE public.hoa_don
  ADD COLUMN IF NOT EXISTS yeu_cau_vat boolean NOT NULL DEFAULT false;

ALTER TABLE public.luu_tru
  ADD COLUMN IF NOT EXISTS ghi_chu text;

ALTER TABLE public.tai_khoan
  ADD COLUMN IF NOT EXISTS trang_thai character varying DEFAULT 'Hoạt động';
