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
