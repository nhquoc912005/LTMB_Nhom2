-- Optional migration for a more complete check-in/change-room workflow.
-- Review before running in production.

ALTER TABLE public.luu_tru
ADD COLUMN IF NOT EXISTS ghi_chu TEXT;

CREATE TABLE IF NOT EXISTS public.lich_su_doi_phong (
    id_lich_su_doi_phong SERIAL PRIMARY KEY,
    ma_dat_phong INTEGER NOT NULL REFERENCES public.dat_phong(ma_dat_phong),
    id_phong_cu INTEGER NOT NULL REFERENCES public.phong(id_phong),
    id_phong_moi INTEGER NOT NULL REFERENCES public.phong(id_phong),
    ly_do TEXT,
    thoi_gian_doi TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_letan INTEGER REFERENCES public.le_tan(id_letan)
);

-- Recommended business status values:
-- phong.trang_thai: AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE
-- dat_phong.trang_thai: PENDING, CONFIRMED, CHECKED_IN, COMPLETED, CANCELLED
