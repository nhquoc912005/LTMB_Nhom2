const express = require("express");
const cors = require('cors');
const app = express();
const pool = require("./db");
const createCheckInRouter = require("./checkin-routes");
const createCheckoutRouter = require("./checkout-routes");
const { createServiceRouter, ensureServiceTables } = require("./service-routes");
const createAuthRouter = require("./auth-routes");
const createUserRouter = require("./user-routes");

app.use(express.json());
app.use(cors());

// 1. Module Nhận phòng (Check-in)
app.use("/api", createCheckInRouter(pool));
// 2. Module Trả phòng (Check-out)
app.use("/api", createCheckoutRouter(pool));
// 3. Module Dịch vụ & Tài sản (Service & Assets)
app.use("/api", createServiceRouter(pool));
// 4. Module Xác thực (Auth)
app.use("/api/auth", createAuthRouter(pool));
// 5. Module Nhân viên (Users)
app.use("/api/users", createUserRouter(pool));

/* =====================================
   0. DASHBOARD STATS (Thống kê trang chủ)
===================================== */
app.get("/api/stats", async (req, res) => {
  try {
    const totalRooms = await pool.query("SELECT COUNT(*) FROM public.phong");
    const occupiedRooms = await pool.query("SELECT COUNT(*) FROM public.phong WHERE trang_thai = 'Bận'");
    const availableRooms = await pool.query("SELECT COUNT(*) FROM public.phong WHERE trang_thai = 'Trống' OR trang_thai IS NULL");
    const maintenanceRooms = await pool.query("SELECT COUNT(*) FROM public.phong WHERE trang_thai = 'Bảo trì'");

    res.json({
      success: true,
      data: {
        totalRooms: parseInt(totalRooms.rows[0].count),
        occupiedRooms: parseInt(occupiedRooms.rows[0].count),
        availableRooms: parseInt(availableRooms.rows[0].count),
        maintenanceRooms: parseInt(maintenanceRooms.rows[0].count)
      }
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

/* =====================================
   1. BOOKINGS (Danh sách đặt phòng)
===================================== */
// API: Lấy danh sách toàn bộ các bản ghi đặt phòng
app.get("/api/bookings", async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        dp.ma_dat_phong,
        COALESCE(dp.so_phong, STRING_AGG(p.ten_phong, ', ')) as room_number,
        dp.ten_nguoi_dat as customer_name,
        dp.email,
        dp.sdt_nguoi_dat as customer_phone,
        dp.tong_so_nguoi,
        dp.so_nguoi_lon,
        dp.so_tre_em,
        dp.ngay_nhan,
        dp.ngay_tra,
        dp.phuong_thuc_thanh_toan as payment_method,
        dp.tong_thanh_toan as total_amount,
        dp.trang_thai,
        dp.ghi_chu as note
      FROM public.dat_phong dp
      LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.ma_dat_phong = dp.ma_dat_phong
      LEFT JOIN public.phong p ON p.id_phong = ctdp.id_phong
      GROUP BY dp.ma_dat_phong
      ORDER BY dp.ngay_nhan DESC;
    `);
    res.json({
      success: true,
      data: result.rows,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

// API: Lấy các hoạt động gần đây nhất (Chờ check-in và Đã trả phòng)
app.get("/api/dashboard/activities", async (req, res) => {
  try {
    const result = await pool.query(`
      (
        -- Hoạt động đặt cọc (Hiển thị là Chờ check-in)
        SELECT 
          ma_dat_phong as booking_id,
          COALESCE(so_phong, (
            SELECT STRING_AGG(p.ten_phong, ', ')
            FROM public.chi_tiet_dat_phong ctdp
            JOIN public.phong p ON p.id_phong = ctdp.id_phong
            WHERE ctdp.ma_dat_phong = public.dat_phong.ma_dat_phong
          ), 'Chưa gán') as room_number,
          ten_nguoi_dat as customer_name,
          ngay_nhan as activity_time,
          'Chờ check-in' as status
        FROM public.dat_phong
        WHERE trang_thai = 'Đã đặt cọc'
      )
      UNION ALL
      (
        -- Hoạt động đã trả phòng (Check-out)
        SELECT 
          lt.ma_dat_phong as booking_id,
          COALESCE(dp.so_phong, (
            SELECT STRING_AGG(p.ten_phong, ', ')
            FROM public.chi_tiet_dat_phong ctdp
            JOIN public.phong p ON p.id_phong = ctdp.id_phong
            WHERE ctdp.ma_dat_phong = lt.ma_dat_phong
          ), 'N/A') as room_number,
          dp.ten_nguoi_dat as customer_name,
          lt.thoi_gian_checkout_thuc_te as activity_time,
          'Đã check-out' as status
        FROM public.luu_tru lt
        JOIN public.dat_phong dp ON lt.ma_dat_phong = dp.ma_dat_phong
        WHERE lt.thoi_gian_checkout_thuc_te IS NOT NULL
      )
      ORDER BY activity_time DESC
      LIMIT 5
    `);
    res.json({
      success: true,
      data: result.rows,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

ensureServiceTables(pool).catch((err) => {
  console.error("Không thể khởi tạo bảng dịch vụ/bồi thường:", err.message);
});

/* =====================================
   2. CREATE BOOKING (Tạo mới)
===================================== */
// API: Tạo một bản ghi đặt phòng mới
app.post("/api/bookings", async (req, res) => {
  try {
    const {
      room_number,
      customer_name,
      email,
      phone,
      total_guests,
      adults,
      children = 0,
      check_in,
      check_out,
      total_amount,
      note,
      payment_method
    } = req.body;

    const result = await pool.query(
      `INSERT INTO public.dat_phong
      (so_phong, ten_nguoi_dat, email, sdt_nguoi_dat, tong_so_nguoi, so_nguoi_lon, so_tre_em, ngay_nhan, ngay_tra, tong_thanh_toan, ghi_chu, phuong_thuc_thanh_toan)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
      RETURNING *`,
      [room_number, customer_name, email, phone, total_guests, adults, children, check_in, check_out, total_amount, note, payment_method]
    );

    res.status(201).json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   3. UPDATE STATUS (Cập nhật trạng thái)
===================================== */
// API: Cập nhật trạng thái của một bản ghi đặt phòng (ví dụ: Chờ check-in -> Đã check-in)
app.put("/api/bookings/:id/status", async (req, res) => {
  try {
    const { status } = req.body;
    const result = await pool.query(
      "UPDATE public.dat_phong SET trang_thai = $1 WHERE ma_dat_phong = $2 RETURNING *",
      [status, req.params.id]
    );
    if (result.rows.length === 0) return res.status(404).json({ success: false, error: "Booking not found" });
    res.json({ success: true, data: result.rows[0] });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

/* =====================================
   4. ROOMS (Lấy từ bảng phong)
===================================== */
// API: Lấy danh sách các số phòng hiện có trong hệ thống
// API: Lấy danh sách đầy đủ thông tin phòng
app.get("/api/rooms", async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        id_phong as id,
        ten_phong as room_number,
        loai_phong as room_type,
        suc_chua as capacity,
        gia_phong as price,
        trang_thai as status
      FROM public.phong 
      ORDER BY ten_phong ASC
    `);
    res.json({ success: true, data: result.rows });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// API: Cập nhật trạng thái phòng
app.put("/api/rooms/:id/status", async (req, res) => {
  try {
    const { status } = req.body;
    const result = await pool.query(
      "UPDATE public.phong SET trang_thai = $1 WHERE id_phong = $2 RETURNING *",
      [status, req.params.id]
    );
    res.json({ success: true, data: result.rows[0] });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running on port ${PORT}`);
});
