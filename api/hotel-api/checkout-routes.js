const express = require("express");

const BOOKING_STATUS = {
  CHECKED_IN: parseStatusList(process.env.CHECKOUT_ACTIVE_BOOKING_STATUSES, ["Đang ở", "Đã check-in"]),
  COMPLETED: process.env.BOOKING_STATUS_COMPLETED || "Đã thanh toán",
};

const ROOM_STATUS = {
  OCCUPIED: parseStatusList(process.env.CHECKOUT_OCCUPIED_ROOM_STATUSES, ["Bận"]),
  AFTER_CHECKOUT: "Trống",
};

const INVOICE_STATUS = {
  UNPAID: process.env.INVOICE_STATUS_UNPAID || "UNPAID",
  PAID: process.env.INVOICE_STATUS_PAID || "PAID",
};

const UNPAID_STATUSES = parseStatusList(process.env.INVOICE_UNPAID_STATUSES, [
  INVOICE_STATUS.UNPAID,
  "Chưa thanh toán",
]);

const PAYMENT_METHODS = {
  CASH: process.env.PAYMENT_METHOD_CASH || "CASH",
  TRANSFER: process.env.PAYMENT_METHOD_TRANSFER || "TRANSFER",
};

const ROOM_FEE_STRATEGY = process.env.CHECKOUT_ROOM_FEE_STRATEGY || "BOOKING_TOTAL_OR_ACTUAL_NIGHTS";
const ONE_DAY_MS = 24 * 60 * 60 * 1000;
const columnCache = new Map();

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

function money(value) {
  const parsed = Number(value || 0);
  if (!Number.isFinite(parsed)) return 0;
  return Math.round(parsed);
}

function parseMoney(value, fieldName) {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed < 0) {
    throw new BusinessError(400, "INVALID_AMOUNT", `${fieldName} không hợp lệ`);
  }
  return Math.round(parsed);
}

function normalizePaymentMethod(value) {
  const raw = String(value || "").trim();
  const upper = raw.toUpperCase();
  if (upper === "CASH" || raw === "Tiền mặt" || raw === "Tien mat") return PAYMENT_METHODS.CASH;
  if (upper === "TRANSFER" || raw === "Chuyển khoản" || raw === "Chuyen khoan") return PAYMENT_METHODS.TRANSFER;
  throw new BusinessError(400, "INVALID_PAYMENT_METHOD", "Phương thức thanh toán chỉ hỗ trợ tiền mặt hoặc chuyển khoản");
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

function calculateStayNights(checkinAt, checkoutAt) {
  const checkin = checkinAt instanceof Date ? checkinAt : new Date(checkinAt);
  const checkout = checkoutAt instanceof Date ? checkoutAt : new Date(checkoutAt);
  
  // If checkinAt was DD/MM/YYYY string, we need to parse it manually because JS Date is inconsistent with it
  if (typeof checkinAt === 'string' && checkinAt.includes('/')) {
    const [d, m, y] = checkinAt.split('/').map(Number);
    checkin.setFullYear(y, m - 1, d);
  }

  if (Number.isNaN(checkin.getTime()) || Number.isNaN(checkout.getTime())) {
    return 1;
  }
  
  if (checkout <= checkin) return 1;

  const diffMs = checkout.getTime() - checkin.getTime();
  return Math.max(1, Math.ceil(diffMs / ONE_DAY_MS));
}

function calculateActualRoomFee(rooms, nights) {
  return rooms.reduce((sum, room) => {
    return sum + money(room.gia_phong) * Number(room.so_luong_phong || 1) * nights;
  }, 0);
}

function calculateRoomFee(stay, rooms, nights) {
  const bookingTotal = money(stay.tong_thanh_toan);
  const actualTotal = calculateActualRoomFee(rooms, nights);

  if (ROOM_FEE_STRATEGY === "BOOKING_TOTAL") return bookingTotal;
  if (ROOM_FEE_STRATEGY === "ACTUAL_NIGHTS") return actualTotal;
  return bookingTotal > 0 ? bookingTotal : actualTotal;
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

function mapStayRow(row) {
  const rooms = normalizeRows(row.rooms);
  return {
    id_luutru: row.id_luutru,
    ma_dat_phong: row.ma_dat_phong,
    customer_name: row.customer_name,
    customer_phone: row.customer_phone,
    email: row.email,
    checkin_at: formatDate(row.thoi_gian_checkin_thuc_te),
    checkout_at: formatDate(row.thoi_gian_checkout_thuc_te),
    expected_checkout_at: formatDate(row.ngay_tra),
    adults: Number(row.so_nguoi_lon || 0),
    children: Number(row.so_tre_em || 0),
    total_guests: Number(row.so_nguoi_thuc_te || row.tong_so_nguoi || 0),
    booking_status: row.booking_status,
    tien_coc: money(row.tien_coc),
    tong_thanh_toan: money(row.tong_thanh_toan),
    rooms,
    room_names: rooms.map((room) => room && room.ten_phong).filter(Boolean).join(", "),
    raw_checkin: row.thoi_gian_checkin_thuc_te,
    raw_checkout: row.thoi_gian_checkout_thuc_te,
    raw_expected_checkout: row.ngay_tra,
  };
}

async function hasColumn(client, tableName, columnName) {
  const key = `${tableName}.${columnName}`;
  if (columnCache.has(key)) return columnCache.get(key);

  const result = await client.query(
    `
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = $1
      AND column_name = $2
    LIMIT 1
    `,
    [tableName, columnName]
  );
  const exists = result.rows.length > 0;
  columnCache.set(key, exists);
  return exists;
}

async function getStayForUpdateByBooking(client, maDatPhong) {
  const result = await client.query(
    `
    SELECT
      lt.id_luutru,
      lt.ma_dat_phong,
      lt.thoi_gian_checkin_thuc_te,
      lt.thoi_gian_checkout_thuc_te,
      lt.so_nguoi_thuc_te,
      dp.ngay_tra,
      dp.so_nguoi_lon,
      dp.so_tre_em,
      dp.tong_so_nguoi,
      dp.trang_thai AS booking_status,
      dp.ten_nguoi_dat,
      dp.email,
      dp.sdt_nguoi_dat,
      dp.tien_coc,
      dp.tong_thanh_toan,
      COALESCE(kh.ho_ten, dp.ten_nguoi_dat) AS customer_name,
      COALESCE(kh.sdt, dp.sdt_nguoi_dat) AS customer_phone
    FROM public.luu_tru lt
    JOIN public.dat_phong dp ON dp.ma_dat_phong = lt.ma_dat_phong
    LEFT JOIN public.khach_hang kh ON kh.id_kh = dp.id_kh
    WHERE lt.ma_dat_phong = $1
      AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
    ORDER BY lt.id_luutru DESC
    LIMIT 1
    FOR UPDATE OF lt, dp
    `,
    [maDatPhong]
  );
  return result.rows[0] || null;
}

async function getStayForUpdateById(client, idLuutru) {
  const result = await client.query(
    `
    SELECT
      lt.id_luutru,
      lt.ma_dat_phong,
      lt.thoi_gian_checkin_thuc_te,
      lt.thoi_gian_checkout_thuc_te,
      lt.so_nguoi_thuc_te,
      dp.ngay_tra,
      dp.so_nguoi_lon,
      dp.so_tre_em,
      dp.tong_so_nguoi,
      dp.trang_thai AS booking_status,
      dp.ten_nguoi_dat,
      dp.email,
      dp.sdt_nguoi_dat,
      dp.tien_coc,
      dp.tong_thanh_toan,
      COALESCE(kh.ho_ten, dp.ten_nguoi_dat) AS customer_name,
      COALESCE(kh.sdt, dp.sdt_nguoi_dat) AS customer_phone
    FROM public.luu_tru lt
    JOIN public.dat_phong dp ON dp.ma_dat_phong = lt.ma_dat_phong
    LEFT JOIN public.khach_hang kh ON kh.id_kh = dp.id_kh
    WHERE lt.id_luutru = $1
    FOR UPDATE OF lt, dp
    `,
    [idLuutru]
  );
  return result.rows[0] || null;
}

async function getRoomsForBooking(client, maDatPhong, shouldLock) {
  const result = await client.query(
    `
    SELECT
      ctdp.id_ct_dat_phong,
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
    ${shouldLock ? "FOR UPDATE OF ctdp, p" : ""}
    `,
    [maDatPhong]
  );
  return result.rows;
}

async function getServiceLines(client, idLuutru) {
  const result = await client.query(
    `
    SELECT
      sddv.id_sudung_dv,
      sddv.soluong,
      sddv.thoi_gian,
      sddv.thanh_tien,
      sddv.id_hoadon,
      dv.id_dichvu,
      dv.ten_dich_vu,
      dv.don_gia,
      COALESCE(sddv.thanh_tien, sddv.soluong * dv.don_gia, 0) AS line_total
    FROM public.su_dung_dich_vu sddv
    LEFT JOIN public.dich_vu dv ON dv.id_dichvu = sddv.id_dichvu
    WHERE sddv.id_luutru = $1
    ORDER BY sddv.thoi_gian ASC, sddv.id_sudung_dv ASC
    `,
    [idLuutru]
  );
  return result.rows;
}

async function getDamageLines(client, idLuutru) {
  const result = await client.query(
    `
    SELECT
      th.id_thie_thai,
      th.muc_do,
      th.so_tien_boi_thuong,
      th.trang_thai,
      ts.id_taisan,
      ts.ten_tai_san
    FROM public.thiet_hai th
    LEFT JOIN public.tai_san ts ON ts.id_taisan = th.id_taisan
    WHERE th.id_luutru = $1
      AND (th.trang_thai IS NULL OR th.trang_thai NOT IN ('CANCELLED', 'Đã hủy', 'HUY'))
    ORDER BY th.id_thie_thai ASC
    `,
    [idLuutru]
  );
  return result.rows;
}

async function buildDraftBill(client, stayRow, rooms, invoice) {
  const stay = mapStayRow({ ...stayRow, rooms });
  const checkoutPreviewAt = new Date();
  const nights = calculateStayNights(stay.raw_checkin, checkoutPreviewAt);
  const serviceLines = await getServiceLines(client, stay.id_luutru);
  const damageLines = await getDamageLines(client, stay.id_luutru);

  const roomFee = calculateRoomFee(stay, rooms, nights);
  const serviceFee = serviceLines.reduce((sum, item) => sum + money(item.line_total), 0);
  const damageFee = damageLines.reduce((sum, item) => sum + money(item.so_tien_boi_thuong), 0);
  const deposit = money(stay.tien_coc);
  const grossTotal = roomFee + serviceFee + damageFee;
  const amountDue = Math.max(0, grossTotal - deposit);
  const refundAmount = Math.max(0, deposit - grossTotal);

  return {
    id_hoadon: invoice?.id_hoadon || null,
    invoice_status: invoice?.trang_thai || null,
    id_luutru: stay.id_luutru,
    ma_dat_phong: stay.ma_dat_phong,
    customer_name: stay.customer_name,
    customer_phone: stay.customer_phone,
    email: stay.email,
    rooms: stay.rooms,
    room_names: stay.room_names,
    checkin_at: stay.checkin_at,
    checkout_preview_at: checkoutPreviewAt.toISOString(),
    expected_checkout_at: stay.expected_checkout_at,
    adults: stay.adults,
    children: stay.children,
    total_guests: stay.total_guests,
    room_fee_strategy: ROOM_FEE_STRATEGY,
    chargeable_nights: nights,
    room_fee: roomFee,
    service_fee: serviceFee,
    damage_fee: damageFee,
    extra_fee: serviceFee + damageFee,
    deposit,
    gross_total: grossTotal,
    amount_due: amountDue,
    refund_amount: refundAmount,
    service_lines: serviceLines.map((item) => ({
      id_sudung_dv: item.id_sudung_dv,
      id_dichvu: item.id_dichvu,
      ten_dich_vu: item.ten_dich_vu,
      soluong: Number(item.soluong || 0),
      don_gia: money(item.don_gia),
      thanh_tien: money(item.line_total),
      thoi_gian: item.thoi_gian,
    })),
    damage_lines: damageLines.map((item) => ({
      id_thie_thai: item.id_thie_thai,
      id_taisan: item.id_taisan,
      ten_tai_san: item.ten_tai_san,
      muc_do: item.muc_do,
      trang_thai: item.trang_thai,
      so_tien_boi_thuong: money(item.so_tien_boi_thuong),
    })),
    payment_url: invoice?.id_hoadon ? `/api/v2/invoices/${invoice.id_hoadon}/pay` : null,
  };
}

async function findDraftInvoiceForStay(client, idLuutru) {
  const hasStayColumn = await hasColumn(client, "hoa_don", "id_luutru");
  if (hasStayColumn) {
    const result = await client.query(
      `
      SELECT *
      FROM public.hoa_don
      WHERE id_luutru = $1
        AND (trang_thai IS NULL OR trang_thai = ANY($2::text[]))
      ORDER BY id_hoadon DESC
      LIMIT 1
      FOR UPDATE
      `,
      [idLuutru, UNPAID_STATUSES]
    );
    if (result.rows[0]) return result.rows[0];
  }

  const result = await client.query(
    `
    SELECT hd.*
    FROM public.hoa_don hd
    JOIN public.su_dung_dich_vu sddv ON sddv.id_hoadon = hd.id_hoadon
    WHERE sddv.id_luutru = $1
      AND (hd.trang_thai IS NULL OR hd.trang_thai = ANY($2::text[]))
    ORDER BY hd.id_hoadon DESC
    LIMIT 1
    FOR UPDATE OF hd
    `,
    [idLuutru, UNPAID_STATUSES]
  );
  return result.rows[0] || null;
}

async function createOrUpdateDraftInvoice(client, stay, draft) {
  let invoice = await findDraftInvoiceForStay(client, stay.id_luutru);
  const hasStayColumn = await hasColumn(client, "hoa_don", "id_luutru");

  if (!invoice) {
    if (hasStayColumn) {
      const created = await client.query(
        `
        INSERT INTO public.hoa_don (tong_tien, trang_thai, id_luutru)
        VALUES ($1, $2, $3)
        RETURNING *
        `,
        [draft.amount_due, INVOICE_STATUS.UNPAID, stay.id_luutru]
      );
      invoice = created.rows[0];
    } else {
      const created = await client.query(
        `
        INSERT INTO public.hoa_don (tong_tien, trang_thai)
        VALUES ($1, $2)
        RETURNING *
        `,
        [draft.amount_due, INVOICE_STATUS.UNPAID]
      );
      invoice = created.rows[0];
    }
  } else {
    const updated = await client.query(
      `
      UPDATE public.hoa_don
      SET tong_tien = $1, trang_thai = $2
      WHERE id_hoadon = $3
      RETURNING *
      `,
      [draft.amount_due, INVOICE_STATUS.UNPAID, invoice.id_hoadon]
    );
    invoice = updated.rows[0];
  }

  await client.query(
    `
    UPDATE public.su_dung_dich_vu
    SET id_hoadon = $1
    WHERE id_luutru = $2
      AND (id_hoadon IS NULL OR id_hoadon = $1)
    `,
    [invoice.id_hoadon, stay.id_luutru]
  );

  return invoice;
}

async function getInvoiceForUpdate(client, idHoaDon) {
  const result = await client.query(
    "SELECT * FROM public.hoa_don WHERE id_hoadon = $1 FOR UPDATE",
    [idHoaDon]
  );
  return result.rows[0] || null;
}

function assertInvoiceCanBePaid(invoice) {
  if (!invoice.trang_thai || UNPAID_STATUSES.includes(invoice.trang_thai)) return;
  throw new BusinessError(409, "INVOICE_NOT_UNPAID", "Hóa đơn không ở trạng thái chờ thanh toán", {
    current_status: invoice.trang_thai,
    allowed_statuses: UNPAID_STATUSES,
  });
}

async function resolveStayForInvoice(client, invoice, body) {
  const hasStayColumn = await hasColumn(client, "hoa_don", "id_luutru");
  if (hasStayColumn && invoice.id_luutru) {
    return getStayForUpdateById(client, invoice.id_luutru);
  }

  const usageResult = await client.query(
    `
    SELECT DISTINCT id_luutru
    FROM public.su_dung_dich_vu
    WHERE id_hoadon = $1
      AND id_luutru IS NOT NULL
    `,
    [invoice.id_hoadon]
  );
  if (usageResult.rows.length === 1) {
    return getStayForUpdateById(client, usageResult.rows[0].id_luutru);
  }

  if (body.id_luutru) {
    return getStayForUpdateById(client, parseRequiredId(body.id_luutru, "ID lưu trú"));
  }
  if (body.ma_dat_phong) {
    return getStayForUpdateByBooking(client, String(body.ma_dat_phong).trim());
    return getStayForUpdateByBooking(client, parseRequiredId(body.ma_dat_phong, "Mã đặt phòng"));
  }

  throw new BusinessError(
    400,
    "CHECKOUT_REFERENCE_REQUIRED",
    "Schema hoa_don chưa liên kết trực tiếp với luu_tru. Vui lòng truyền id_luutru/ma_dat_phong hoặc thêm cột hoa_don.id_luutru",
    { recommended_migration: "ALTER TABLE public.hoa_don ADD COLUMN id_luutru INTEGER REFERENCES public.luu_tru(id_luutru);" }
  );
}

function assertStayCanCheckout(stay) {
  if (!stay) {
    throw new BusinessError(404, "STAY_NOT_FOUND", "Không tìm thấy thông tin lưu trú");
  }
  if (!stay.thoi_gian_checkin_thuc_te) {
    throw new BusinessError(409, "NOT_CHECKED_IN", "Lưu trú chưa có thời gian nhận phòng thực tế");
  }
  if (stay.thoi_gian_checkout_thuc_te) {
    throw new BusinessError(409, "ALREADY_CHECKED_OUT", "Đặt phòng này đã trả phòng trước đó");
  }
  if (!BOOKING_STATUS.CHECKED_IN.includes(stay.booking_status)) {
    throw new BusinessError(409, "BOOKING_NOT_CHECKED_IN", "Đặt phòng không ở trạng thái đang lưu trú", {
      current_status: stay.booking_status,
      allowed_statuses: BOOKING_STATUS.CHECKED_IN,
    });
  }
}

function assertRoomsCanCheckout(rooms) {
  if (rooms.length === 0) {
    throw new BusinessError(409, "BOOKING_HAS_NO_ROOM", "Đặt phòng chưa có thông tin phòng");
  }
  const invalidRooms = rooms.filter((room) => !ROOM_STATUS.OCCUPIED.includes(room.trang_thai));
  if (invalidRooms.length > 0) {
    throw new BusinessError(409, "ROOM_NOT_OCCUPIED", "Có phòng không ở trạng thái đang sử dụng", {
      rooms: invalidRooms.map((room) => ({
        id_phong: room.id_phong,
        ten_phong: room.ten_phong,
        trang_thai: room.trang_thai,
      })),
      allowed_statuses: ROOM_STATUS.OCCUPIED,
    });
  }
}

async function persistOptionalCheckoutFields(client, invoiceId, stayId, note, vatRequested) {
  const hasInvoiceNote = await hasColumn(client, "hoa_don", "ghi_chu");
  if (hasInvoiceNote && note !== null) {
    await client.query("UPDATE public.hoa_don SET ghi_chu = $1 WHERE id_hoadon = $2", [note, invoiceId]);
  }

  const hasStayNote = await hasColumn(client, "luu_tru", "ghi_chu");
  if (hasStayNote && note !== null) {
    await client.query("UPDATE public.luu_tru SET ghi_chu = $1 WHERE id_luutru = $2", [note, stayId]);
  }

  const hasVatFlag = await hasColumn(client, "hoa_don", "yeu_cau_vat");
  if (hasVatFlag) {
    await client.query("UPDATE public.hoa_don SET yeu_cau_vat = $1 WHERE id_hoadon = $2", [
      Boolean(vatRequested),
      invoiceId,
    ]);
  }
}

function createCheckoutRouter(pool) {
  const router = express.Router();

  router.get("/v2/checkouts", asyncRoute(async (req, res) => {
    const values = [BOOKING_STATUS.CHECKED_IN];
    const where = [
      "dp.trang_thai = ANY($1::text[])",
      "lt.thoi_gian_checkin_thuc_te IS NOT NULL",
      "lt.thoi_gian_checkout_thuc_te IS NULL",
      "p.trang_thai = 'Bận'",
    ];

    if (req.query.date) {
      values.push(req.query.date);
      where.push(`dp.ngay_tra::date = $${values.length}::date`);
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

    const result = await pool.query(
      `
      SELECT DISTINCT ON (p.id_phong)
        lt.id_luutru,
        lt.ma_dat_phong,
        lt.thoi_gian_checkin_thuc_te,
        lt.thoi_gian_checkout_thuc_te,
        lt.so_nguoi_thuc_te,
        dp.ngay_tra,
        dp.so_nguoi_lon,
        dp.so_tre_em,
        dp.tong_so_nguoi,
        dp.trang_thai AS booking_status,
        dp.email,
        dp.tien_coc,
        dp.tong_thanh_toan,
        COALESCE(kh.ho_ten, dp.ten_nguoi_dat) AS customer_name,
        COALESCE(kh.sdt, dp.sdt_nguoi_dat) AS customer_phone,
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
      FROM public.luu_tru lt
      JOIN public.dat_phong dp ON dp.ma_dat_phong = lt.ma_dat_phong
      LEFT JOIN public.khach_hang kh ON kh.id_kh = dp.id_kh
      LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.ma_dat_phong = dp.ma_dat_phong
      LEFT JOIN public.phong p ON p.id_phong = ctdp.id_phong
      WHERE ${where.join(" AND ")}
      GROUP BY
        p.id_phong,
        lt.id_luutru,
        lt.ma_dat_phong,
        lt.thoi_gian_checkin_thuc_te,
        lt.thoi_gian_checkout_thuc_te,
        lt.so_nguoi_thuc_te,
        dp.ma_dat_phong,
        dp.ngay_tra,
        dp.so_nguoi_lon,
        dp.so_tre_em,
        dp.tong_so_nguoi,
        dp.trang_thai,
        dp.email,
        dp.tien_coc,
        dp.tong_thanh_toan,
        dp.ten_nguoi_dat,
        dp.sdt_nguoi_dat,
        kh.ho_ten,
        kh.sdt
      ORDER BY p.id_phong, lt.id_luutru DESC, lt.thoi_gian_checkin_thuc_te DESC
      `,
      values
    );

    const client = await pool.connect();
    try {
      const data = [];
      for (const row of result.rows) {
        try {
          const stay = mapStayRow(row);
          const draft = await buildDraftBill(client, row, stay.rooms, null);
          data.push({
            ...draft,
            status_label: "Đang lưu trú",
          });
        } catch (rowErr) {
          console.error(`Error processing stay ${row.id_luutru}:`, rowErr);
          // Tiếp tục xử lý các hàng khác nếu một hàng bị lỗi
        }
      }

      res.json({
        success: true,
        data,
        meta: {
          booking_statuses: BOOKING_STATUS.CHECKED_IN,
          room_fee_strategy: ROOM_FEE_STRATEGY,
        },
      });
    } finally {
      client.release();
    }
  }));

  const createDraftByBooking = asyncRoute(async (req, res) => {
    const maDatPhong = String(req.params.maDatPhong || "").trim();
    const client = await pool.connect();
    try {
      await client.query("BEGIN");

      const stay = await getStayForUpdateByBooking(client, maDatPhong);
      assertStayCanCheckout(stay);

      const rooms = await getRoomsForBooking(client, maDatPhong, false);
      const draftWithoutInvoice = await buildDraftBill(client, stay, rooms, null);
      const invoice = await createOrUpdateDraftInvoice(client, stay, draftWithoutInvoice);
      const draft = await buildDraftBill(client, stay, rooms, invoice);

      await client.query("COMMIT");
      res.json({ success: true, data: draft });
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  });

  const createDraftByStay = asyncRoute(async (req, res) => {
    const idLuutru = parseRequiredId(req.params.idLuutru, "ID lưu trú");
    const client = await pool.connect();
    try {
      await client.query("BEGIN");

      const stay = await getStayForUpdateById(client, idLuutru);
      assertStayCanCheckout(stay);

      const rooms = await getRoomsForBooking(client, stay.ma_dat_phong, false);
      const draftWithoutInvoice = await buildDraftBill(client, stay, rooms, null);
      const invoice = await createOrUpdateDraftInvoice(client, stay, draftWithoutInvoice);
      const draft = await buildDraftBill(client, stay, rooms, invoice);

      await client.query("COMMIT");
      res.json({ success: true, data: draft });
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  });

  router.get("/v2/checkouts/:maDatPhong/draft-bill", createDraftByBooking);
  router.post("/v2/checkouts/:maDatPhong/draft-bill", createDraftByBooking);
  router.get("/v2/stays/:idLuutru/draft-bill", createDraftByStay);
  router.post("/v2/stays/:idLuutru/draft-bill", createDraftByStay);

  router.post("/v2/invoices/:idHoaDon/pay", asyncRoute(async (req, res) => {
    const idHoaDon = parseRequiredId(req.params.idHoaDon, "ID hóa đơn");
    const body = req.body || {};
    const method = normalizePaymentMethod(body.phuong_thuc);
    const paidAmount = parseMoney(body.so_tien, "Số tiền thanh toán");
    const note = body.ghi_chu || body.note ? String(body.ghi_chu || body.note).trim() : null;
    const vatRequested = Boolean(body.yeu_cau_vat || body.request_vat);

    const client = await pool.connect();
    try {
      await client.query("BEGIN");

      const invoice = await getInvoiceForUpdate(client, idHoaDon);
      if (!invoice) {
        throw new BusinessError(404, "INVOICE_NOT_FOUND", "Không tìm thấy hóa đơn");
      }
      assertInvoiceCanBePaid(invoice);

      const stay = await resolveStayForInvoice(client, invoice, body);
      assertStayCanCheckout(stay);

      const rooms = await getRoomsForBooking(client, stay.ma_dat_phong, true);
      assertRoomsCanCheckout(rooms);

      const draft = await buildDraftBill(client, stay, rooms, invoice);
      if (paidAmount !== draft.amount_due) {
        throw new BusinessError(400, "PAYMENT_AMOUNT_MISMATCH", "Số tiền thanh toán không khớp với hóa đơn tạm tính", {
          expected_amount: draft.amount_due,
          received_amount: paidAmount,
        });
      }

      const payment = await client.query(
        `
        INSERT INTO public.thanh_toan (so_tien, phuong_thuc, trang_thai, id_hoadon)
        VALUES ($1, $2, $3, $4)
        RETURNING *
        `,
        [paidAmount, method, INVOICE_STATUS.PAID, idHoaDon]
      );

      await client.query(
        "UPDATE public.hoa_don SET tong_tien = $1, trang_thai = $2 WHERE id_hoadon = $3",
        [draft.amount_due, INVOICE_STATUS.PAID, idHoaDon]
      );
      await persistOptionalCheckoutFields(client, idHoaDon, stay.id_luutru, note, vatRequested);
      const notePersisted = Boolean(
        (await hasColumn(client, "hoa_don", "ghi_chu")) ||
        (await hasColumn(client, "luu_tru", "ghi_chu"))
      );
      const vatFlagPersisted = await hasColumn(client, "hoa_don", "yeu_cau_vat");
      await client.query(
        "UPDATE public.luu_tru SET thoi_gian_checkout_thuc_te = CURRENT_TIMESTAMP WHERE id_luutru = $1",
        [stay.id_luutru]
      );
      await client.query(
        "UPDATE public.dat_phong SET trang_thai = $1 WHERE ma_dat_phong = $2",
        [BOOKING_STATUS.COMPLETED, stay.ma_dat_phong]
      );
      await client.query(
        "UPDATE public.phong SET trang_thai = $1 WHERE id_phong = ANY($2::int[])",
        [ROOM_STATUS.AFTER_CHECKOUT, rooms.map((room) => room.id_phong)]
      );

      await client.query("COMMIT");
      res.json({
        success: true,
        message: "Thanh toán và trả phòng thành công",
        data: {
          invoice: {
            id_hoadon: idHoaDon,
            tong_tien: draft.amount_due,
            trang_thai: INVOICE_STATUS.PAID,
          },
          payment: payment.rows[0],
          checkout: {
            id_luutru: stay.id_luutru,
            ma_dat_phong: stay.ma_dat_phong,
            room_status_after_checkout: ROOM_STATUS.AFTER_CHECKOUT,
            note_persisted: notePersisted,
            vat_flag_persisted: vatFlagPersisted,
          },
        },
      });
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  }));

  return router;
}

module.exports = createCheckoutRouter;
