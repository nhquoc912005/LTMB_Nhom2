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

const BOOKING_STATUS = {
  RESERVED: "Đã đặt cọc",
  CANCELLED: "Đã hủy",
  CHECKED_IN: "Đang ở",
  CHECKED_OUT: "Đã trả phòng",
  CHECKED_OUT_ALT: "Đã check-out",
};

const ROOM_STATUS = {
  AVAILABLE: "Trống",
  RESERVED: "Đã đặt",
};

function normalizeRoomNumbers(value) {
  if (Array.isArray(value)) {
    return value.map((item) => String(item || "").trim()).filter(Boolean);
  }
  return String(value || "")
    .split(",")
    .map((item) => item.replace(/^Phòng\s+/i, "").trim())
    .filter(Boolean);
}

function generateBookingId() {
  const stamp = new Date().toISOString().replace(/\D/g, "").slice(0, 14);
  const random = Math.random().toString(36).slice(2, 6).toUpperCase();
  return `DP${stamp}${random}`;
}

function mapBookingRow(row) {
  if (!row) return null;
  return {
    ma_dat_phong: row.ma_dat_phong,
    booking_id: row.ma_dat_phong,
    room_number: row.room_number || row.room_names || row.so_phong,
    room_names: row.room_names || row.room_number || row.so_phong,
    ten_nguoi_dat: row.ten_nguoi_dat,
    customer_name: row.customer_name || row.ten_nguoi_dat,
    email: row.email,
    sdt_nguoi_dat: row.sdt_nguoi_dat,
    customer_phone: row.customer_phone || row.sdt_nguoi_dat,
    tong_so_nguoi: row.tong_so_nguoi,
    so_nguoi_lon: row.so_nguoi_lon,
    so_tre_em: row.so_tre_em,
    ngay_nhan: row.ngay_nhan,
    ngay_tra: row.ngay_tra,
    payment_method: row.payment_method || row.phuong_thuc_thanh_toan,
    total_amount: row.total_amount ?? row.tong_thanh_toan,
    tong_thanh_toan: row.tong_thanh_toan ?? row.total_amount,
    trang_thai: row.trang_thai,
    note: row.note || row.ghi_chu,
    rooms: Array.isArray(row.rooms) ? row.rooms : [],
  };
}

async function getBookingDetails(client, maDatPhong) {
  const result = await client.query(
    `
    SELECT
      dp.*,
      dp.ten_nguoi_dat AS customer_name,
      dp.sdt_nguoi_dat AS customer_phone,
      dp.phuong_thuc_thanh_toan AS payment_method,
      dp.tong_thanh_toan AS total_amount,
      dp.ghi_chu AS note,
      COALESCE(
        string_agg(p.ten_phong, ', ' ORDER BY p.ten_phong)
          FILTER (WHERE p.id_phong IS NOT NULL),
        dp.so_phong
      ) AS room_names,
      COALESCE(
        json_agg(
          json_build_object(
            'id', p.id_phong,
            'id_phong', p.id_phong,
            'id_ct_dat_phong', ctdp.id_ct_dat_phong,
            'room_number', p.ten_phong,
            'ten_phong', p.ten_phong,
            'room_type', p.loai_phong,
            'loai_phong', p.loai_phong,
            'capacity', p.suc_chua,
            'suc_chua', p.suc_chua,
            'price', p.gia_phong,
            'gia_phong', p.gia_phong,
            'status', p.trang_thai,
            'trang_thai', p.trang_thai
          )
          ORDER BY p.ten_phong
        ) FILTER (WHERE p.id_phong IS NOT NULL),
        '[]'::json
      ) AS rooms
    FROM public.dat_phong dp
    LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.ma_dat_phong = dp.ma_dat_phong
    LEFT JOIN public.phong p ON p.id_phong = ctdp.id_phong
    WHERE dp.ma_dat_phong = $1
    GROUP BY dp.ma_dat_phong
    `,
    [maDatPhong]
  );
  return mapBookingRow(result.rows[0]);
}

async function assertRoomsAvailableForBooking(client, roomIds, checkIn, checkOut, excludeBookingId) {
  const conflict = await client.query(
    `
    SELECT dp.ma_dat_phong, p.ten_phong
    FROM public.dat_phong dp
    JOIN public.chi_tiet_dat_phong ctdp ON ctdp.ma_dat_phong = dp.ma_dat_phong
    JOIN public.phong p ON p.id_phong = ctdp.id_phong
    WHERE ctdp.id_phong = ANY($1::int[])
      AND ($4::text IS NULL OR dp.ma_dat_phong <> $4)
      AND COALESCE(dp.trang_thai, '') NOT IN ($5, $6, $7)
      AND dp.ngay_nhan::date < $3::date
      AND dp.ngay_tra::date > $2::date
    LIMIT 1
    `,
    [roomIds, checkIn, checkOut, excludeBookingId || null, BOOKING_STATUS.CANCELLED, BOOKING_STATUS.CHECKED_OUT, BOOKING_STATUS.CHECKED_OUT_ALT]
  );
  if (conflict.rows.length > 0) {
    const roomName = conflict.rows[0].ten_phong;
    const bookingId = conflict.rows[0].ma_dat_phong;
    const err = new Error(`Phòng ${roomName} đã có booking ${bookingId} trong khoảng ngày này`);
    err.statusCode = 409;
    throw err;
  }
}

async function releaseBookingRooms(client, maDatPhong) {
  await client.query(
    `
    UPDATE public.phong p
    SET trang_thai = $1
    FROM public.chi_tiet_dat_phong ctdp
    WHERE ctdp.id_phong = p.id_phong
      AND ctdp.ma_dat_phong = $2
      AND NOT EXISTS (
        SELECT 1
        FROM public.luu_tru lt
        JOIN public.chi_tiet_dat_phong c2 ON c2.ma_dat_phong = lt.ma_dat_phong
        WHERE c2.id_phong = p.id_phong
          AND lt.thoi_gian_checkout_thuc_te IS NULL
      )
    `,
    [ROOM_STATUS.AVAILABLE, maDatPhong]
  );
}

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
app.get("/api/bookings/check-availability", async (req, res) => {
  const client = await pool.connect();
  try {
    const roomNumbers = normalizeRoomNumbers(req.query.room_number || req.query.room_numbers);
    const checkIn = req.query.check_in;
    const checkOut = req.query.check_out;
    if (!roomNumbers.length || !checkIn || !checkOut) {
      return res.status(400).json({ success: false, data: false, message: "Thiếu phòng hoặc ngày nhận/trả" });
    }

    const rooms = await client.query(
      "SELECT id_phong, ten_phong, trang_thai FROM public.phong WHERE ten_phong = ANY($1::text[])",
      [roomNumbers]
    );
    if (rooms.rows.length !== roomNumbers.length) {
      return res.json({ success: true, data: false, message: "Có phòng không tồn tại" });
    }

    const invalidRoom = rooms.rows.find((room) => room.trang_thai && ![ROOM_STATUS.AVAILABLE, ROOM_STATUS.RESERVED].includes(room.trang_thai));
    if (invalidRoom) {
      return res.json({ success: true, data: false, message: `Phòng ${invalidRoom.ten_phong} không sẵn sàng` });
    }

    await assertRoomsAvailableForBooking(client, rooms.rows.map((room) => room.id_phong), checkIn, checkOut, null);
    res.json({ success: true, data: true, message: "Phòng còn trống" });
  } catch (err) {
    if (err.statusCode === 409) {
      return res.json({ success: true, data: false, message: err.message });
    }
    console.error(err);
    res.status(500).json({ success: false, data: false, error: err.message });
  } finally {
    client.release();
  }
});

app.get("/api/bookings/:id", async (req, res) => {
  const client = await pool.connect();
  try {
    const booking = await getBookingDetails(client, req.params.id);
    if (!booking) {
      return res.status(404).json({ success: false, message: "Không tìm thấy booking" });
    }
    res.json({ success: true, data: booking });
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  } finally {
    client.release();
  }
});

app.post("/api/bookings", async (req, res) => {
  const client = await pool.connect();
  try {
    const {
      room_number,
      room_numbers,
      customer_name,
      ten_nguoi_dat,
      email,
      phone,
      sdt_nguoi_dat,
      total_guests,
      adults,
      children = 0,
      check_in,
      check_out,
      total_amount,
      note,
      payment_method,
    } = req.body;

    const roomsRequested = normalizeRoomNumbers(room_numbers || room_number || req.body.so_phong);
    if (!roomsRequested.length) {
      return res.status(400).json({ success: false, message: "Vui lòng chọn phòng" });
    }
    if (!check_in || !check_out) {
      return res.status(400).json({ success: false, message: "Thiếu ngày nhận/trả phòng" });
    }

    await client.query("BEGIN");
    const rooms = await client.query(
      "SELECT * FROM public.phong WHERE ten_phong = ANY($1::text[]) FOR UPDATE",
      [roomsRequested]
    );
    if (rooms.rows.length !== roomsRequested.length) {
      throw Object.assign(new Error("Có phòng không tồn tại trong hệ thống"), { statusCode: 400 });
    }

    const invalidRoom = rooms.rows.find((room) => room.trang_thai && ![ROOM_STATUS.AVAILABLE, ROOM_STATUS.RESERVED].includes(room.trang_thai));
    if (invalidRoom) {
      throw Object.assign(new Error(`Phòng ${invalidRoom.ten_phong} không sẵn sàng để đặt`), { statusCode: 409 });
    }

    const roomIds = rooms.rows.map((room) => room.id_phong);
    await assertRoomsAvailableForBooking(client, roomIds, check_in, check_out, null);

    const maDatPhong = generateBookingId();
    const roomSnapshot = rooms.rows.map((room) => room.ten_phong).join(", ");
    const guestName = customer_name || ten_nguoi_dat;
    const phoneNumber = phone || sdt_nguoi_dat;
    const totalGuests = total_guests ?? (Number(adults || 0) + Number(children || 0));
    const insert = await client.query(
      `INSERT INTO public.dat_phong
        (ma_dat_phong, so_phong, ten_nguoi_dat, email, sdt_nguoi_dat, tong_so_nguoi, so_nguoi_lon, so_tre_em, ngay_nhan, ngay_tra, tong_thanh_toan, ghi_chu, phuong_thuc_thanh_toan, trang_thai)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
       RETURNING *`,
      [
        maDatPhong,
        roomSnapshot,
        guestName,
        email,
        phoneNumber,
        totalGuests,
        adults,
        children,
        check_in,
        check_out,
        total_amount,
        note,
        payment_method,
        BOOKING_STATUS.RESERVED,
      ]
    );

    for (const room of rooms.rows) {
      await client.query(
        "INSERT INTO public.chi_tiet_dat_phong (id_phong, ma_dat_phong, so_luong_phong) VALUES ($1, $2, 1)",
        [room.id_phong, maDatPhong]
      );
    }
    await client.query("UPDATE public.phong SET trang_thai = $1 WHERE id_phong = ANY($2::int[])", [
      ROOM_STATUS.RESERVED,
      roomIds,
    ]);

    const booking = await getBookingDetails(client, insert.rows[0].ma_dat_phong);
    await client.query("COMMIT");
    return res.status(201).json({ success: true, data: booking, message: "Tạo đặt phòng thành công" });
  } catch (err) {
    await client.query("ROLLBACK").catch(() => {});
    console.error(err);
    return res.status(err.statusCode || 500).json({ success: false, message: err.message, error: err.message });
  } finally {
    client.release();
  }

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
app.put("/api/bookings/:id/cancel", async (req, res) => {
  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    const result = await client.query(
      "UPDATE public.dat_phong SET trang_thai = $1 WHERE ma_dat_phong = $2 RETURNING *",
      [BOOKING_STATUS.CANCELLED, req.params.id]
    );
    if (result.rows.length === 0) {
      await client.query("ROLLBACK");
      return res.status(404).json({ success: false, message: "Không tìm thấy booking" });
    }
    await releaseBookingRooms(client, req.params.id);
    const booking = await getBookingDetails(client, req.params.id);
    await client.query("COMMIT");
    res.json({ success: true, data: booking, message: "Đã hủy đặt phòng" });
  } catch (err) {
    await client.query("ROLLBACK").catch(() => {});
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  } finally {
    client.release();
  }
});

app.put("/api/bookings/:id/confirm", async (req, res) => {
  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    const result = await client.query(
      "UPDATE public.dat_phong SET trang_thai = $1 WHERE ma_dat_phong = $2 RETURNING *",
      [BOOKING_STATUS.RESERVED, req.params.id]
    );
    if (result.rows.length === 0) {
      await client.query("ROLLBACK");
      return res.status(404).json({ success: false, message: "Không tìm thấy booking" });
    }
    await client.query(
      `UPDATE public.phong p
       SET trang_thai = $1
       FROM public.chi_tiet_dat_phong ctdp
       WHERE ctdp.id_phong = p.id_phong
         AND ctdp.ma_dat_phong = $2`,
      [ROOM_STATUS.RESERVED, req.params.id]
    );
    const booking = await getBookingDetails(client, req.params.id);
    await client.query("COMMIT");
    res.json({ success: true, data: booking, message: "Đã xác nhận đặt phòng" });
  } catch (err) {
    await client.query("ROLLBACK").catch(() => {});
    console.error(err);
    res.status(500).json({ success: false, error: err.message });
  } finally {
    client.release();
  }
});

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
