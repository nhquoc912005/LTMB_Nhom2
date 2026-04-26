const ACTIVE_ROOM_STATUSES = parseStatusList(process.env.ACTIVE_ROOM_STATUSES, [
  "OCCUPIED",
  "Đang sử dụng",
  "Đang lưu trú",
]);

const ACTIVE_BOOKING_STATUSES = parseStatusList(process.env.ACTIVE_BOOKING_STATUSES, [
  "CHECKED_IN",
  "Đã check-in",
  "Đang lưu trú",
]);

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

const CATALOGS = {
  service: {
    table: "public.services",
    linkTable: "public.room_services",
    idColumn: "service_id",
    nameAlias: "service_name",
    notFoundMessage: "Dịch vụ không tồn tại.",
    deleteBlockedMessage: "Dịch vụ này đã được gán vào phòng, không thể xoá.",
  },
  asset: {
    table: "public.assets",
    linkTable: "public.room_assets",
    idColumn: "asset_id",
    nameAlias: "asset_name",
    notFoundMessage: "Tài sản/bồi thường không tồn tại.",
    deleteBlockedMessage: "Tài sản/bồi thường này đã được gán vào phòng, không thể xoá.",
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

function parseUuid(value, fieldName) {
  const normalized = String(value || "").trim();
  if (!UUID_PATTERN.test(normalized)) {
    throw new BusinessError(400, `${fieldName} không hợp lệ.`, "INVALID_ID");
  }
  return normalized;
}

function parseRoomId(value) {
  const roomId = Number.parseInt(value, 10);
  if (!Number.isInteger(roomId) || roomId <= 0 || String(value).trim() !== String(roomId)) {
    throw new BusinessError(400, "ID phòng không hợp lệ.", "INVALID_ROOM_ID");
  }
  return roomId;
}

function parsePrice(value) {
  if (typeof value === "string" && value.trim() === "") {
    throw new BusinessError(400, "Giá tiền phải lớn hơn hoặc bằng 0.", "INVALID_PRICE");
  }

  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed < 0) {
    throw new BusinessError(400, "Giá tiền phải lớn hơn hoặc bằng 0.", "INVALID_PRICE");
  }
  return Number(parsed.toFixed(2));
}

function parseQuantity(value) {
  if (typeof value === "string" && !/^\d+$/.test(value.trim())) {
    throw new BusinessError(400, "Số lượng phải là số nguyên dương.", "INVALID_QUANTITY");
  }

  const quantity = Number(value);
  if (!Number.isInteger(quantity) || quantity <= 0) {
    throw new BusinessError(400, "Số lượng phải là số nguyên dương.", "INVALID_QUANTITY");
  }
  return quantity;
}

function normalizeNullableText(value) {
  if (value === undefined || value === null) return null;
  const normalized = String(value).trim();
  return normalized || null;
}

function normalizeCatalogPayload(body, current = undefined) {
  const name = String(body.name ?? current?.name ?? "").trim();
  if (!name) {
    throw new BusinessError(400, "Tên không được để trống.", "NAME_REQUIRED");
  }

  return {
    name,
    price: parsePrice(body.price ?? current?.price ?? 0),
    unit: body.unit === undefined ? (current?.unit ?? null) : normalizeNullableText(body.unit),
    icon: body.icon === undefined ? (current?.icon ?? null) : normalizeNullableText(body.icon),
  };
}

function calculateTotalPrice(price, quantity) {
  return Number((Number(price) * Number(quantity)).toFixed(2));
}

function floorOf(roomNumber) {
  const digits = String(roomNumber || "").replace(/\D+/g, "");
  if (digits.length >= 3) return Number.parseInt(digits.slice(0, -2), 10) || 1;
  if (digits.length > 0) return Number.parseInt(digits[0], 10) || 1;
  return 1;
}

function mapCatalogRow(row) {
  return {
    id: row.id,
    name: row.name,
    price: Number(row.price || 0),
    unit: row.unit,
    icon: row.icon,
    created_at: row.created_at,
    updated_at: row.updated_at,
  };
}

function mapRoomRow(row) {
  const roomNumber = row.room_number || "";
  const booking = row.booking_id
    ? {
        id: row.booking_id,
        status: row.booking_status,
        check_in_date: row.check_in_date,
        check_out_date: row.check_out_date,
      }
    : null;

  return {
    room_id: row.room_id,
    room_number: roomNumber,
    floor: floorOf(roomNumber),
    status: row.room_status || row.booking_status || null,
    booking_id: row.booking_id,
    current_booking: booking,
    check_in_date: row.check_in_date,
    check_out_date: row.check_out_date,
    expected_check_in: row.check_in_date,
    expected_check_out: row.check_out_date,
    room_price: Number(row.room_price || 0),
    room_fee: Number(row.room_fee || row.room_price || 0),
    is_occupied: Boolean(row.is_occupied),
  };
}

function mapLineRow(row, kind) {
  const cfg = CATALOGS[kind];
  return {
    id: row.id,
    room_id: row.room_id,
    [cfg.idColumn]: row[cfg.idColumn],
    [cfg.nameAlias]: row.name,
    name: row.name,
    quantity: Number(row.quantity || 0),
    price: Number(row.price || 0),
    total_price: Number(row.total_price || 0),
    created_at: row.created_at,
    updated_at: row.updated_at,
  };
}

class ServiceManagementService {
  constructor(pool) {
    this.pool = pool;
  }

  async ensureSchema() {
    await this.pool.query(`
      CREATE EXTENSION IF NOT EXISTS pgcrypto;

      CREATE OR REPLACE FUNCTION public.set_updated_at()
      RETURNS TRIGGER AS $$
      BEGIN
        NEW.updated_at = NOW();
        RETURN NEW;
      END;
      $$ LANGUAGE plpgsql;

      CREATE TABLE IF NOT EXISTS public.services (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name TEXT NOT NULL,
        price NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (price >= 0),
        unit TEXT,
        icon TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );

      CREATE TABLE IF NOT EXISTS public.assets (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name TEXT NOT NULL,
        price NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (price >= 0),
        unit TEXT,
        icon TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );

      CREATE TABLE IF NOT EXISTS public.room_services (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        room_id INTEGER NOT NULL REFERENCES public.phong(id_phong) ON DELETE CASCADE,
        service_id UUID NOT NULL REFERENCES public.services(id) ON DELETE RESTRICT,
        quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
        price NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
        total_price NUMERIC(15, 2) NOT NULL CHECK (total_price >= 0),
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );

      CREATE TABLE IF NOT EXISTS public.room_assets (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        room_id INTEGER NOT NULL REFERENCES public.phong(id_phong) ON DELETE CASCADE,
        asset_id UUID NOT NULL REFERENCES public.assets(id) ON DELETE RESTRICT,
        quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
        price NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
        total_price NUMERIC(15, 2) NOT NULL CHECK (total_price >= 0),
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );

      CREATE INDEX IF NOT EXISTS idx_services_name_lower ON public.services (LOWER(name));
      CREATE INDEX IF NOT EXISTS idx_assets_name_lower ON public.assets (LOWER(name));
      CREATE INDEX IF NOT EXISTS idx_room_services_room_id ON public.room_services(room_id);
      CREATE INDEX IF NOT EXISTS idx_room_assets_room_id ON public.room_assets(room_id);

      DROP TRIGGER IF EXISTS trg_services_updated_at ON public.services;
      CREATE TRIGGER trg_services_updated_at
        BEFORE UPDATE ON public.services
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

      DROP TRIGGER IF EXISTS trg_assets_updated_at ON public.assets;
      CREATE TRIGGER trg_assets_updated_at
        BEFORE UPDATE ON public.assets
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

      DROP TRIGGER IF EXISTS trg_room_services_updated_at ON public.room_services;
      CREATE TRIGGER trg_room_services_updated_at
        BEFORE UPDATE ON public.room_services
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

      DROP TRIGGER IF EXISTS trg_room_assets_updated_at ON public.room_assets;
      CREATE TRIGGER trg_room_assets_updated_at
        BEFORE UPDATE ON public.room_assets
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

      DO $$
      BEGIN
        IF to_regclass('public.dich_vu') IS NOT NULL
           AND NOT EXISTS (SELECT 1 FROM public.services) THEN
          INSERT INTO public.services (name, price)
          SELECT ten_dich_vu, COALESCE(don_gia, 0)
          FROM public.dich_vu
          WHERE ten_dich_vu IS NOT NULL;
        END IF;

        IF to_regclass('public.tai_san') IS NOT NULL
           AND NOT EXISTS (SELECT 1 FROM public.assets) THEN
          INSERT INTO public.assets (name, price)
          SELECT ten_tai_san, MAX(COALESCE(gia_tri_boi_thuong, 0))
          FROM public.tai_san
          WHERE ten_tai_san IS NOT NULL
          GROUP BY ten_tai_san;
        END IF;

        IF to_regclass('public.service') IS NOT NULL THEN
          INSERT INTO public.services (name, price, unit, icon)
          SELECT s.name, COALESCE(s.price, 0), s.unit, s.icon
          FROM public.service s
          WHERE s.name IS NOT NULL
            AND NOT EXISTS (
              SELECT 1
              FROM public.services ns
              WHERE LOWER(ns.name) = LOWER(s.name)
            );
        END IF;

        IF to_regclass('public.asset') IS NOT NULL THEN
          INSERT INTO public.assets (name, price, unit, icon)
          SELECT a.name, COALESCE(a.price, 0), a.unit, a.icon
          FROM public.asset a
          WHERE a.name IS NOT NULL
            AND NOT EXISTS (
              SELECT 1
              FROM public.assets na
              WHERE LOWER(na.name) = LOWER(a.name)
            );
        END IF;
      END $$;
    `);
  }

  async listCatalog(kind, search) {
    const cfg = CATALOGS[kind];
    const values = [];
    let where = "";

    if (search && String(search).trim()) {
      values.push(`%${String(search).trim()}%`);
      where = "WHERE name ILIKE $1";
    }

    const result = await this.pool.query(
      `SELECT id, name, price, unit, icon, created_at, updated_at
       FROM ${cfg.table}
       ${where}
       ORDER BY created_at DESC, updated_at DESC`,
      values
    );
    return result.rows.map(mapCatalogRow);
  }

  async createCatalog(kind, body) {
    const cfg = CATALOGS[kind];
    const payload = normalizeCatalogPayload(body || {});
    const result = await this.pool.query(
      `INSERT INTO ${cfg.table} (name, price, unit, icon)
       VALUES ($1, $2, $3, $4)
       RETURNING id, name, price, unit, icon, created_at, updated_at`,
      [payload.name, payload.price, payload.unit, payload.icon]
    );
    return mapCatalogRow(result.rows[0]);
  }

  async updateCatalog(kind, id, body) {
    const cfg = CATALOGS[kind];
    const catalogId = parseUuid(id, "ID danh mục");
    const current = await this.pool.query(`SELECT * FROM ${cfg.table} WHERE id = $1`, [catalogId]);
    if (current.rows.length === 0) {
      throw new BusinessError(404, cfg.notFoundMessage, "NOT_FOUND");
    }

    const payload = normalizeCatalogPayload(body || {}, current.rows[0]);
    const result = await this.pool.query(
      `UPDATE ${cfg.table}
       SET name = $1, price = $2, unit = $3, icon = $4, updated_at = NOW()
       WHERE id = $5
       RETURNING id, name, price, unit, icon, created_at, updated_at`,
      [payload.name, payload.price, payload.unit, payload.icon, catalogId]
    );
    return mapCatalogRow(result.rows[0]);
  }

  async deleteCatalog(kind, id) {
    const cfg = CATALOGS[kind];
    const catalogId = parseUuid(id, "ID danh mục");
    const current = await this.pool.query(`SELECT id FROM ${cfg.table} WHERE id = $1`, [catalogId]);
    if (current.rows.length === 0) {
      throw new BusinessError(404, cfg.notFoundMessage, "NOT_FOUND");
    }

    const used = await this.pool.query(`SELECT 1 FROM ${cfg.linkTable} WHERE ${cfg.idColumn} = $1 LIMIT 1`, [catalogId]);
    if (used.rows.length > 0) {
      throw new BusinessError(409, cfg.deleteBlockedMessage, "CATALOG_IN_USE");
    }

    await this.pool.query(`DELETE FROM ${cfg.table} WHERE id = $1`, [catalogId]);
    return { id: catalogId };
  }

  async getOccupiedRooms(search) {
    const values = [ACTIVE_ROOM_STATUSES, ACTIVE_BOOKING_STATUSES];
    let searchCondition = "";

    if (search && String(search).trim()) {
      values.push(`%${String(search).trim()}%`);
      searchCondition = `AND p.ten_phong ILIKE $${values.length}`;
    }

    const result = await this.pool.query(
      `
      WITH occupied_rooms AS (
        SELECT DISTINCT ON (p.id_phong)
          p.id_phong AS room_id,
          p.ten_phong AS room_number,
          p.trang_thai AS room_status,
          p.gia_phong AS room_price,
          dp.ma_dat_phong AS booking_id,
          dp.trang_thai AS booking_status,
          dp.ngay_nhan AS check_in_date,
          dp.ngay_tra AS check_out_date,
          dp.tong_thanh_toan AS room_fee,
          lt.id_luutru AS stay_id,
          TRUE AS is_occupied
        FROM public.phong p
        LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.id_phong = p.id_phong
        LEFT JOIN public.dat_phong dp ON dp.ma_dat_phong = ctdp.ma_dat_phong
        LEFT JOIN public.luu_tru lt
          ON lt.ma_dat_phong = dp.ma_dat_phong
         AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
         AND lt.thoi_gian_checkout_thuc_te IS NULL
        WHERE (
          p.trang_thai = ANY($1::text[])
          OR dp.trang_thai = ANY($2::text[])
          OR lt.id_luutru IS NOT NULL
        )
        ${searchCondition}
        ORDER BY p.id_phong, lt.id_luutru DESC NULLS LAST, dp.ngay_nhan DESC NULLS LAST
      )
      SELECT *
      FROM occupied_rooms
      ORDER BY room_number ASC
      `,
      values
    );

    return result.rows.map(mapRoomRow);
  }

  async getRoomDetail(roomIdValue) {
    const roomId = parseRoomId(roomIdValue);
    const result = await this.pool.query(
      `
      SELECT
        p.id_phong AS room_id,
        p.ten_phong AS room_number,
        p.trang_thai AS room_status,
        p.gia_phong AS room_price,
        dp.ma_dat_phong AS booking_id,
        dp.trang_thai AS booking_status,
        dp.ngay_nhan AS check_in_date,
        dp.ngay_tra AS check_out_date,
        dp.tong_thanh_toan AS room_fee,
        lt.id_luutru AS stay_id,
        (
          p.trang_thai = ANY($2::text[])
          OR dp.trang_thai = ANY($3::text[])
          OR lt.id_luutru IS NOT NULL
        ) AS is_occupied
      FROM public.phong p
      LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.id_phong = p.id_phong
      LEFT JOIN public.dat_phong dp ON dp.ma_dat_phong = ctdp.ma_dat_phong
      LEFT JOIN public.luu_tru lt
        ON lt.ma_dat_phong = dp.ma_dat_phong
       AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
       AND lt.thoi_gian_checkout_thuc_te IS NULL
      WHERE p.id_phong = $1
      ORDER BY
        CASE
          WHEN lt.id_luutru IS NOT NULL THEN 0
          WHEN dp.trang_thai = ANY($3::text[]) THEN 1
          ELSE 2
        END,
        lt.id_luutru DESC NULLS LAST,
        dp.ngay_nhan DESC NULLS LAST
      LIMIT 1
      `,
      [roomId, ACTIVE_ROOM_STATUSES, ACTIVE_BOOKING_STATUSES]
    );

    if (result.rows.length === 0) {
      throw new BusinessError(404, "Phòng không tồn tại.", "ROOM_NOT_FOUND");
    }
    return mapRoomRow(result.rows[0]);
  }

  async listRoomLines(kind, roomIdValue) {
    const cfg = CATALOGS[kind];
    const roomId = parseRoomId(roomIdValue);
    await this.assertRoomExists(roomId);

    const result = await this.pool.query(
      `SELECT rl.*, c.name
       FROM ${cfg.linkTable} rl
       JOIN ${cfg.table} c ON c.id = rl.${cfg.idColumn}
       WHERE rl.room_id = $1
       ORDER BY rl.created_at ASC, rl.id ASC`,
      [roomId]
    );
    return result.rows.map((row) => mapLineRow(row, kind));
  }

  async addRoomLine(kind, roomIdValue, body) {
    const cfg = CATALOGS[kind];
    const roomId = parseRoomId(roomIdValue);
    const catalogId = parseUuid(body?.[cfg.idColumn] || body?.catalog_id, "ID danh mục");
    const quantity = parseQuantity(body?.quantity);

    const client = await this.pool.connect();
    try {
      await client.query("BEGIN");
      await this.assertRoomOccupied(roomId, client);

      const catalog = await client.query(`SELECT id, price FROM ${cfg.table} WHERE id = $1 FOR SHARE`, [catalogId]);
      if (catalog.rows.length === 0) {
        throw new BusinessError(404, cfg.notFoundMessage, "NOT_FOUND");
      }

      const price = parsePrice(catalog.rows[0].price);
      const totalPrice = calculateTotalPrice(price, quantity);
      const inserted = await client.query(
        `INSERT INTO ${cfg.linkTable} (room_id, ${cfg.idColumn}, quantity, price, total_price)
         VALUES ($1, $2, $3, $4, $5)
         RETURNING id`,
        [roomId, catalogId, quantity, price, totalPrice]
      );
      const line = await this.loadRoomLine(kind, inserted.rows[0].id, roomId, client);
      await client.query("COMMIT");
      return line;
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  }

  async updateRoomLine(kind, roomIdValue, lineIdValue, body) {
    const cfg = CATALOGS[kind];
    const roomId = roomIdValue === null || roomIdValue === undefined ? null : parseRoomId(roomIdValue);
    const lineId = parseUuid(lineIdValue, "ID dòng");
    const quantity = parseQuantity(body?.quantity);

    const current = await this.loadRoomLine(kind, lineId, roomId);
    const totalPrice = calculateTotalPrice(current.price, quantity);
    const values = [quantity, totalPrice, lineId];
    let roomCondition = "";
    if (roomId !== null) {
      values.push(roomId);
      roomCondition = `AND room_id = $${values.length}`;
    }

    await this.pool.query(
      `UPDATE ${cfg.linkTable}
       SET quantity = $1, total_price = $2, updated_at = NOW()
       WHERE id = $3 ${roomCondition}`,
      values
    );
    return this.loadRoomLine(kind, lineId, roomId);
  }

  async deleteRoomLine(kind, roomIdValue, lineIdValue) {
    const cfg = CATALOGS[kind];
    const roomId = roomIdValue === null || roomIdValue === undefined ? null : parseRoomId(roomIdValue);
    const lineId = parseUuid(lineIdValue, "ID dòng");
    await this.loadRoomLine(kind, lineId, roomId);

    const values = [lineId];
    let roomCondition = "";
    if (roomId !== null) {
      values.push(roomId);
      roomCondition = `AND room_id = $${values.length}`;
    }

    await this.pool.query(`DELETE FROM ${cfg.linkTable} WHERE id = $1 ${roomCondition}`, values);
    return { id: lineId };
  }

  async assertRoomExists(roomId, client = this.pool) {
    const result = await client.query("SELECT id_phong FROM public.phong WHERE id_phong = $1", [roomId]);
    if (result.rows.length === 0) {
      throw new BusinessError(404, "Phòng không tồn tại.", "ROOM_NOT_FOUND");
    }
  }

  async assertRoomOccupied(roomId, client = this.pool) {
    const detail = await this.getRoomDetailWithClient(roomId, client);
    if (!detail.is_occupied) {
      throw new BusinessError(409, "Phòng hiện không trong trạng thái đang lưu trú.", "ROOM_NOT_OCCUPIED");
    }
    return detail;
  }

  async getRoomDetailWithClient(roomId, client) {
    const result = await client.query(
      `
      SELECT
        p.id_phong AS room_id,
        p.ten_phong AS room_number,
        p.trang_thai AS room_status,
        p.gia_phong AS room_price,
        dp.ma_dat_phong AS booking_id,
        dp.trang_thai AS booking_status,
        dp.ngay_nhan AS check_in_date,
        dp.ngay_tra AS check_out_date,
        dp.tong_thanh_toan AS room_fee,
        lt.id_luutru AS stay_id,
        (
          p.trang_thai = ANY($2::text[])
          OR dp.trang_thai = ANY($3::text[])
          OR lt.id_luutru IS NOT NULL
        ) AS is_occupied
      FROM public.phong p
      LEFT JOIN public.chi_tiet_dat_phong ctdp ON ctdp.id_phong = p.id_phong
      LEFT JOIN public.dat_phong dp ON dp.ma_dat_phong = ctdp.ma_dat_phong
      LEFT JOIN public.luu_tru lt
        ON lt.ma_dat_phong = dp.ma_dat_phong
       AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
       AND lt.thoi_gian_checkout_thuc_te IS NULL
      WHERE p.id_phong = $1
      ORDER BY
        CASE
          WHEN lt.id_luutru IS NOT NULL THEN 0
          WHEN dp.trang_thai = ANY($3::text[]) THEN 1
          ELSE 2
        END,
        lt.id_luutru DESC NULLS LAST,
        dp.ngay_nhan DESC NULLS LAST
      LIMIT 1
      `,
      [roomId, ACTIVE_ROOM_STATUSES, ACTIVE_BOOKING_STATUSES]
    );

    if (result.rows.length === 0) {
      throw new BusinessError(404, "Phòng không tồn tại.", "ROOM_NOT_FOUND");
    }
    return mapRoomRow(result.rows[0]);
  }

  async loadRoomLine(kind, lineId, roomId = null, client = this.pool) {
    const cfg = CATALOGS[kind];
    const values = [lineId];
    let roomCondition = "";
    if (roomId !== null && roomId !== undefined) {
      values.push(roomId);
      roomCondition = `AND rl.room_id = $${values.length}`;
    }

    const result = await client.query(
      `SELECT rl.*, c.name
       FROM ${cfg.linkTable} rl
       JOIN ${cfg.table} c ON c.id = rl.${cfg.idColumn}
       WHERE rl.id = $1 ${roomCondition}
       LIMIT 1`,
      values
    );

    if (result.rows.length === 0) {
      throw new BusinessError(404, "Không tìm thấy dữ liệu đã gán trong phòng.", "LINE_NOT_FOUND");
    }
    return mapLineRow(result.rows[0], kind);
  }
}

module.exports = {
  ServiceManagementService,
  BusinessError,
  calculateTotalPrice,
  parseQuantity,
  parsePrice,
};
