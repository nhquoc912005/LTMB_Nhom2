const express = require("express");
const cors = require('cors');
const app = express();
const pool = require("./db");
const createCheckInRouter = require("./checkin-routes");
const createCheckoutRouter = require("./checkout-routes");
const { createServiceRouter, ensureServiceTables } = require("./service-routes");

app.use(express.json());
app.use(cors());
app.use("/api", createCheckInRouter(pool));
app.use("/api", createCheckoutRouter(pool));
app.use("/api", createServiceRouter(pool));

ensureServiceTables(pool).catch((err) => {
  console.error("Không thể khởi tạo bảng dịch vụ/bồi thường:", err.message);
});

/* =====================================
   1. BOOKINGS (Danh sách đặt phòng)
===================================== */
app.get("/api/bookings", async (req, res) => {
  try {
    const result = await pool.query("SELECT * FROM public.bookings ORDER BY created_at DESC");
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

/* =====================================
   2. CREATE BOOKING (Tạo mới)
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
      total_amount,
      note,
      payment_method
    } = req.body;

    const result = await pool.query(
      `INSERT INTO public.bookings
      (room_number, customer_name, email, phone, total_guests, adults, children, check_in, check_out, total_amount, note, payment_method)
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
app.put("/api/bookings/:id/status", async (req, res) => {
  try {
    const { status } = req.body;
    const result = await pool.query(
      "UPDATE public.bookings SET status = $1, updated_at = CURRENT_TIMESTAMP WHERE booking_id = $2 RETURNING *",
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
   4. ROOMS (Lấy từ bảng bookings)
===================================== */
app.get("/api/rooms", async (req, res) => {
  try {
    // Lấy danh sách các số phòng duy nhất từ bảng bookings
    const result = await pool.query("SELECT DISTINCT room_number FROM public.bookings ORDER BY room_number ASC");
    res.json({ success: true, data: result.rows });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running on port ${PORT}`);
});
