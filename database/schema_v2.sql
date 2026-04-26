-- 1. VaiTro (Role for Permissions)
CREATE TABLE vai_tro (
    id_vaitro SERIAL PRIMARY KEY,
    ten_vaitro VARCHAR(50) UNIQUE NOT NULL
);

-- 2. TaiKhoan (User Accounts)
CREATE TABLE tai_khoan (
    id_taikhoan SERIAL PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) UNIQUE NOT NULL,
    mat_khau VARCHAR(255) NOT NULL,
    ho_ten VARCHAR(255),
    id_vaitro INTEGER REFERENCES vai_tro(id_vaitro)
);

-- 3. LeTan (Receptionist linked to Account)
CREATE TABLE le_tan (
    id_letan SERIAL PRIMARY KEY,
    id_taikhoan INTEGER REFERENCES tai_khoan(id_taikhoan)
);

-- 2. Phong (Room)
CREATE TABLE phong (
    id_phong SERIAL PRIMARY KEY,
    ten_phong VARCHAR(100) NOT NULL,
    loai_phong VARCHAR(50),
    suc_chua INTEGER,
    gia_phong DECIMAL(15, 2),
    trang_thai VARCHAR(50) -- e.g., 'AVAILABLE', 'OCCUPIED', 'MAINTENANCE'
);

-- 3. KhachHang (Customer)
CREATE TABLE khach_hang (
    id_kh SERIAL PRIMARY KEY,
    ho_ten VARCHAR(255) NOT NULL,
    sdt VARCHAR(15),
    cccd VARCHAR(20) UNIQUE
);

-- 4. DatPhong (Booking)
CREATE TABLE dat_phong (
    ma_dat_phong SERIAL PRIMARY KEY,
    ngay_nhan TIMESTAMP NOT NULL,          -- Expected Check-in
    ngay_tra TIMESTAMP NOT NULL,           -- Expected Check-out
    so_nguoi_lon INTEGER DEFAULT 1,
    so_tre_em INTEGER DEFAULT 0,
    tong_so_nguoi INTEGER GENERATED ALWAYS AS (so_nguoi_lon + so_tre_em) STORED,
    so_phong VARCHAR(50),                  -- Room number(s) string for quick reference
    trang_thai VARCHAR(50),                -- e.g., 'PENDING', 'CONFIRMED', 'CANCELLED', 'CHECKED_IN', 'COMPLETED'
    ten_nguoi_dat VARCHAR(255),
    email VARCHAR(255),
    sdt_nguoi_dat VARCHAR(15),
    tien_coc DECIMAL(15, 2) DEFAULT 0,
    tong_thanh_toan DECIMAL(15, 2) DEFAULT 0,
    id_letan INTEGER REFERENCES le_tan(id_letan),
    id_kh INTEGER REFERENCES khach_hang(id_kh)
);

-- 5. ChiTietDatPhong (Booking Details - Many-to-Many between DatPhong and Phong)
CREATE TABLE chi_tiet_dat_phong (
    id_ct_dat_phong SERIAL PRIMARY KEY,
    id_phong INTEGER REFERENCES phong(id_phong),
    ma_dat_phong INTEGER REFERENCES dat_phong(ma_dat_phong),
    so_luong_phong INTEGER DEFAULT 1
);

-- 6. LuuTru (Stay Records / Actual Check-in/out)
CREATE TABLE luu_tru (
    id_luutru SERIAL PRIMARY KEY,
    ma_dat_phong INTEGER REFERENCES dat_phong(ma_dat_phong),
    thoi_gian_checkin_thuc_te TIMESTAMP,
    thoi_gian_checkout_thuc_te TIMESTAMP,
    so_nguoi_thuc_te INTEGER
);

-- 7. DichVu (Services)
CREATE TABLE dich_vu (
    id_dichvu SERIAL PRIMARY KEY,
    ten_dich_vu VARCHAR(255) NOT NULL,
    don_gia DECIMAL(15, 2) NOT NULL
);

-- 8. HoaDon (Invoices)
CREATE TABLE hoa_don (
    id_hoadon SERIAL PRIMARY KEY,
    tong_tien DECIMAL(15, 2) DEFAULT 0,
    trang_thai VARCHAR(50), -- e.g., 'UNPAID', 'PAID', 'CANCELLED'
    ngay_lap TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. SuDungDichVu (Service Usage)
CREATE TABLE su_dung_dich_vu (
    id_sudung_dv SERIAL PRIMARY KEY,
    soluong INTEGER DEFAULT 1,
    thoi_gian TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    thanh_tien DECIMAL(15, 2),
    id_dichvu INTEGER REFERENCES dich_vu(id_dichvu),
    id_luutru INTEGER REFERENCES luu_tru(id_luutru),
    id_hoadon INTEGER REFERENCES hoa_don(id_hoadon)
);

-- 10. ThanhToan (Payments)
CREATE TABLE thanh_toan (
    id_thanhtoan SERIAL PRIMARY KEY,
    so_tien DECIMAL(15, 2) NOT NULL,
    phuong_thuc VARCHAR(50), -- e.g., 'CASH', 'CREDIT_CARD', 'TRANSFER'
    trang_thai VARCHAR(50),
    id_hoadon INTEGER REFERENCES hoa_don(id_hoadon)
);

-- 11. TaiSan (Assets in Rooms)
CREATE TABLE tai_san (
    id_taisan SERIAL PRIMARY KEY,
    ten_tai_san VARCHAR(255) NOT NULL,
    gia_tri_boi_thuong DECIMAL(15, 2),
    id_phong INTEGER REFERENCES phong(id_phong)
);

-- 12. ThietHai (Damages during Stay)
CREATE TABLE thiet_hai (
    id_thie_thai SERIAL PRIMARY KEY,
    muc_do VARCHAR(50),
    so_tien_boi_thuong DECIMAL(15, 2),
    trang_thai VARCHAR(50),
    id_taisan INTEGER REFERENCES tai_san(id_taisan),
    id_luutru INTEGER REFERENCES luu_tru(id_luutru)
);

-- 13. Service / Compensation catalog used by the mobile service assignment flow
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    price NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (price >= 0),
    unit TEXT,
    icon TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    price NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (price >= 0),
    unit TEXT,
    icon TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS room_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id INTEGER NOT NULL REFERENCES phong(id_phong) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    price NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
    total_price NUMERIC(15, 2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS room_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id INTEGER NOT NULL REFERENCES phong(id_phong) ON DELETE CASCADE,
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    price NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
    total_price NUMERIC(15, 2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_services_name_lower ON services (LOWER(name));
CREATE INDEX IF NOT EXISTS idx_assets_name_lower ON assets (LOWER(name));
CREATE INDEX IF NOT EXISTS idx_room_services_room_id ON room_services(room_id);
CREATE INDEX IF NOT EXISTS idx_room_assets_room_id ON room_assets(room_id);

DROP TRIGGER IF EXISTS trg_services_updated_at ON services;
CREATE TRIGGER trg_services_updated_at
    BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_assets_updated_at ON assets;
CREATE TRIGGER trg_assets_updated_at
    BEFORE UPDATE ON assets
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_room_services_updated_at ON room_services;
CREATE TRIGGER trg_room_services_updated_at
    BEFORE UPDATE ON room_services
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_room_assets_updated_at ON room_assets;
CREATE TRIGGER trg_room_assets_updated_at
    BEFORE UPDATE ON room_assets
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

INSERT INTO services (name, price)
SELECT ten_dich_vu, COALESCE(don_gia, 0)
FROM dich_vu
WHERE ten_dich_vu IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM services);

INSERT INTO assets (name, price)
SELECT ten_tai_san, MAX(COALESCE(gia_tri_boi_thuong, 0))
FROM tai_san
WHERE ten_tai_san IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM assets)
GROUP BY ten_tai_san;
