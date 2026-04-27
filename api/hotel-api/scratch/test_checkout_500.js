const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

// Mock dependencies from checkout-routes.js
function parseStatusList(raw, fallback) {
  if (!raw) return fallback;
  const statuses = raw.split(",").map((item) => item.trim()).filter(Boolean);
  return statuses.length > 0 ? statuses : fallback;
}
const BOOKING_STATUS = {
  CHECKED_IN: ["Đang ở", "Đã check-in"],
};
function money(value) {
  const parsed = Number(value || 0);
  if (!Number.isFinite(parsed)) return 0;
  return Math.round(parsed);
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
    room_names: rooms.map((room) => room.ten_phong).filter(Boolean).join(", "),
  };
}

async function testQuery() {
    const client = await pool.connect();
    try {
        const values = [BOOKING_STATUS.CHECKED_IN];
        const where = [
          "dp.trang_thai = ANY($1::text[])",
          "lt.thoi_gian_checkin_thuc_te IS NOT NULL",
          "lt.thoi_gian_checkout_thuc_te IS NULL",
        ];
        const sql = `
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
            kh.ho_ten,
            kh.sdt
          ORDER BY lt.thoi_gian_checkin_thuc_te ASC, lt.id_luutru ASC
        `;
        
        console.log("Executing query...");
        const result = await pool.query(sql, values);
        console.log("Result rows:", result.rows.length);
        if (result.rows.length > 0) {
            console.log("Mapping first row...");
            const mapped = mapStayRow(result.rows[0]);
            console.log("Mapped successfully:", mapped.ma_dat_phong);
        }
    } catch (err) {
        console.error("CRASH ERROR:", err);
    } finally {
        client.release();
        await pool.end();
    }
}

testQuery();
