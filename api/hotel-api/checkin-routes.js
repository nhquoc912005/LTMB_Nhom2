const express = require("express");

const router = express.Router();

const BOOKING_STATUS = {
  // Chấp nhận cả "Đã đặt cọc" và "Chờ check-in"
  WAITING_CHECKIN: parseStatusList(
    process.env.CHECKIN_READY_STATUSES,
    ["Đã đặt cọc", "Chờ check-in"]
  ),
  CHECKED_IN: process.env.BOOKING_STATUS_CHECKED_IN || "Đang ở",
};

const ROOM_STATUS = {
  AVAILABLE: "Trống",
  RESERVED: "Đã đặt",
  OCCUPIED: "Bận",
  MAINTENANCE: "Bảo trì",
};

const ROOM_READY_FOR_CHECKIN = parseStatusList(
  process.env.ROOM_READY_FOR_CHECKIN_STATUSES,
  [ROOM_STATUS.AVAILABLE, ROOM_STATUS.RESERVED]
);
const AVAILABLE_ROOM_STATUSES = parseStatusList(
  process.env.AVAILABLE_ROOM_STATUSES,
  [ROOM_STATUS.AVAILABLE]
);
const ROOM_STATUS_AFTER_CHANGE = process.env.ROOM_STATUS_AFTER_CHANGE || ROOM_STATUS.RESERVED;

function parseStatusList(raw, fallback) {
  if (!raw) return fallback;
  const statuses = raw.split(",").map((item) => item.trim()).filter(Boolean);
  return statuses.length > 0 ? statuses : fallback;
}

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
      sendError(res, error);
    }
  };
}

function sendError(res, error) {
  if (error instanceof BusinessError) {
    return res.status(error.statusCode).json({
      success: false,
      code: error.code,
      message: error.message,
      details: error.details,
    });
  }

  if (error && error.code === "23505") {
    return res.status(409).json({
      success: false,
      code: "DUPLICATE_CCCD",
      message: "Số CMND/CCCD đã tồn tại cho khách hàng khác",
    });
  }

  console.error(error);
  return res.status(500).json({
    success: false,
    code: "INTERNAL_ERROR",
    message: "Lỗi hệ thống, vui lòng thử lại sau",
  });
}

function parseRequiredId(value, fieldName) {
  const id = Number.parseInt(value, 10);
  if (!Number.isInteger(id) || id <= 0) {
    throw new BusinessError(400, "INVALID_ID", `${fieldName} không hợp lệ`);
  }
  return id;
}

function validateCccd(cccd) {
  const normalized = String(cccd || "").trim();
  if (!normalized) {
    throw new BusinessError(400, "CCCD_REQUIRED", "Vui lòng nhập số CMND/CCCD");
  }
  if (!/^(\d{9}|\d{12})$/.test(normalized)) {
    throw new BusinessError(400, "INVALID_CCCD", "Số CMND/CCCD phải gồm 9 hoặc 12 chữ số");
  }
  return normalized;
}

function assertStatusAllowed(status, allowedStatuses, errorCode, message) {
  if (!allowedStatuses.includes(status)) {
    throw new BusinessError(409, errorCode, message, { current_status: status, allowed_statuses: allowedStatuses });
  }
}

function totalGuests(booking) {
  return Number(booking.tong_so_nguoi ?? (Number(booking.so_nguoi_lon || 0) + Number(booking.so_tre_em || 0)));
}

function totalCapacity(roomDetails) {
  return roomDetails.reduce((sum, room) => {
    return sum + Number(room.suc_chua || 0) * Number(room.so_luong_phong || 1);
  }, 0);
}

function normalizeRooms(rooms) {
  if (Array.isArray(rooms)) return rooms;
  if (!rooms) return [];
  if (typeof rooms === "string") {
    try {
      const parsed = JSON.parse(rooms);
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
    if (isNaN(d.getTime())) return dateStr;
    const day = String(d.getDate()).padStart(2, "0");
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
  } catch (_) {
    return dateStr;
  }
}

function mapBookingRow(row) {
  const adults = Number(row.so_nguoi_lon || 0);
  const children = Number(row.so_tre_em || 0);
  let total = Number(row.tong_so_nguoi || 0);
  if (total === 0) total = adults + children;

  return {
    ma_dat_phong: row.ma_dat_phong,
    customer_name: row.customer_name,
    customer_phone: row.customer_phone,
    email: row.email,
    ngay_nhan: formatDate(row.ngay_nhan),
    ngay_tra: formatDate(row.ngay_tra),
    so_nguoi_lon: adults,
    so_tre_em: children,
    tong_so_nguoi: total,
    trang_thai: row.trang_thai,
    rooms: normalizeRooms(row.rooms),
    room_names: row.room_names || "",
  };
}

async function getBookingDetails(client, maDatPhong) {
  const result = await client.query(
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
        json_agg(
          json_build_object(
            'id_ct_dat_phong', ctdp.id_ct_dat_phong,
            'id_phong', p.id_phong,
            'ten_phong', p.ten_phong,
            'loai_phong', p.loai_phong,
            'suc_chua', p.suc_chua,
            'gia_phong', p.gia_phong,
            'trang_thai', p.trang_thai,
            'so_luong_phong', ctdp.so_luong_phong
          )
          ORDER BY p.ten_phong
        ) FILTER (WHERE p.id_phong IS NOT NULL),
        '[]'::json
      ) AS rooms
    FROM public.dat_phong dp
    LEFT JOIN public.khach_hang kh ON kh.id_kh = dp.id_kh
    LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.ma_dat_phong = dp.ma_dat_phong
    LEFT JOIN public.phong p ON p.id_phong = ctdp.id_phong
    WHERE dp.ma_dat_phong = $1
    GROUP BY dp.ma_dat_phong, kh.ho_ten, kh.sdt
    `,
    [maDatPhong]
  );

  return result.rows[0] ? mapBookingRow(result.rows[0]) : null;
}

async function getBookingForUpdate(client, maDatPhong) {
  const result = await client.query(
    "SELECT * FROM public.dat_phong WHERE ma_dat_phong = $1 FOR UPDATE",
    [maDatPhong]
  );
  return result.rows[0];
}

async function getCustomerForUpdate(client, idKh) {
  const result = await client.query(
    "SELECT * FROM public.khach_hang WHERE id_kh = $1 FOR UPDATE",
    [idKh]
  );
  return result.rows[0];
}

async function getRoomDetailsForUpdate(client, maDatPhong) {
  const result = await client.query(
    `
    SELECT
      ctdp.id_ct_dat_phong,
      ctdp.ma_dat_phong,
      ctdp.id_phong,
      ctdp.so_luong_phong,
      p.ten_phong,
      p.loai_phong,
      p.suc_chua,
      p.gia_phong,
      p.trang_thai
    FROM public.chi_tiet_dat_phong ctdp
    JOIN public.phong p ON p.id_phong = ctdp.id_phong
    WHERE ctdp.ma_dat_phong = $1
    ORDER BY p.ten_phong
    FOR UPDATE OF ctdp, p
    `,
    [maDatPhong]
  );
  return result.rows;
}

async function assertNotAlreadyCheckedIn(client, maDatPhong) {
  const result = await client.query(
    `
    SELECT id_luutru
    FROM public.luu_tru
    WHERE ma_dat_phong = $1
      AND thoi_gian_checkin_thuc_te IS NOT NULL
      AND thoi_gian_checkout_thuc_te IS NULL
    LIMIT 1
    FOR UPDATE
    `,
    [maDatPhong]
  );

  if (result.rows.length > 0) {
    throw new BusinessError(409, "ALREADY_CHECKED_IN", "Booking này đã được nhận phòng và khách đang lưu trú");
  }
}

/**
 * Đóng các bản ghi lưu trú "ma" (ghost stays) của các phòng được chọn.
 * Ghost stay = phòng có trạng thái 'Trống' nhưng vẫn còn luu_tru chưa checkout.
 * Điều này xảy ra khi dữ liệu bị sai lệch (ví dụ: checkout thủ công bên ngoài).
 */
async function closeGhostStays(client, roomIds) {
  const result = await client.query(
    `
    UPDATE public.luu_tru lt
    SET thoi_gian_checkout_thuc_te = CURRENT_TIMESTAMP
    FROM public.chi_tiet_dat_phong ctdp
    JOIN public.phong p ON p.id_phong = ctdp.id_phong
    WHERE ctdp.ma_dat_phong = lt.ma_dat_phong
      AND ctdp.id_phong = ANY($1::int[])
      AND lt.thoi_gian_checkout_thuc_te IS NULL
      AND p.trang_thai = 'Trống'
    RETURNING lt.id_luutru, lt.ma_dat_phong
    `,
    [roomIds]
  );
  if (result.rows.length > 0) {
    console.log(
      `[CheckIn] Đã đóng ${result.rows.length} ghost stay(s):`,
      result.rows.map((r) => `#${r.id_luutru} (${r.ma_dat_phong})`).join(", ")
    );
  }
  return result.rows;
}

/**
 * Tự động tìm hoặc tạo khách hàng dựa trên CCCD, sau đó gắn vào booking.
 *
 * Luồng xử lý:
 *  1. Nếu booking đã có id_kh → kiểm tra CCCD khớp, cập nhật nếu chưa có.
 *  2. Nếu booking chưa có id_kh:
 *     a. Tìm khách hàng theo CCCD trong DB.
 *     b. Nếu tìm thấy → dùng id_kh đó.
 *     c. Nếu không thấy → tạo mới khách hàng.
 *     d. Gắn id_kh vào bảng dat_phong.
 */
async function autoLinkOrCreateCustomer(client, booking, cccd) {
  // --- Trường hợp 1: Booking đã liên kết với khách hàng ---
  if (booking.id_kh) {
    const customer = await getCustomerForUpdate(client, booking.id_kh);
    if (!customer) {
      throw new BusinessError(404, "CUSTOMER_NOT_FOUND", "Không tìm thấy khách hàng của booking");
    }

    // Kiểm tra CCCD trùng với khách hàng khác
    const duplicate = await client.query(
      "SELECT id_kh FROM public.khach_hang WHERE cccd = $1 AND id_kh <> $2 LIMIT 1",
      [cccd, booking.id_kh]
    );
    if (duplicate.rows.length > 0) {
      throw new BusinessError(409, "DUPLICATE_CCCD", "Số CMND/CCCD đã tồn tại cho khách hàng khác");
    }

    // Cập nhật CCCD nếu khách chưa có
    if (!customer.cccd) {
      const updated = await client.query(
        "UPDATE public.khach_hang SET cccd = $1 WHERE id_kh = $2 RETURNING *",
        [cccd, booking.id_kh]
      );
      console.log(`[CheckIn] Đã cập nhật CCCD cho khách hàng #${booking.id_kh}`);
      return updated.rows[0];
    }

    // CCCD đã có nhưng không khớp
    if (customer.cccd !== cccd) {
      throw new BusinessError(
        409,
        "CCCD_MISMATCH",
        `Số CMND/CCCD không khớp với hồ sơ của khách hàng (kết thúc bằng ...${customer.cccd.slice(-3)})`
      );
    }

    return customer;
  }

  // --- Trường hợp 2: Booking chưa liên kết (id_kh = null) ---
  // 2a. Tìm khách hàng theo CCCD
  const existingBycccd = await client.query(
    "SELECT * FROM public.khach_hang WHERE cccd = $1 LIMIT 1 FOR UPDATE",
    [cccd]
  );

  let customer;
  if (existingBycccd.rows.length > 0) {
    // 2b. Tìm thấy — dùng khách hàng có sẵn
    customer = existingBycccd.rows[0];
    console.log(`[CheckIn] Tìm thấy khách hàng #${customer.id_kh} theo CCCD, tự động liên kết.`);
  } else {
    // 2c. Không tìm thấy — tạo mới khách hàng từ thông tin booking
    const newCustomer = await client.query(
      `INSERT INTO public.khach_hang (ho_ten, sdt, email, cccd)
       VALUES ($1, $2, $3, $4)
       RETURNING *`,
      [
        booking.ten_nguoi_dat || booking.customer_name || "Khách vãng lai",
        booking.sdt_nguoi_dat || booking.customer_phone || null,
        booking.email || null,
        cccd,
      ]
    );
    customer = newCustomer.rows[0];
    console.log(`[CheckIn] Tạo mới khách hàng #${customer.id_kh} với CCCD ${cccd}.`);
  }

  // 2d. Gắn id_kh vào booking
  await client.query(
    "UPDATE public.dat_phong SET id_kh = $1 WHERE ma_dat_phong = $2",
    [customer.id_kh, booking.ma_dat_phong]
  );
  console.log(`[CheckIn] Đã liên kết khách hàng #${customer.id_kh} vào booking ${booking.ma_dat_phong}.`);

  return customer;
}

async function refreshBookingRoomSnapshot(client, maDatPhong) {
  const result = await client.query(
    `
    UPDATE public.dat_phong dp
    SET so_phong = room_snapshot.room_names
    FROM (
      SELECT ctdp.ma_dat_phong, string_agg(p.ten_phong, ', ' ORDER BY p.ten_phong) AS room_names
      FROM public.chi_tiet_dat_phong ctdp
      JOIN public.phong p ON p.id_phong = ctdp.id_phong
      WHERE ctdp.ma_dat_phong = $1
      GROUP BY ctdp.ma_dat_phong
    ) room_snapshot
    WHERE dp.ma_dat_phong = room_snapshot.ma_dat_phong
    RETURNING dp.so_phong
    `,
    [maDatPhong]
  );
  return result.rows[0]?.so_phong || null;
}

async function getCurrentRoomDetailForUpdate(client, maDatPhong, idCtDatPhong, oldRoomId) {
  const values = [maDatPhong];
  const conditions = ["ctdp.ma_dat_phong = $1"];

  if (idCtDatPhong) {
    values.push(idCtDatPhong);
    conditions.push(`ctdp.id_ct_dat_phong = $${values.length}`);
  }

  if (oldRoomId) {
    values.push(oldRoomId);
    conditions.push(`ctdp.id_phong = $${values.length}`);
  }

  const result = await client.query(
    `
    SELECT
      ctdp.id_ct_dat_phong,
      ctdp.ma_dat_phong,
      ctdp.id_phong,
      ctdp.so_luong_phong,
      p.ten_phong,
      p.loai_phong,
      p.suc_chua,
      p.gia_phong,
      p.trang_thai
    FROM public.chi_tiet_dat_phong ctdp
    JOIN public.phong p ON p.id_phong = ctdp.id_phong
    WHERE ${conditions.join(" AND ")}
    ORDER BY ctdp.id_ct_dat_phong
    LIMIT 2
    FOR UPDATE OF ctdp, p
    `,
    values
  );

  if (result.rows.length === 0) {
    throw new BusinessError(404, "CURRENT_ROOM_NOT_FOUND", "Không tìm thấy phòng hiện tại của booking");
  }

  if (result.rows.length > 1 && !idCtDatPhong) {
    throw new BusinessError(
      400,
      "ROOM_DETAIL_REQUIRED",
      "Booking có nhiều phòng, vui lòng truyền id_ct_dat_phong hoặc old_room_id để xác định phòng cần đổi"
    );
  }

  return result.rows[0];
}

function createCheckInRouter(pool) {
  router.get("/check-in/bookings", asyncRoute(async (req, res) => {
    const values = [BOOKING_STATUS.WAITING_CHECKIN];
    const where = [
      "dp.trang_thai = ANY($1::text[])",
      // Chỉ loại trừ booking đang có khách lưu trú CHƯA checkout (active stay).
      // Nếu booking đã checkout trước đó (thoi_gian_checkout_thuc_te IS NOT NULL)
      // thì vẫn hiển thị trong danh sách chờ check-in (phòng hợp lệ).
      `NOT EXISTS (
        SELECT 1
        FROM public.luu_tru lt
        WHERE lt.ma_dat_phong = dp.ma_dat_phong
          AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
          AND lt.thoi_gian_checkout_thuc_te IS NULL
      )`,
    ];

    if (req.query.from) {
      values.push(req.query.from);
      where.push(`dp.ngay_nhan::date >= $${values.length}::date`);
    }
    if (req.query.to) {
      values.push(req.query.to);
      where.push(`dp.ngay_nhan::date <= $${values.length}::date`);
    }
    if (req.query.q) {
      values.push(`%${String(req.query.q).trim()}%`);
      where.push(`(
        COALESCE(kh.ho_ten, dp.ten_nguoi_dat, '') ILIKE $${values.length}
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

    const sql = `
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
        (
          SELECT string_agg(p_sub.ten_phong, ', ' ORDER BY p_sub.ten_phong)
          FROM public.chi_tiet_dat_phong ctdp_sub
          JOIN public.phong p_sub ON p_sub.id_phong = ctdp_sub.id_phong
          WHERE ctdp_sub.ma_dat_phong = dp.ma_dat_phong
        ) AS room_names,
        COALESCE(
          (
            SELECT json_agg(
              json_build_object(
                'id', p_sub.id_phong,
                'id_phong', p_sub.id_phong,
                'id_ct_dat_phong', ctdp_sub.id_ct_dat_phong,
                'room_number', p_sub.ten_phong,
                'ten_phong', p_sub.ten_phong,
                'room_type', p_sub.loai_phong,
                'loai_phong', p_sub.loai_phong,
                'capacity', p_sub.suc_chua,
                'suc_chua', p_sub.suc_chua,
                'price', p_sub.gia_phong,
                'gia_phong', p_sub.gia_phong,
                'status', p_sub.trang_thai,
                'trang_thai', p_sub.trang_thai
              )
              ORDER BY p_sub.ten_phong
            )
            FROM public.chi_tiet_dat_phong ctdp_sub
            JOIN public.phong p_sub ON p_sub.id_phong = ctdp_sub.id_phong
            WHERE ctdp_sub.ma_dat_phong = dp.ma_dat_phong
          ),
          '[]'::json
        ) AS rooms
      FROM public.dat_phong dp
      LEFT JOIN public.khach_hang kh ON kh.id_kh = dp.id_kh
      WHERE ${where.join(" AND ")}
      ORDER BY dp.ngay_nhan ASC, dp.ma_dat_phong ASC
    `;

    console.log("Check-in Query SQL:", sql);
    console.log("Check-in Query Params:", values);

    const result = await pool.query(sql, values);

    res.json({
      success: true,
      data: result.rows.map(mapBookingRow),
      meta: {
        booking_statuses: BOOKING_STATUS.WAITING_CHECKIN,
        room_source: "chi_tiet_dat_phong",
      },
    });
  }));

  const confirmCheckIn = asyncRoute(async (req, res) => {
    const maDatPhong = String(req.params.maDatPhong || "").trim();
    const body = req.body || {};
    const cccd = validateCccd(body.cccd || body.cmnd_cccd);
    const note = body.note ? String(body.note).trim() : null;

    const client = await pool.connect();
    try {
      await client.query("BEGIN");

      // ── Bước 1: Lấy & kiểm tra booking ──────────────────────────────────────
      const booking = await getBookingForUpdate(client, maDatPhong);
      if (!booking) {
        throw new BusinessError(404, "BOOKING_NOT_FOUND", "Không tìm thấy booking");
      }

      assertStatusAllowed(
        booking.trang_thai,
        BOOKING_STATUS.WAITING_CHECKIN,
        "BOOKING_NOT_READY_FOR_CHECKIN",
        `Booking đang ở trạng thái "${booking.trang_thai}", không thể nhận phòng. ` +
        `Trạng thái hợp lệ: ${BOOKING_STATUS.WAITING_CHECKIN.join(", ")}.`
      );

      await assertNotAlreadyCheckedIn(client, maDatPhong);

      // ── Bước 2: Kiểm tra phòng ───────────────────────────────────────────────
      const rooms = await getRoomDetailsForUpdate(client, maDatPhong);
      if (rooms.length === 0) {
        throw new BusinessError(409, "BOOKING_HAS_NO_ROOM", "Booking chưa có phòng trong chi_tiet_dat_phong");
      }

      const invalidRooms = rooms.filter((room) => !ROOM_READY_FOR_CHECKIN.includes(room.trang_thai));
      if (invalidRooms.length > 0) {
        throw new BusinessError(409, "ROOM_NOT_READY", "Có phòng không ở trạng thái sẵn sàng nhận khách", {
          rooms: invalidRooms.map((room) => ({
            id_phong: room.id_phong,
            ten_phong: room.ten_phong,
            trang_thai: room.trang_thai,
          })),
          allowed_statuses: ROOM_READY_FOR_CHECKIN,
        });
      }

      // ── Bước 3: Tự động đóng ghost stays (dữ liệu sai lệch) ─────────────────
      // Phòng trạng thái 'Trống' nhưng vẫn còn luu_tru chưa kết thúc → đóng lại.
      const roomIds = rooms.map((r) => r.id_phong);
      await closeGhostStays(client, roomIds);

      // ── Bước 4: Kiểm tra conflict lần cuối sau khi đóng ghost stays ──────────
      const conflictCheck = await client.query(
        `
        SELECT p.ten_phong
        FROM public.luu_tru lt
        JOIN public.chi_tiet_dat_phong ctdp ON ctdp.ma_dat_phong = lt.ma_dat_phong
        JOIN public.phong p ON p.id_phong = ctdp.id_phong
        WHERE ctdp.id_phong = ANY($1::int[])
          AND lt.thoi_gian_checkout_thuc_te IS NULL
        LIMIT 1
        `,
        [roomIds]
      );
      if (conflictCheck.rows.length > 0) {
        throw new BusinessError(
          409,
          "ROOM_STILL_OCCUPIED",
          `Phòng "${conflictCheck.rows[0].ten_phong}" đang có khách đang lưu trú thực sự, không thể nhận phòng.`
        );
      }

      // ── Bước 5: Kiểm tra sức chứa ───────────────────────────────────────────
      const guestCount = totalGuests(booking);
      const capacity = totalCapacity(rooms);
      if (capacity > 0 && guestCount > capacity) {
        throw new BusinessError(409, "CAPACITY_EXCEEDED", "Tổng số người vượt quá sức chứa phòng", {
          guest_count: guestCount,
          total_capacity: capacity,
        });
      }

      // ── Bước 6: Tự động tìm/tạo/liên kết khách hàng ─────────────────────────
      await autoLinkOrCreateCustomer(client, booking, cccd);

      // ── Bước 7: Tạo bản ghi lưu trú mới ─────────────────────────────────────
      const stay = await client.query(
        `
        INSERT INTO public.luu_tru
          (ma_dat_phong, thoi_gian_checkin_thuc_te, so_nguoi_thuc_te)
        VALUES ($1, CURRENT_TIMESTAMP, $2)
        RETURNING *
        `,
        [maDatPhong, guestCount]
      );

      // ── Bước 8: Cập nhật trạng thái booking → "Đang ở" ──────────────────────
      await client.query(
        "UPDATE public.dat_phong SET trang_thai = $1 WHERE ma_dat_phong = $2",
        [BOOKING_STATUS.CHECKED_IN, maDatPhong]
      );

      // ── Bước 9: Cập nhật trạng thái phòng → "Bận" ───────────────────────────
      await client.query(
        "UPDATE public.phong SET trang_thai = $1 WHERE id_phong = ANY($2::int[])",
        [ROOM_STATUS.OCCUPIED, roomIds]
      );

      const updatedBooking = await getBookingDetails(client, maDatPhong);
      await client.query("COMMIT");

      res.json({
        success: true,
        message: "Nhận phòng thành công",
        data: {
          booking: updatedBooking,
          stay: stay.rows[0],
          note_persisted: false,
          note_received: note,
        },
      });
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  });

  router.post("/check-in/bookings/:maDatPhong/confirm", confirmCheckIn);
  router.put("/bookings/:maDatPhong/check-in", confirmCheckIn);

  router.get("/check-in/bookings/:maDatPhong/available-rooms", asyncRoute(async (req, res) => {
    const maDatPhong = String(req.params.maDatPhong || "").trim();
    const idCtDatPhong = req.query.id_ct_dat_phong ? parseRequiredId(req.query.id_ct_dat_phong, "ID chi tiết đặt phòng") : null;
    const currentRoomId = req.query.current_room_id ? parseRequiredId(req.query.current_room_id, "ID phòng hiện tại") : null;
    const sameType = String(req.query.same_type || "").toLowerCase() === "true";
    const sameOrLargerCapacity = String(req.query.same_or_larger_capacity || "").toLowerCase() === "true";

    let currentRoom = null;
    if (idCtDatPhong || currentRoomId) {
      const result = await pool.query(
        `
        SELECT p.*
        FROM public.chi_tiet_dat_phong ctdp
        JOIN public.phong p ON p.id_phong = ctdp.id_phong
        WHERE ctdp.ma_dat_phong = $1
          AND ($2::int IS NULL OR ctdp.id_ct_dat_phong = $2)
          AND ($3::int IS NULL OR ctdp.id_phong = $3)
        LIMIT 1
        `,
        [maDatPhong, idCtDatPhong, currentRoomId]
      );
      currentRoom = result.rows[0] || null;
    }

    const values = [AVAILABLE_ROOM_STATUSES, maDatPhong];
    const where = [
      "p.trang_thai = ANY($1::text[])",
      `NOT EXISTS (
        SELECT 1
        FROM public.chi_tiet_dat_phong ctdp_current
        WHERE ctdp_current.ma_dat_phong = $2
          AND ctdp_current.id_phong = p.id_phong
      )`,
    ];

    if (sameType && currentRoom?.loai_phong) {
      values.push(currentRoom.loai_phong);
      where.push(`p.loai_phong = $${values.length}`);
    }

    if (sameOrLargerCapacity && currentRoom?.suc_chua) {
      values.push(Number(currentRoom.suc_chua));
      where.push(`p.suc_chua >= $${values.length}`);
    }

    const result = await pool.query(
      `
      SELECT
        p.id_phong AS id,
        p.ten_phong AS room_number,
        p.loai_phong AS room_type,
        p.suc_chua AS capacity,
        p.gia_phong AS price,
        p.trang_thai AS status
      FROM public.phong p
      WHERE ${where.join(" AND ")}
      ORDER BY p.ten_phong ASC
      `,
      values
    );

    res.json({
      success: true,
      data: result.rows,
      meta: {
        available_statuses: AVAILABLE_ROOM_STATUSES,
        excluded_booking_id: maDatPhong,
        same_type_filter_applied: Boolean(sameType && currentRoom?.loai_phong),
        capacity_filter_applied: Boolean(sameOrLargerCapacity && currentRoom?.suc_chua),
        sql: `SELECT p.id_phong, p.ten_phong, p.loai_phong, p.suc_chua, p.gia_phong, p.trang_thai FROM public.phong p WHERE ...`, // Simplified for meta
        params: values,
        count: result.rows.length
      },
    });
  }));

  const changeRoom = asyncRoute(async (req, res) => {
    const maDatPhong = String(req.params.maDatPhong || "").trim();
    const body = req.body || {};
    const idCtDatPhong = body.id_ct_dat_phong ? parseRequiredId(body.id_ct_dat_phong, "ID chi tiết đặt phòng") : null;
    const oldRoomId = body.old_room_id ? parseRequiredId(body.old_room_id, "ID phòng cũ") : null;
    const newRoomId = parseRequiredId(body.new_room_id, "ID phòng mới");
    const reason = body.reason ? String(body.reason).trim() : null;

    const client = await pool.connect();
    try {
      await client.query("BEGIN");

      const booking = await getBookingForUpdate(client, maDatPhong);
      if (!booking) {
        throw new BusinessError(404, "BOOKING_NOT_FOUND", "Không tìm thấy booking");
      }
      assertStatusAllowed(
        booking.trang_thai,
        BOOKING_STATUS.WAITING_CHECKIN,
        "BOOKING_NOT_READY_FOR_ROOM_CHANGE",
        "Booking không ở trạng thái cho phép đổi phòng trước check-in"
      );
      await assertNotAlreadyCheckedIn(client, maDatPhong);

      const currentRoom = await getCurrentRoomDetailForUpdate(client, maDatPhong, idCtDatPhong, oldRoomId);
      if (currentRoom.id_phong === newRoomId) {
        throw new BusinessError(409, "SAME_ROOM", "Không thể đổi sang chính phòng hiện tại");
      }

      const newRoomResult = await client.query(
        "SELECT * FROM public.phong WHERE id_phong = $1 FOR UPDATE",
        [newRoomId]
      );
      const newRoom = newRoomResult.rows[0];
      if (!newRoom) {
        throw new BusinessError(404, "NEW_ROOM_NOT_FOUND", "Không tìm thấy phòng mới");
      }
      if (!AVAILABLE_ROOM_STATUSES.includes(newRoom.trang_thai)) {
        throw new BusinessError(409, "NEW_ROOM_NOT_AVAILABLE", "Phòng mới không trống", {
          id_phong: newRoom.id_phong,
          ten_phong: newRoom.ten_phong,
          trang_thai: newRoom.trang_thai,
          available_statuses: AVAILABLE_ROOM_STATUSES,
        });
      }

      // KIỂM TRA BỔ SUNG: Đảm bảo phòng mới không có khách lưu trú "ma" chưa trả phòng
      const newRoomConflict = await client.query(
        `
        SELECT 1 FROM public.luu_tru lt
        JOIN public.chi_tiet_dat_phong ctdp ON ctdp.ma_dat_phong = lt.ma_dat_phong
        WHERE ctdp.id_phong = $1 AND lt.thoi_gian_checkout_thuc_te IS NULL
        LIMIT 1
        `,
        [newRoomId]
      );
      if (newRoomConflict.rows.length > 0) {
        throw new BusinessError(
          409,
          "NEW_ROOM_STILL_OCCUPIED",
          `Phòng ${newRoom.ten_phong} hiện đang có khách lưu trú chưa trả phòng, không thể đổi sang.`
        );
      }

      await client.query(
        "UPDATE public.chi_tiet_dat_phong SET id_phong = $1 WHERE id_ct_dat_phong = $2",
        [newRoomId, currentRoom.id_ct_dat_phong]
      );
      await client.query(
        "UPDATE public.phong SET trang_thai = $1 WHERE id_phong = $2",
        [ROOM_STATUS.AVAILABLE, currentRoom.id_phong]
      );
      await client.query(
        "UPDATE public.phong SET trang_thai = $1 WHERE id_phong = $2",
        [ROOM_STATUS_AFTER_CHANGE, newRoomId]
      );
      await refreshBookingRoomSnapshot(client, maDatPhong);

      const updatedBooking = await getBookingDetails(client, maDatPhong);
      await client.query("COMMIT");

      res.json({
        success: true,
        message: "Đổi phòng thành công",
        data: {
          booking: updatedBooking,
          changed_detail_id: currentRoom.id_ct_dat_phong,
          old_room: {
            id_phong: currentRoom.id_phong,
            ten_phong: currentRoom.ten_phong,
          },
          new_room: {
            id_phong: newRoom.id_phong,
            ten_phong: newRoom.ten_phong,
          },
          new_room_status: ROOM_STATUS_AFTER_CHANGE,
          reason_persisted: false,
          reason_received: reason,
        },
      });
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  });

  router.post("/check-in/bookings/:maDatPhong/change-room", changeRoom);
  router.put("/bookings/:maDatPhong/change-room", changeRoom);

  return router;
}

module.exports = createCheckInRouter;
