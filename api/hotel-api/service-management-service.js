/* ==========================================================================
   SERVICE MANAGEMENT SERVICE
   Refactored to match user's database schema:
   - dịch vụ -> public.dich_vu
   - tài sản -> public.tai_san
   - sử dụng dịch vụ -> public.su_dung_dich_vu (linked to id_luutru)
   - thiệt hại -> public.thiet_hai (linked to id_luutru)
   ========================================================================== */

const ACTIVE_ROOM_STATUSES = parseStatusList(process.env.ACTIVE_ROOM_STATUSES, [
  "Bận",
  "Đang ở",
  "Đã check-in",
]);

const ACTIVE_BOOKING_STATUSES = parseStatusList(process.env.ACTIVE_BOOKING_STATUSES, [
  "Đã check-in",
  "Đang ở",
  "Đang lưu trú",
]);

const CATALOGS = {
  service: {
    table: "public.dich_vu",
    linkTable: "public.su_dung_dich_vu",
    idColumn: "id_dichvu",
    pkColumn: "id_sudung_dv",
    nameColumn: "ten_dich_vu",
    priceColumn: "don_gia",
    totalColumn: "thanh_tien",
    notFoundMessage: "Dịch vụ không tồn tại.",
    deleteBlockedMessage: "Dịch vụ này đã được sử dụng, không thể xoá.",
  },
  asset: {
    table: "public.tai_san",
    linkTable: "public.thiet_hai",
    idColumn: "id_taisan",
    pkColumn: "id_thie_thai",
    nameColumn: "ten_tai_san",
    priceColumn: "gia_tri_boi_thuong",
    totalColumn: "so_tien_boi_thuong",
    notFoundMessage: "Tài sản/bồi thường không tồn tại.",
    deleteBlockedMessage: "Tài sản này đã có ghi nhận thiệt hại, không thể xoá.",
  },
};

class BusinessError extends Error {
  constructor(statusCode, message, error = "BUSINESS_ERROR", details = undefined) {
    super(message);
    this.statusCode = statusCode;
    this.error = error;
    this.details = details;
  }
}

function parseStatusList(raw, fallback) {
  if (!raw) return fallback;
  const statuses = raw.split(",").map((item) => item.trim()).filter(Boolean);
  return statuses.length > 0 ? statuses : fallback;
}

function parseId(value, fieldName) {
  const id = Number.parseInt(value, 10);
  if (!Number.isInteger(id) || id <= 0) {
    throw new BusinessError(400, `${fieldName} không hợp lệ.`, "INVALID_ID");
  }
  return id;
}

function parsePrice(value) {
  const parsed = Number(value || 0);
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 0;
  }
  return Math.round(parsed);
}

function parseQuantity(value) {
  const quantity = Number(value || 1);
  if (!Number.isInteger(quantity) || quantity <= 0) {
    return 1;
  }
  return quantity;
}

function floorOf(roomNumber) {
  const digits = String(roomNumber || "").replace(/\D+/g, "");
  if (digits.length >= 3) return Number.parseInt(digits.slice(0, -2), 10) || 1;
  if (digits.length > 0) return Number.parseInt(digits[0], 10) || 1;
  return 1;
}

function mapCatalogRow(row, kind) {
  const cfg = CATALOGS[kind];
  return {
    id: row[cfg.idColumn],
    name: row[cfg.nameColumn],
    price: Number(row[cfg.priceColumn] || 0),
    created_at: null, // Legacy fields not in user schema
    updated_at: null,
  };
}

function mapRoomRow(row) {
  const roomNumber = row.room_number || "";
  const booking = row.booking_id
    ? {
        id: row.booking_id,
        status: row.booking_status,
        check_in_date: row.check_in,
        check_out_date: row.check_out,
      }
    : null;

  return {
    room_id: row.room_id,
    room_number: roomNumber,
    floor: floorOf(roomNumber),
    status: row.room_status || row.booking_status || "Đang ở",
    booking_id: row.booking_id,
    stay_id: row.stay_id,
    customer_name: row.customer_name,
    check_in: row.check_in,
    check_out: row.check_out,
    room_fee: Number(row.room_fee || 0),
    current_booking: booking,
    is_occupied: Boolean(row.stay_id),
  };
}

function mapLineRow(row, kind) {
  const cfg = CATALOGS[kind];
  return {
    id: row[cfg.pkColumn],
    room_id: row.room_id,
    stay_id: row.id_luutru,
    [cfg.idColumn]: row[cfg.idColumn],
    name: row[cfg.nameColumn],
    quantity: Number(row.soluong || 1),
    price: Number(row[cfg.priceColumn] || 0),
    total_price: Number(row[cfg.totalColumn] || 0),
    created_at: row.thoi_gian || null,
  };
}

class ServiceManagementService {
  constructor(pool) {
    this.pool = pool;
  }

  async ensureSchema() {
    // Schema is managed by user, nothing to create.
    console.log("Using user-defined database schema for services.");
  }

  async listCatalog(kind, search) {
    const cfg = CATALOGS[kind];
    const values = [];
    let where = "";

    if (search && String(search).trim()) {
      values.push(`%${String(search).trim()}%`);
      where = `WHERE ${cfg.nameColumn} ILIKE $1`;
    }

    const result = await this.pool.query(
      `SELECT * FROM ${cfg.table} ${where} ORDER BY ${cfg.idColumn} DESC`,
      values
    );
    return result.rows.map(row => mapCatalogRow(row, kind));
  }

  async createCatalog(kind, body) {
    const cfg = CATALOGS[kind];
    const name = String(body.name || "").trim();
    if (!name) throw new BusinessError(400, "Tên không được để trống.", "NAME_REQUIRED");
    const price = parsePrice(body.price);

    const result = await this.pool.query(
      `INSERT INTO ${cfg.table} (${cfg.nameColumn}, ${cfg.priceColumn})
       VALUES ($1, $2)
       RETURNING *`,
      [name, price]
    );
    return mapCatalogRow(result.rows[0], kind);
  }

  async updateCatalog(kind, id, body) {
    const cfg = CATALOGS[kind];
    const catalogId = parseId(id, "ID danh mục");
    const name = String(body.name || "").trim();
    const price = parsePrice(body.price);

    const result = await this.pool.query(
      `UPDATE ${cfg.table}
       SET ${cfg.nameColumn} = COALESCE(NULLIF($1, ''), ${cfg.nameColumn}),
           ${cfg.priceColumn} = $2
       WHERE ${cfg.idColumn} = $3
       RETURNING *`,
      [name, price, catalogId]
    );
    if (result.rows.length === 0) throw new BusinessError(404, cfg.notFoundMessage, "NOT_FOUND");
    return mapCatalogRow(result.rows[0], kind);
  }

  async deleteCatalog(kind, id) {
    const cfg = CATALOGS[kind];
    const catalogId = parseId(id, "ID danh mục");
    
    // Check if used
    const used = await this.pool.query(
      `SELECT 1 FROM ${cfg.linkTable} WHERE ${cfg.idColumn} = $1 LIMIT 1`,
      [catalogId]
    );
    if (used.rows.length > 0) throw new BusinessError(409, cfg.deleteBlockedMessage, "CATALOG_IN_USE");

    await this.pool.query(`DELETE FROM ${cfg.table} WHERE ${cfg.idColumn} = $1`, [catalogId]);
    return { id: catalogId };
  }

  async getOccupiedRooms(search) {
    const queryValues = [];
    let searchCondition = "";

    if (search && String(search).trim()) {
      queryValues.push(`%${String(search).trim()}%`);
      searchCondition = `AND p.ten_phong ILIKE $1`;
    }

    const result = await this.pool.query(
      `
      SELECT DISTINCT ON (p.id_phong)
        p.id_phong AS room_id,
        p.ten_phong AS room_number,
        p.trang_thai AS room_status,
        dp.ma_dat_phong AS booking_id,
        dp.trang_thai AS booking_status,
        lt.id_luutru AS stay_id,
        COALESCE(kh.ho_ten, dp.ten_nguoi_dat) AS customer_name,
        dp.ngay_nhan AS check_in,
        dp.ngay_tra AS check_out,
        dp.tong_thanh_toan AS room_fee
      FROM public.phong p
      INNER JOIN public.chi_tiet_dat_phong ctdp ON ctdp.id_phong = p.id_phong
      INNER JOIN public.dat_phong dp ON dp.ma_dat_phong = ctdp.ma_dat_phong
      INNER JOIN public.luu_tru lt
        ON lt.ma_dat_phong = dp.ma_dat_phong
       AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
       AND lt.thoi_gian_checkout_thuc_te IS NULL
      LEFT JOIN public.khach_hang kh ON kh.id_kh = dp.id_kh
      WHERE p.trang_thai = 'Bận'
      ${searchCondition}
      ORDER BY p.id_phong, lt.id_luutru DESC
      `,
      queryValues
    );

    return result.rows.map(mapRoomRow);
  }

  async getRoomDetail(roomIdValue) {
    const roomId = parseId(roomIdValue, "ID phòng");
    const result = await this.pool.query(
      `
      SELECT
        p.id_phong AS room_id,
        p.ten_phong AS room_number,
        p.trang_thai AS room_status,
        dp.ma_dat_phong AS booking_id,
        dp.trang_thai AS booking_status,
        lt.id_luutru AS stay_id
      FROM public.phong p
      LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.id_phong = p.id_phong
      LEFT JOIN public.dat_phong dp ON dp.ma_dat_phong = ctdp.ma_dat_phong
      LEFT JOIN public.luu_tru lt
        ON lt.ma_dat_phong = dp.ma_dat_phong
       AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
       AND lt.thoi_gian_checkout_thuc_te IS NULL
      WHERE p.id_phong = $1
      ORDER BY lt.id_luutru DESC NULLS LAST, dp.ngay_nhan DESC NULLS LAST
      LIMIT 1
      `,
      [roomId]
    );

    if (result.rows.length === 0) throw new BusinessError(404, "Phòng không tồn tại.", "ROOM_NOT_FOUND");
    return mapRoomRow(result.rows[0]);
  }

  async listRoomLines(kind, roomIdValue) {
    const cfg = CATALOGS[kind];
    const room = await this.getRoomDetail(roomIdValue);
    if (!room.stay_id) return [];

    const result = await this.pool.query(
      `SELECT rl.*, c.${cfg.nameColumn}, c.${cfg.priceColumn}
       FROM ${cfg.linkTable} rl
       JOIN ${cfg.table} c ON c.${cfg.idColumn} = rl.${cfg.idColumn}
       WHERE rl.id_luutru = $1
       ORDER BY rl.${cfg.pkColumn} ASC`,
      [room.stay_id]
    );
    return result.rows.map(row => mapLineRow({ ...row, room_id: room.room_id }, kind));
  }

  async addRoomLine(kind, roomIdValue, body) {
    const cfg = CATALOGS[kind];
    const room = await this.getRoomDetail(roomIdValue);
    if (!room.stay_id) throw new BusinessError(409, "Phòng hiện không trong trạng thái đang lưu trú.", "ROOM_NOT_OCCUPIED");

    const catalogId = parseId(body?.[cfg.idColumn] || body?.catalog_id, "ID danh mục");
    const quantity = parseQuantity(body?.quantity);

    const catalog = await this.pool.query(`SELECT * FROM ${cfg.table} WHERE ${cfg.idColumn} = $1`, [catalogId]);
    if (catalog.rows.length === 0) throw new BusinessError(404, cfg.notFoundMessage, "NOT_FOUND");

    const price = Number(catalog.rows[0][cfg.priceColumn] || 0);
    const totalPrice = Math.round(price * quantity);

    const sql = kind === "service" 
      ? `INSERT INTO public.su_dung_dich_vu (id_luutru, id_dichvu, soluong, thanh_tien) VALUES ($1, $2, $3, $4) RETURNING *`
      : `INSERT INTO public.thiet_hai (id_luutru, id_taisan, muc_do, so_tien_boi_thuong, trang_thai) VALUES ($1, $2, $3, $4, 'Chưa xử lý') RETURNING *`;
    
    const params = kind === "service"
      ? [room.stay_id, catalogId, quantity, totalPrice]
      : [room.stay_id, catalogId, body.muc_do || "Trung bình", totalPrice];

    const result = await this.pool.query(sql, params);
    return this.loadLine(kind, result.rows[0][cfg.pkColumn]);
  }

  async updateRoomLine(kind, roomIdValue, lineIdValue, body) {
    const cfg = CATALOGS[kind];
    const lineId = parseId(lineIdValue, "ID dòng");
    const quantity = parseQuantity(body?.quantity);
    
    if (kind === "service") {
      const current = await this.pool.query(
        `SELECT rl.*, c.${cfg.priceColumn} FROM ${cfg.linkTable} rl JOIN ${cfg.table} c ON c.${cfg.idColumn} = rl.${cfg.idColumn} WHERE ${cfg.pkColumn} = $1`,
        [lineId]
      );
      if (current.rows.length === 0) throw new BusinessError(404, "Không tìm thấy dữ liệu.", "NOT_FOUND");
      
      const totalPrice = Math.round(Number(current.rows[0][cfg.priceColumn]) * quantity);
      await this.pool.query(
        `UPDATE public.su_dung_dich_vu SET soluong = $1, thanh_tien = $2 WHERE id_sudung_dv = $3`,
        [quantity, totalPrice, lineId]
      );
    } else {
      const price = parsePrice(body.price);
      await this.pool.query(
        `UPDATE public.thiet_hai SET muc_do = $1, so_tien_boi_thuong = $2, trang_thai = $3 WHERE id_thie_thai = $4`,
        [body.muc_do || "Trung bình", price, body.trang_thai || "Chưa xử lý", lineId]
      );
    }
    
    return this.loadLine(kind, lineId);
  }

  async deleteRoomLine(kind, roomIdValue, lineIdValue) {
    const cfg = CATALOGS[kind];
    const lineId = parseId(lineIdValue, "ID dòng");
    await this.pool.query(`DELETE FROM ${cfg.linkTable} WHERE ${cfg.pkColumn} = $1`, [lineId]);
    return { id: lineId };
  }

  async loadLine(kind, lineId) {
    const cfg = CATALOGS[kind];
    const result = await this.pool.query(
      `SELECT rl.*, c.${cfg.nameColumn}, c.${cfg.priceColumn}
       FROM ${cfg.linkTable} rl
       JOIN ${cfg.table} c ON c.${cfg.idColumn} = rl.${cfg.idColumn}
       WHERE rl.${cfg.pkColumn} = $1`,
      [lineId]
    );
    if (result.rows.length === 0) throw new BusinessError(404, "Không tìm thấy dữ liệu.", "NOT_FOUND");
    return mapLineRow(result.rows[0], kind);
  }
}

module.exports = {
  ServiceManagementService,
  BusinessError,
};
