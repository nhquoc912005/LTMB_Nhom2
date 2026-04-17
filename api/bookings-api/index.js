const express = require("express");
const cors = require('cors');
const app = express();
const pool = require("./db");

app.use(express.json());
app.use(cors());

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

    query += " ORDER BY created_at DESC";

    const result = await pool.query(query, values);

    res.json({
      success: true,
      data: result.rows,
    });
  } catch (err) {
    console.error("GET /api/bookings error:", err.message);
    res.status(500).json({ success: false, error: err.message });
  }
});

/* =====================================
   2. CREATE BOOKING
===================================== */
app.post("/api/bookings", async (req, res) => {
  try {
    const { room_number, customer_name, email, phone, total_guests, adults, children, check_in, check_out, total_amount, note } = req.body;

    const result = await pool.query(
      `INSERT INTO bookings
      (room_number, customer_name, email, phone, total_guests, adults, children, check_in, check_out, total_amount, note)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
      RETURNING *`,
      [room_number, customer_name, email, phone, total_guests, adults, children || 0, check_in, check_out, total_amount, note]
    );

    res.status(201).json({ success: true, data: result.rows[0] });
  } catch (err) {
    console.error("POST /api/bookings error:", err.message);
    res.status(500).json({ success: false, error: err.message });
  }
});

/* =====================================
   3. UPDATE STATUS
===================================== */
const updateStatus = async (req, res, newStatus) => {
  try {
    const result = await pool.query(
      `UPDATE bookings SET status = $1, updated_at = CURRENT_TIMESTAMP WHERE booking_id = $2 RETURNING *`,
      [newStatus, req.params.id]
    );
    if (result.rows.length === 0) return res.status(404).json({ success: false, error: "Not found" });
    res.json({ success: true, data: result.rows[0] });
  } catch (err) {
    console.error(`Update status to ${newStatus} error:`, err.message);
    res.status(500).json({ success: false, error: err.message });
  }
};

app.put("/api/bookings/:id/confirm", (req, res) => updateStatus(req, res, 'da_xac_nhan'));
app.put("/api/bookings/:id/check-in", (req, res) => updateStatus(req, res, 'da_nhan_phong'));
app.put("/api/bookings/:id/cancel", (req, res) => updateStatus(req, res, 'da_huy'));
app.put("/api/bookings/:id/check-out", (req, res) => updateStatus(req, res, 'da_tra_phong'));

const PORT = process.env.PORT || 3000;
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running on http://0.0.0.0:${PORT}`);
});
