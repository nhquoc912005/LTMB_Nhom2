-- Optional migration for a complete checkout/payment workflow.
-- Review before running in production or Supabase SQL Editor.

-- Critical: lets /api/v2/invoices/{id_hoadon}/pay resolve the stay from only id_hoadon.
ALTER TABLE public.hoa_don
ADD COLUMN IF NOT EXISTS id_luutru INTEGER REFERENCES public.luu_tru(id_luutru);

-- Store checkout/payment note from dialog_payment_checkout.etNote.
ALTER TABLE public.hoa_don
ADD COLUMN IF NOT EXISTS ghi_chu TEXT;

ALTER TABLE public.luu_tru
ADD COLUMN IF NOT EXISTS ghi_chu TEXT;

-- Minimal support for the "Xuất hoá đơn đỏ" button.
ALTER TABLE public.hoa_don
ADD COLUMN IF NOT EXISTS yeu_cau_vat BOOLEAN NOT NULL DEFAULT FALSE;

-- Full VAT information, if the app later collects company tax data.
CREATE TABLE IF NOT EXISTS public.thong_tin_xuat_vat (
    id_vat SERIAL PRIMARY KEY,
    id_hoadon INTEGER NOT NULL REFERENCES public.hoa_don(id_hoadon),
    ten_cong_ty VARCHAR(255) NOT NULL,
    ma_so_thue VARCHAR(50) NOT NULL,
    dia_chi TEXT,
    email_nhan_hoa_don VARCHAR(255),
    ngay_tao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Recommended room statuses:
-- AVAILABLE, RESERVED, OCCUPIED, CLEANING, MAINTENANCE
