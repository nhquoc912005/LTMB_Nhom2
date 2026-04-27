const { Pool } = require('pg');
const pool = new Pool({
  connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function debugQuery() {
  const statuses = ["Đã đặt cọc", "Chờ check-in"];
  const values = [statuses];
  const where = [
    "dp.trang_thai = ANY($1::text[])",
    `NOT EXISTS (
        SELECT 1
        FROM public.luu_tru lt
        WHERE lt.ma_dat_phong = dp.ma_dat_phong
          AND lt.thoi_gian_checkin_thuc_te IS NOT NULL
      )`,
  ];

  // Simulating empty q
  const q = "";
  if (q) {
    // ...
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
      WHERE ${where.join(" AND ")}
      GROUP BY dp.ma_dat_phong, kh.ho_ten, kh.sdt, kh.id_kh
      ORDER BY dp.ngay_nhan ASC, dp.ma_dat_phong ASC
    `;

  try {
    const res = await pool.query(sql, values);
    console.log("Found rows:", res.rows.length);
    console.log("Results:", JSON.stringify(res.rows, null, 2));
  } catch (err) {
    console.error("SQL Error:", err.message);
  } finally {
    await pool.end();
  }
}

debugQuery();
