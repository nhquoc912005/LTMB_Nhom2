-- PostgreSQL Database Schema for Hotel Management System

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
