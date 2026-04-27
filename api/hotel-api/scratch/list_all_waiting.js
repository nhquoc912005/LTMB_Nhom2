const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function listAll() {
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

    const sql = `
      SELECT
        dp.ma_dat_phong,
        COALESCE(kh.ho_ten, dp.ten_nguoi_dat) AS customer_name,
        dp.trang_thai
      FROM public.dat_phong dp
      LEFT JOIN public.khach_hang kh ON kh.id_kh = dp.id_kh
      WHERE ${where.join(" AND ")}
    `;

    try {
        const res = await pool.query(sql, values);
        console.log("Results:", JSON.stringify(res.rows, null, 2));
    } catch (err) {
        console.error(err);
    } finally {
        await pool.end();
    }
}

listAll();
