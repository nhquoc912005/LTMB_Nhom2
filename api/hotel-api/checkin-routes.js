const express = require("express");

const router = express.Router();

const BOOKING_STATUS = {
  WAITING_CHECKIN: ["Đã đặt cọc", "Chờ check-in", "da_dat", "da_xac_nhan"],
  CHECKED_IN: "Đang ở",
};

const ROOM_STATUS = {
  AVAILABLE: "Trống",
  OCCUPIED: "Bận",
};

class BusinessError extends Error {
  constructor(statusCode, code, message, details) {
    super(message);
    this.statusCode = statusCode;
    this.code = code;
    this.details = details;
  }
}

function asyncRoute(handler) {
  return async (req, res) => {
    try {
      await handler(req, res);
    } catch (error) {
      console.error(error);
      const statusCode = error.statusCode || 500;
      res.status(statusCode).json({
        success: false,
        code: error.code || "INTERNAL_ERROR",
        message: error.message || "Lỗi hệ thống",
        details: error.details,
      });
    }
  };
}

function normalizeRows(rows) {
  if (Array.isArray(rows)) return rows;
  if (!rows) return [];
  if (typeof rows === "string") {
    try {
      const parsed = JSON.parse(rows);
      return Array.isArray(parsed) ? parsed : [];
    } catch (_) {
      return [];
    }
  }
  return [];
}

function formatDate(dateStr) {
  if (!dateStr) return "";
  try {
    const d = new Date(dateStr);
    if (Number.isNaN(d.getTime())) return dateStr;
    const day = String(d.getDate()).padStart(2, "0");
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
  } catch (_) {
    return dateStr;
  }
}

function buildStayPeriod(checkIn, checkOut) {
  return [checkIn, checkOut].filter(Boolean).join(" - ");
}

function mapBookingRow(row) {
  const rooms = normalizeRows(row.rooms);
  const adults = Number(row.so_nguoi_lon || 0);
  const children = Number(row.so_tre_em || 0);
  let total = Number(row.tong_so_nguoi || 0);
  if (total === 0) total = adults + children;

  const roomNames = row.room_names || row.so_phong || "";
  const checkIn = formatDate(row.ngay_nhan);
  const checkOut = formatDate(row.ngay_tra);

  return {
    ma_dat_phong: row.ma_dat_phong,
    booking_id: row.ma_dat_phong,
    bookingId: row.ma_dat_phong,
    customer_name: row.customer_name,
    customerName: row.customer_name,
    ten_nguoi_dat: row.customer_name,
    customer_phone: row.customer_phone,
    customerPhone: row.customer_phone,
    sdt_nguoi_dat: row.customer_phone,
    email: row.email,
    ngay_nhan: checkIn,
    check_in: checkIn,
    checkIn,
    ngay_tra: checkOut,
    check_out: checkOut,
    checkOut,
    so_nguoi_lon: adults,
    adults,
    so_tre_em: children,
    children,
    tong_so_nguoi: total,
    total_guests: total,
    totalGuests: total,
    trang_thai: row.trang_thai,
    room_number: roomNames,
    room_names: roomNames,
    roomNumber: roomNames,
    rooms,
    stayPeriod: buildStayPeriod(checkIn, checkOut),
  };
}

module.exports = function (pool) {
  router.get("/check-in/bookings", asyncRoute(async (req, res) => {
    const values = [BOOKING_STATUS.WAITING_CHECKIN];
    const where = [
      "dp.trang_thai = ANY($1::text[])",
      `NOT EXISTS (
        SELECT 1
        FROM public.luu_tru lt
        WHERE lt.ma_dat_phong = dp.ma_dat_phong
          AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
          AND lt.thoi_gian_checkout_thuc_te IS NULL
      )`,
    ];

    if (req.query.from) {
      values.push(String(req.query.from).trim());
      where.push(`dp.ngay_nhan::date >= $${values.length}::date`);
    }

    if (req.query.to) {
      values.push(String(req.query.to).trim());
      where.push(`dp.ngay_tra::date <= $${values.length}::date`);
    }

    if (req.query.q) {
      values.push(`%${String(req.query.q).trim()}%`);
      where.push(`(
        dp.ma_dat_phong ILIKE $${values.length}
        OR COALESCE(kh.ho_ten, dp.ten_nguoi_dat, '') ILIKE $${values.length}
        OR COALESCE(kh.sdt, dp.sdt_nguoi_dat, '') ILIKE $${values.length}
        OR COALESCE(dp.email, '') ILIKE $${values.length}
        OR EXISTS (
          SELECT 1
          FROM public.chi_tiet_dat_phong ctdp_search
          JOIN public.phong p_search ON p_search.id_phong = ctdp_search.id_phong
          WHERE ctdp_search.ma_dat_phong = dp.ma_dat_phong
            AND p_search.ten_phong ILIKE $${values.length}
        )
      )`);
    }

    const result = await pool.query(
      `
      SELECT
        dp.ma_dat_phong,
        COALESCE(kh.ho_ten, dp.ten_nguoi_dat) AS customer_name,
        COALESCE(kh.sdt, dp.sdt_nguoi_dat) AS customer_phone,
        dp.email,
        dp.ngay_nhan,
        dp.ngay_tra,
        dp.so_nguoi_lon,
        dp.so_tre_em,
        dp.tong_so_nguoi,
        dp.trang_thai,
        COALESCE(
          (
            SELECT string_agg(room_name, ', ')
            FROM (
              SELECT DISTINCT p2.ten_phong AS room_name
              FROM public.chi_tiet_dat_phong ctdp2
              JOIN public.phong p2 ON p2.id_phong = ctdp2.id_phong
              WHERE ctdp2.ma_dat_phong = dp.ma_dat_phong
              ORDER BY room_name
            ) room_names
          ),
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
      LEFT JOIN public.khach_hang kh ON dp.id_kh = kh.id_kh
      LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.ma_dat_phong = dp.ma_dat_phong
      LEFT JOIN public.phong p ON p.id_phong = ctdp.id_phong
      WHERE ${where.join(" AND ")}
      GROUP BY dp.ma_dat_phong, kh.id_kh
      ORDER BY dp.ngay_nhan ASC, dp.ma_dat_phong ASC
      `,
      values
    );

    res.json({
      success: true,
      data: result.rows.map(mapBookingRow),
    });
  }));

  router.post("/check-in/bookings/:maDatPhong/confirm", asyncRoute(async (req, res) => {
    const { maDatPhong } = req.params;
    const { cccd } = req.body || {};

    if (!cccd || !/^(\d{9}|\d{12})$/.test(cccd)) {
      throw new BusinessError(400, "INVALID_CCCD", "Số CMND/CCCD phải gồm 9 hoặc 12 chữ số");
    }

    const client = await pool.connect();
    try {
      await client.query("BEGIN");

      const bookingResult = await client.query(
        "SELECT * FROM public.dat_phong WHERE ma_dat_phong = $1 FOR UPDATE",
        [maDatPhong]
      );
      const booking = bookingResult.rows[0];
      if (!booking) {
        throw new BusinessError(404, "NOT_FOUND", "Không tìm thấy đơn đặt phòng");
      }

      const roomsResult = await client.query(
        `
        SELECT p.id_phong, p.ten_phong
        FROM public.chi_tiet_dat_phong ctdp
        JOIN public.phong p ON ctdp.id_phong = p.id_phong
        WHERE ctdp.ma_dat_phong = $1
        FOR UPDATE OF p
        `,
        [maDatPhong]
      );
      const roomIds = roomsResult.rows.map((room) => room.id_phong);

      await client.query(
        `
        UPDATE public.luu_tru lt
        SET thoi_gian_checkout_thuc_te = CURRENT_TIMESTAMP
        FROM public.chi_tiet_dat_phong ctdp
        WHERE lt.ma_dat_phong = ctdp.ma_dat_phong
          AND ctdp.id_phong = ANY($1::int[])
          AND lt.thoi_gian_checkout_thuc_te IS NULL
        `,
        [roomIds]
      );

      let finalIdKh = booking.id_kh;
      const guestResult = await client.query(
        "SELECT id_kh FROM public.khach_hang WHERE cccd = $1 LIMIT 1",
        [cccd]
      );

      if (guestResult.rows.length > 0) {
        finalIdKh = guestResult.rows[0].id_kh;
      } else {
        const newGuestResult = await client.query(
          "INSERT INTO public.khach_hang (ho_ten, sdt, email, cccd) VALUES ($1, $2, $3, $4) RETURNING id_kh",
          [booking.ten_nguoi_dat, booking.sdt_nguoi_dat, booking.email, cccd]
        );
        finalIdKh = newGuestResult.rows[0].id_kh;
      }

      await client.query(
        "UPDATE public.dat_phong SET id_kh = $1, trang_thai = $2 WHERE ma_dat_phong = $3",
        [finalIdKh, BOOKING_STATUS.CHECKED_IN, maDatPhong]
      );

      await client.query(
        "INSERT INTO public.luu_tru (ma_dat_phong, thoi_gian_checkin_thuc_te, so_nguoi_thuc_te) VALUES ($1, CURRENT_TIMESTAMP, $2)",
        [maDatPhong, booking.tong_so_nguoi || ((booking.so_nguoi_lon || 0) + (booking.so_tre_em || 0))]
      );

      await client.query(
        "UPDATE public.phong SET trang_thai = $1 WHERE id_phong = ANY($2::int[])",
        [ROOM_STATUS.OCCUPIED, roomIds]
      );

      await client.query("COMMIT");
      res.json({ success: true, message: "Nhận phòng thành công" });
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  }));

  router.get("/check-in/bookings/:maDatPhong/available-rooms", asyncRoute(async (req, res) => {
    const result = await pool.query(
      "SELECT id_phong as id, ten_phong as room_number, loai_phong as room_type, gia_phong as price FROM public.phong WHERE trang_thai = $1",
      [ROOM_STATUS.AVAILABLE]
    );
    res.json({ success: true, data: result.rows });
  }));

  return router;
};
