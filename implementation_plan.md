# Kế hoạch phát triển Back-end Check-out và Thanh toán

Dựa trên cấu trúc database `schema_v2.sql`, quy trình Check-out và Thanh toán sẽ có sự liên kết chặt chẽ giữa các đối tượng. Hiện tại mã nguồn ở `index.js` đang dùng chung bảng `bookings` kiểu cũ. Để có tính năng này một cách chuẩn chỉnh với Schema v2, chúng ta cần phát triển luồng API mới như sau.

## User Review Required

> [!IMPORTANT]
> - **Chuyển đổi Schema:** Các API hiện tại đang sử dụng bảng `bookings` cũ. Em đề xuất viết một nhóm API mới với tiền tố **`/api/v2/...`** dành riêng cho hệ thống dùng Schema v2.
> - **Cách tính tiền phòng:** Em sẽ tính tiền phòng dựa trên hiệu số giữa Ngày trả và Ngày nhận thực tế theo bảng `luu_tru` nhân với đơn giá trong bảng `phong`. Anh có muốn thêm quy tắc tính phụ thu trả phòng trễ nào cụ thể không? Nếu không, em sẽ chỉ tính theo công thức mốc ngày (tối thiểu tính 1 ngày).

## Bối cảnh & Quy trình

Quá trình Check-out và Thanh Toán cần tác động đến khoảng **6 bảng** (tables): `dat_phong`, `luu_tru`, `chi_tiet_dat_phong`, `phong`, `hoa_don`, `thanh_toan`.

### Quy trình lý tưởng:
1. **Check-out:** Cập nhật thời gian trả phòng thực tế của khách, thay đổi trạng thái của phòng.
2. **Lập hóa đơn (Invoice):** Tính toán tiền phòng + tổng tiền dịch vụ khách dùng + tổng tiền thiệt hại (nếu có) - số tiền đã đặt cọc.
3. **Thanh toán (Payment):** Khách hàng tiến hành trả số tiền còn lại (theo các phương thức khác nhau) và hệ thống ghi nhận giao dịch.

---

## Proposed Changes

Em sẽ bổ sung mã vào `api/bookings-api/index.js` (hoặc tạo ra file riêng `checkout_payment_router.js`). Chi tiết các API sẽ tạo như sau:

### 1. API Trả Phòng (Check-out)
**Endpoint:** `PUT /api/v2/bookings/:ma_dat_phong/check-out`
- **Logic:**
  - Nhận `ma_dat_phong`.
  - Cập nhật trường `thoi_gian_checkout_thuc_te = CURRENT_TIMESTAMP` đối với bản ghi trong bảng `luu_tru` đang sử dụng `ma_dat_phong` tương ứng.
  - Từ `ma_dat_phong`, truy vấn tìm các ID phòng thông qua bảng `chi_tiet_dat_phong`. Cập nhật `trang_thai` của các `phong` đó thành `AVAILABLE` (hoặc `MAINTENANCE` để làm buồng).
  - Cập nhật trạng thái của `dat_phong` thành `CHECKED_OUT` (hoặc `da_tra_phong`).

### 2. API Lập Hóa Đơn (Tạo Invoice)
**Endpoint:** `POST /api/v2/bookings/:ma_dat_phong/invoice`
- **Logic:**
  - Lấy thông tin giá `phong` từ bảng `phong` kèm số đêm (dựa theo check-in thực tế và check-out thực tế của bảng `luu_tru`). -> Tính ra **Tiền phòng**.
  - Query bảng `su_dung_dich_vu` dựa vào `id_luutru` -> SUM(`thanh_tien`) -> Lấy ra tổng **Tiền dịch vụ**.
  - Query bảng `thiet_hai` dựa vào `id_luutru` -> SUM(`so_tien_boi_thuong`) -> Ra tổng **Tiền thiệt hại/phụ thu**.
  - Tổng hợp `tien_coc` đã thu từ khách trong bảng `dat_phong`.
  - Công thức tiền bill: `Tổng cần trả = Tiền phòng + Tiền dịch vụ + Tiền thiệt hại - Tiền cọc`.
  - Thực hiện `INSERT` hóa đơn mới vào bảng `hoa_don`. Trả về đối tượng hóa đơn vừa lập để Frontend hiển thị lên màn hình (Popup Invoice).

### 3. API Xác nhận Thanh Toán
**Endpoint:** `POST /api/v2/invoices/:id_hoadon/pay`
- **Payload Input:** Dữ liệu thanh toán từ Font-end: `{ phuong_thuc: 'CASH', so_tien: 1500000 }`
- **Logic:**
  - Check xem số tiền thanh toán thực tế có đạt đủ số yêu cầu của hóa đơn hay không.
  - Khởi tạo (INSERT) một bản ghi vào bảng `thanh_toan` chứa thông tin lịch sử.
  - Sửa đổi `trang_thai` của bảng `hoa_don` sang thành `PAID` (đã thanh toán).
  - Hoàn tất mọi thay đổi, trả về kết quả thành công cho Client. Cập nhật `dat_phong` thành `COMPLETED`.

---

## Open Questions

1. Em thấy file `index.js` vẫn đang code rất đơn giản, bảng `bookings` cũ là một database phẳng. Em có nên **viết lại toàn bộ** backend dựa vào hệ DB mới này (tạo API check-in, book phòng lại toàn bộ) hay **chỉ viết riêng** các endpoint cho Check-out & Thanh toán trước cho anh?
2. Em có nên insert một vài dữ liệu mẫu (mock data) vào hệ DB PostgreSQL của anh ở local để chúng ta test thử kết quả không?

## Verification Plan

- Chỉnh sửa/code logic cần thiết trong Node.js.
- Chạy hệ thống local (khởi động postgreSQL và ứng dụng Node.js `npm start`).
- Sử dụng Terminal để giả lập việc gọi API:
  - Cập nhật Check-out ảo qua endpoint mới.
  - Báo giá Invoice xuất ra JSON có đúng số tiền không.
  - Thực hiện thanh toán Payment giả lập.
