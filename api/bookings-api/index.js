const express = require("express");
const app = express();
const pool = require("./db");

app.use(express.json());

/* =====================================
   1. GET ALL BOOKINGS
===================================== */
app.get("/api/bookings", async (req, res) => {
  try {
    const { status, room_number } = req.query;

    let query = "SELECT * FROM bookings WHERE 1=1";
    let values = [];

    if (status) {
      values.push(status);
      query += ` AND status = $${values.length}`;
    }

    if (room_number) {
      values.push(room_number);
      query += ` AND room_number = $${values.length}`;
    }

    const result = await pool.query(query, values);

    res.json({
      success: true,
      data: result.rows,
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   2. GET BY ID
===================================== */
app.get("/api/bookings/:id", async (req, res) => {
  try {
    const result = await pool.query(
      "SELECT * FROM bookings WHERE booking_id = $1",
      [req.params.id],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Booking not found",
      });
    }

    res.json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   3. CHECK AVAILABILITY
===================================== */
app.get("/api/bookings/check-availability", async (req, res) => {
  const { room_number, check_in, check_out } = req.query;

  try {
    const result = await pool.query(
      `SELECT 1 FROM bookings
       WHERE room_number = $1
       AND daterange(check_in, check_out, '[)') &&
           daterange($2, $3, '[)')`,
      [room_number, check_in, check_out],
    );

    res.json({
      success: true,
      available: result.rows.length === 0,
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   4. CREATE BOOKING
===================================== */
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
    } = req.body;

    // validate required
    if (!room_number || !customer_name || !check_in || !check_out) {
      return res.status(400).json({
        success: false,
        error: "Missing required fields",
      });
    }

    // validate date
    if (new Date(check_out) <= new Date(check_in)) {
      return res.status(400).json({
        success: false,
        error: "Invalid dates",
      });
    }

    // validate guests
    if (adults + children !== total_guests) {
      return res.status(400).json({
        success: false,
        error: "Guest mismatch",
      });
    }

    // check availability
    const check = await pool.query(
      `SELECT 1 FROM bookings
       WHERE room_number = $1
       AND daterange(check_in, check_out, '[)') &&
           daterange($2, $3, '[)')`,
      [room_number, check_in, check_out],
    );

    if (check.rows.length > 0) {
      return res.status(400).json({
        success: false,
        error: "Room not available",
      });
    }

    // insert
    const result = await pool.query(
      `INSERT INTO bookings 
      (room_number, customer_name, email, phone, total_guests, adults, children, check_in, check_out)
      VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)
      RETURNING *`,
      [
        room_number,
        customer_name,
        email,
        phone,
        total_guests,
        adults,
        children,
        check_in,
        check_out,
      ],
    );

    res.status(201).json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   5. UPDATE BOOKING
===================================== */
app.put("/api/bookings/:id", async (req, res) => {
  try {
    const id = req.params.id;
    const { customer_name, phone } = req.body;

    const result = await pool.query(
      `UPDATE bookings
       SET customer_name = $1, phone = $2
       WHERE booking_id = $3
       RETURNING *`,
      [customer_name, phone, id],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Booking not found",
      });
    }

    res.json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   6. CANCEL BOOKING
===================================== */
app.put("/api/bookings/:id/cancel", async (req, res) => {
  try {
    const result = await pool.query(
      `UPDATE bookings
       SET status = 'da_huy'
       WHERE booking_id = $1
       RETURNING *`,
      [req.params.id],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Booking not found",
      });
    }

    res.json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   7. CONFIRM
===================================== */
app.put("/api/bookings/:id/confirm", async (req, res) => {
  try {
    const result = await pool.query(
      `UPDATE bookings
       SET status = 'da_xac_nhan'
       WHERE booking_id = $1
       RETURNING *`,
      [req.params.id],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Booking not found",
      });
    }

    res.json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   8. CHECK-IN
===================================== */
app.put("/api/bookings/:id/check-in", async (req, res) => {
  try {
    const result = await pool.query(
      `UPDATE bookings
       SET status = 'da_nhan_phong'
       WHERE booking_id = $1
       RETURNING *`,
      [req.params.id],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Booking not found",
      });
    }

    res.json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   9. CHECK-OUT
===================================== */
app.put("/api/bookings/:id/check-out", async (req, res) => {
  try {
    const result = await pool.query(
      `UPDATE bookings
       SET status = 'da_tra_phong'
       WHERE booking_id = $1
       RETURNING *`,
      [req.params.id],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Booking not found",
      });
    }

    res.json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   10. PAYMENT
===================================== */
app.put("/api/bookings/:id/payment", async (req, res) => {
  try {
    const result = await pool.query(
      `UPDATE bookings
       SET payment_status = 'da_thanh_toan'
       WHERE booking_id = $1
       RETURNING *`,
      [req.params.id],
    );

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        error: "Booking not found",
      });
    }

    res.json({
      success: true,
      data: result.rows[0],
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
});

/* =====================================
   SERVER
===================================== */
app.listen(3000, () => {
  console.log("Server running on port 3000");
});



/* =====================================
   API V2: THỰC HIỆN CHECK-OUT
===================================== */
app.put('/api/v2/bookings/:ma_dat_phong/check-out', async (req, res) => {
  const { ma_dat_phong } = req.params;
  const client = await pool.connect(); // Dùng client riêng để tạo transaction

  try {
    await client.query('BEGIN'); // Bắt đầu transaction

    // 1. Cập nhật thời gian check-out trong bảng luu_tru
    await client.query(`
      UPDATE luu_tru 
      SET thoi_gian_checkout_thuc_te = CURRENT_TIMESTAMP 
      WHERE ma_dat_phong = $1 AND thoi_gian_checkout_thuc_te IS NULL
    `, [ma_dat_phong]);

    // 2. Tìm tất cả các phòng mà khách đang thuê
    const chiTietResult = await client.query(`
      SELECT id_phong FROM chi_tiet_dat_phong WHERE ma_dat_phong = $1
    `, [ma_dat_phong]);

    // 3. Đặt trạng thái các phòng đó thành 'AVAILABLE' (hoặc 'CLEANING')
    for (let row of chiTietResult.rows) {
      await client.query(`
        UPDATE phong SET trang_thai = 'AVAILABLE' WHERE id_phong = $1
      `, [row.id_phong]);
    }

    // 4. Đổi trạng thái đặt phòng thành đã trả (CHECKED_OUT)
    await client.query(`
      UPDATE dat_phong SET trang_thai = 'CHECKED_OUT' WHERE ma_dat_phong = $1
    `, [ma_dat_phong]);

    await client.query('COMMIT'); // Lưu mọi thay đổi
    res.json({ success: true, message: "Check-out thành công!" });

  } catch (error) {
    await client.query('ROLLBACK'); // Quay xe lại nếu có bất kỳ lỗi gì
    res.status(500).json({ success: false, error: error.message });
  } finally {
    client.release(); // Trả kết nối về lại cho pool
  }
});
/* =====================================
   API V2: LẬP HÓA ĐƠN
===================================== */
app.post('/api/v2/bookings/:ma_dat_phong/invoice', async (req, res) => {
  const { ma_dat_phong } = req.params;
  try {
    // 1. Lấy thông tin lưu trú thực tế & giá phòng
    // Sử dụng JOIN để lấy được giá phòng của khách đó
    const luuTruQuery = await pool.query(`
      SELECT 
        lt.id_luutru, 
        EXTRACT(EPOCH FROM (lt.thoi_gian_checkout_thuc_te - lt.thoi_gian_checkin_thuc_te))/86400 AS so_ngay_o,
        dp.tien_coc,
        p.gia_phong
      FROM luu_tru lt
      JOIN dat_phong dp ON lt.ma_dat_phong = dp.ma_dat_phong
      JOIN chi_tiet_dat_phong ct ON ct.ma_dat_phong = dp.ma_dat_phong
      JOIN phong p ON p.id_phong = ct.id_phong
      WHERE lt.ma_dat_phong = $1
      LIMIT 1;
    `, [ma_dat_phong]);
    if (luuTruQuery.rows.length === 0) {
      return res.status(404).json({ success: false, error: "Không tìm thấy dữ liệu lưu trú" });
    }
    const info = luuTruQuery.rows[0];
    const id_luutru = info.id_luutru;
    const tien_coc = Number(info.tien_coc || 0);
    // Tính tiền phòng: Làm tròn số ngày lên (ví dụ 1.2 ngày tính thành 2 ngày)
    // Nếu ở dưới 1 ngày (trong ngày) thì tính 1 ngày.
    let soNgayTinhTien = Math.ceil(Number(info.so_ngay_o));
    if (soNgayTinhTien < 1) soNgayTinhTien = 1;
    const tienPhong = soNgayTinhTien * Number(info.gia_phong);
    // 2. Tính tổng tiền dịch vụ khách dùng
    const dichVuQuery = await pool.query(`
      SELECT COALESCE(SUM(thanh_tien), 0) as tong_dv 
      FROM su_dung_dich_vu WHERE id_luutru = $1
    `, [id_luutru]);
    const tienDichVu = Number(dichVuQuery.rows[0].tong_dv);
    // 3. Tính tiền bồi thường thiệt hại (nếu có)
    const thietHaiQuery = await pool.query(`
      SELECT COALESCE(SUM(so_tien_boi_thuong), 0) as tong_thiet_hai 
      FROM thiet_hai WHERE id_luutru = $1
    `, [id_luutru]);
    const tienThietHai = Number(thietHaiQuery.rows[0].tong_thiet_hai);
    // 4. Tổng kết tiền cuối cùng
    const tongTienHoaDon = (tienPhong + tienDichVu + tienThietHai) - tien_coc;
    // 5. Tạo vào bảng Hoa Đơn (mới mở bill chứ chưa thu tiền)
    const newInvoice = await pool.query(`
      INSERT INTO hoa_don (tong_tien, trang_thai) 
      VALUES ($1, 'UNPAID') RETURNING id_hoadon, tong_tien, ngay_lap
    `, [Math.max(tongTienHoaDon, 0)]); // Không cho bill rớt xuống âm
    res.json({
      success: true,
      detail: {
        tien_phong: tienPhong,
        tien_dich_vu: tienDichVu,
        tien_thiet_hai: tienThietHai,
        tien_coc_da_tru: tien_coc
      },
      invoice: newInvoice.rows[0]
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});
/* =====================================
   API V2: XÁC NHẬN THANH TOÁN
===================================== */
app.post('/api/v2/invoices/:id_hoadon/pay', async (req, res) => {
  const { id_hoadon } = req.params;
  const { phuong_thuc, so_tien } = req.body; // body { "phuong_thuc": "CREDIT_CARD", "so_tien": 500000 }

  const client = await pool.connect();

  try {
    await client.query('BEGIN');

    // 1. Kiểm tra hóa đơn tồn tại và chưa thanh toán
    const hd = await client.query(`SELECT tong_tien, trang_thai FROM hoa_don WHERE id_hoadon = $1`, [id_hoadon]);
    if (hd.rows.length === 0 || hd.rows[0].trang_thai === 'PAID') {
      throw new Error("Hóa đơn không hợp lệ hoặc đã thanh toán trước đó");
    }

    // 2. Insert vào bảng thanh_toan để lưu log kế toán
    await client.query(`
      INSERT INTO thanh_toan (so_tien, phuong_thuc, trang_thai, id_hoadon)
      VALUES ($1, $2, 'SUCCESS', $3)
    `, [so_tien, phuong_thuc, id_hoadon]);

    // 3. Đổi trạng thái hóa đơn
    await client.query(`
      UPDATE hoa_don SET trang_thai = 'PAID' WHERE id_hoadon = $1
    `, [id_hoadon]);

    // Tuỳ chọn: Nếu anh muốn link luôn việc cập nhật trạng thái "COMPLETED" cho dat_phong ở đây 
    // Mặc dù hơi vòng vèo (Invoice -> SuDungDv -> LuuTru -> DatPhong), nhưng anh có thể update sau.

    await client.query('COMMIT');
    res.json({ success: true, message: "Thanh toán thành công!" });

  } catch (error) {
    await client.query('ROLLBACK');
    res.status(500).json({ success: false, error: error.message });
  } finally {
    client.release();
  }
});

