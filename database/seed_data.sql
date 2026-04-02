-- PostgreSQL Extensive Seed Data for schema_v2 (VIETNAMESE)
-- Master Tables: 20+ records each
-- Transactional Tables: 100+ records each

-- 1. vai_tro (20+ records)
INSERT INTO vai_tro (ten_vaitro) VALUES 
('Quản trị viên'), ('Quản lý'), ('Lễ tân'), ('Kế toán'), ('Nhân viên buồng phòng'),
('Nhân viên bảo trì'), ('Nhân viên nhà hàng'), ('Nhân viên bar'), ('Nhân viên kỹ thuật'), ('Nhân viên an ninh'),
('Nhân viên spa'), ('Nhân viên gym'), ('Nhân viên tour'), ('Nhân viên lái xe'), ('Nhân viên hành chính'),
('Nhân viên nhân sự'), ('Nhân viên marketing'), ('Nhân viên bán hàng'), ('Nhân viên hỗ trợ khách hàng'), ('Nhân viên đón tiếp');

-- 2. tai_khoan (20+ records)
INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, ho_ten, id_vaitro) VALUES 
('admin_hotel', 'pass_123', 'Người Quản Trị Hệ Thống', 1),
('mgr_nguyen', 'pass_456', 'Nguyễn Thị Hương', 2),
('recept_lan', 'pass_789', 'Lê Thị Lan', 3),
('recept_mai', 'pass_101', 'Phạm Hồng Mai', 3),
('recept_cuc', 'pass_202', 'Nguyễn Thu Cúc', 3),
('staff_001', 'pass_303', 'Trần Văn Mạnh', 5),
('staff_002', 'pass_404', 'Lê Văn Lộc', 6),
('staff_003', 'pass_505', 'Vũ Thị Diễm', 7),
('staff_004', 'pass_606', 'Phan Văn Hưng', 10),
('staff_005', 'pass_707', 'Bùi Văn Tiến', 11),
('staff_006', 'pass_808', 'Nguyễn Minh Tuấn', 12),
('staff_007', 'pass_909', 'Lê Văn Tài', 14),
('staff_008', 'pass_211', 'Trần Thị Thuỷ', 15),
('staff_009', 'pass_311', 'Phạm Như Ý', 17),
('staff_010', 'pass_411', 'Nguyễn Hải Đăng', 19),
('staff_011', 'pass_511', 'Lê Quốc Anh', 18),
('staff_012', 'pass_611', 'Vũ Đông Hồ', 9),
('staff_013', 'pass_711', 'Đặng Thành Long', 8),
('staff_014', 'pass_811', 'Hoàng Bảo Nam', 4),
('staff_015', 'pass_911', 'Lê Tuyết Trang', 16);

-- 3. le_tan (Linked to accounts)
INSERT INTO le_tan (id_taikhoan) VALUES (3), (4), (5);

-- 4. phong (20 records)
INSERT INTO phong (ten_phong, loai_phong, suc_chua, gia_phong, trang_thai) VALUES 
('101', 'Phòng Đơn Tiêu Chuẩn', 1, 400000.00, 'Trống'), ('102', 'Phòng Đơn Tiêu Chuẩn', 1, 400000.00, 'Trống'), ('103', 'Phòng Đôi Tiêu Chuẩn', 2, 700000.00, 'Trống'),
('104', 'Phòng Đôi Tiêu Chuẩn', 2, 700000.00, 'Đã đặt'), ('105', 'Phòng Ba Tiêu Chuẩn', 3, 1000000.00, 'Trống'), ('201', 'Phòng Đơn Cao Cấp', 1, 600000.00, 'Trống'),
('202', 'Phòng Đơn Cao Cấp', 1, 600000.00, 'Bảo trì'), ('203', 'Phòng Đôi Cao Cấp', 2, 1000000.00, 'Đang lưu trú'), ('204', 'Phòng Đôi Cao Cấp', 2, 1000000.00, 'Trống'),
('205', 'Phòng Ba Cao Cấp', 3, 1500000.00, 'Trống'), ('301', 'Phòng Superior Đơn', 1, 800000.00, 'Trống'), ('302', 'Phòng Superior Đơn', 1, 800000.00, 'Trống'),
('303', 'Phòng Superior Đôi', 2, 1400000.00, 'Bảo trì'), ('304', 'Phòng Superior Đôi', 2, 1400000.00, 'Trống'), ('305', 'Phòng Superior Ba', 3, 2000000.00, 'Trống'),
('401', 'Phòng Suite', 2, 3000000.00, 'Trống'), ('402', 'Phòng Suite', 2, 3000000.00, 'Trống'), ('403', 'Phòng Tổng Thống', 4, 10000000.00, 'Trống'),
('501', 'Phòng Tiết Kiệm', 1, 300000.00, 'Trống'), ('502', 'Phòng Tiết Kiệm', 1, 300000.00, 'Trống');

-- 5. khach_hang (20 records)
INSERT INTO khach_hang (ho_ten, sdt, cccd) VALUES 
('Nguyễn Văn An', '0901234560', '001090000000'), ('Lê Thị Bình', '0901234561', '001090000001'), ('Trần Văn Cường', '0901234562', '001090000002'),
('Phạm Văn Dũng', '0901234563', '001090000003'), ('Hoàng Thị Em', '0901234564', '001090000004'), ('Đỗ Văn Phúc', '0901234565', '001090000005'),
('Vũ Thị Gấm', '0901234566', '001090000006'), ('Bùi Văn Hùng', '0901234567', '001090000007'), ('Đặng Thị Hoa', '0901234568', '001090000008'),
('Ngô Văn Nam', '0901234569', '001090000009'), ('Lý Thị Kim', '0911234560', '001090000010'), ('Dương Văn Lợi', '0911234561', '001090000011'),
('Quách Thị Mai', '0911234562', '001090000012'), ('Mai Văn Nhân', '0911234563', '001090000013'), ('Đinh Thị Oanh', '0911234564', '001090000014'),
('Lương Văn Phương', '0911234565', '001090000015'), ('Trịnh Thị Quỳnh', '0911234566', '001090000016'), ('Hà Văn Rạng', '0911234567', '001090000017'),
('Phan Thị Sang', '0911234568', '001090000018'), ('Bùi Văn Tâm', '0911234569', '001090000019');

-- 6. dich_vu (20 records)
INSERT INTO dich_vu (ten_dich_vu, don_gia) VALUES 
('Buffet Sáng', 150000.00), ('Buffet Tối', 250000.00), ('Giặt Là Phổ Thông', 50000.00), ('Giặt Là Lấy Ngay', 100000.00),
('Xe Đưa Đón Sân Bay', 350000.00), ('Spa - Toàn Thân', 500000.00), ('Spa - Chăm Sóc Da Mặt', 30000.00), ('Phòng Tập Gym Theo Giờ', 100000.00),
('Vé Bể Bơi', 50000.00), ('Phí Phục Vụ Tại Phòng', 30000.00), ('Mini Bar - Nước Ngọt', 20000.00), ('Mini Bar - Bia', 35000.00),
('Mini Bar - Snack', 15000.00), ('Cho Thuê Xe Máy', 20000.00), ('Hướng Dẫn Viên Du Lịch', 500000.00), ('Dịch Vụ Phiên Dịch', 300000.00),
('Trông Trẻ', 200000.00), ('Bác Sĩ Theo Yêu Cầu', 1000000.00), ('Dịch Vụ Thư Ký', 400000.00), ('Photo/In Ấn', 50000.00);

-- 7. tai_san (20+ records)
INSERT INTO tai_san (ten_tai_san, gia_tri_boi_thuong, id_phong) VALUES 
('Tivi Thông Minh 43"', 10000000.00, 1), ('Điều Hòa', 8000000.00, 1), ('Tủ Lạnh Mini', 3000000.00, 1), ('Ấm Đun Nước Điện', 500000.00, 1),
('Bình Nóng Lạnh', 2000000.00, 1), ('Giường Đơn Em Bé', 3000000.00, 2), ('Giường Đôi Lớn', 8000000.00, 3), ('Bàn Làm Việc', 1500000.00, 4),
('Bộ Sofa', 12000000.00, 16), ('Bàn Ăn', 5000000.00, 16), ('Lò Vi Sóng', 2500000.00, 16), ('Máy Pha Cafe', 7000000.00, 18),
('Két Sắt', 2000000.00, 6), ('Máy Sấy Tóc', 400000.00, 7), ('Điện Thoại Bàn', 300000.00, 8), ('Bức Tranh Trang Trí', 2000000.00, 17),
('Bình Hoa Pha Lê', 5000000.00, 18), ('Bồn Tắm', 15000000.00, 18), ('Đồng Hồ Treo Tường', 300000.00, 9), ('Đèn Đọc Sách', 600000.00, 10),
('Tủ Quần Áo', 4000000.00, 11);

-- 8. dat_phong (100 records)
INSERT INTO dat_phong (ngay_nhan, ngay_tra, so_nguoi_lon, so_tre_em, so_phong, trang_thai, ten_nguoi_dat, email, sdt_nguoi_dat, id_letan, id_kh)
SELECT 
    NOW() + (random() * interval '30 days') - interval '15 days', 
    NOW() + (random() * interval '35 days'), 
    (random()*2+1)::int, 
    (random()*2)::int, 
    (CASE WHEN random() < 0.5 THEN 'P' ELSE 'R' END) || (random()*9)::int || (random()*9)::int, 
    (ARRAY['Chờ check-in', 'Đã check-in', 'Đã hủy'])[floor(random() * 3) + 1],
    'Khách Hàng ' || i, 
    'khach'||i||'@email.vn', 
    '09'||(random()*100000000+100000000)::bigint::text, 
    (random()*2+1)::int, 
    (random()*19+1)::int
FROM generate_series(1, 100) s(i);

-- 9. chi_tiet_dat_phong (100+ records)
INSERT INTO chi_tiet_dat_phong (id_phong, ma_dat_phong, so_luong_phong)
SELECT (random()*19+1)::int, s.i, 1
FROM generate_series(1, 100) s(i);

-- 10. luu_tru (100 records)
INSERT INTO luu_tru (ma_dat_phong, thoi_gian_checkin_thuc_te, thoi_gian_checkout_thuc_te, so_nguoi_thuc_te)
SELECT s.i, NOW() - (random() * interval '5 days'), NOW(), (random()*4+1)::int
FROM generate_series(1, 100) s(i);

-- 11. hoa_don (100 records)
INSERT INTO hoa_don (tong_tien, trang_thai, ngay_lap)
SELECT (random()*10000000)::decimal, (CASE WHEN random() < 0.7 THEN 'Đã thanh toán' ELSE 'Chưa thanh toán' END), NOW() - (random() * interval '10 days')
FROM generate_series(1, 100) s(i);

-- 12. su_dung_dich_vu (100 records)
INSERT INTO su_dung_dich_vu (soluong, thanh_tien, id_dichvu, id_luutru, id_hoadon)
SELECT (random()*5+1)::int, (random()*500000)::decimal, (random()*19+1)::int, (random()*99+1)::int, (random()*99+1)::int
FROM generate_series(1, 100) s(i);

-- 13. thanh_toan (100 records)
INSERT INTO thanh_toan (so_tien, phuong_thuc, trang_thai, id_hoadon)
SELECT (random()*10000000)::decimal, (ARRAY['Tiền mặt', 'Chuyển khoản', 'Thẻ'])[floor(random() * 3) + 1], 'Thành công', s.i
FROM generate_series(1, 100) s(i);

-- 14. thiet_hai (100 records)
INSERT INTO thiet_hai (muc_do, so_tien_boi_thuong, trang_thai, id_taisan, id_luutru)
SELECT 
    (ARRAY['Nhẹ', 'Trung bình', 'Nặng'])[floor(random() * 3) + 1], 
    (random()*1000000)::decimal, 
    'Đã giải quyết', 
    (random()*19+1)::int, 
    (random()*99+1)::int
FROM generate_series(1, 100) s(i);
