const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function debugQuery() {
    const BOOKING_STATUS_WAITING_CHECKIN = ["Đã đặt cọc", "Chờ check-in"];
    const values = [BOOKING_STATUS_WAITING_CHECKIN];
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
        dp.*,
        kh.ho_ten as customer_name,
        kh.sdt as customer_phone
      FROM public.dat_phong dp
      LEFT JOIN public.khach_hang kh ON kh.id_khach_hang = dp.id_khach_hang
      WHERE ${where.join(" AND ")}
      ORDER BY dp.ngay_nhan ASC
    `;

    try {
        console.log("Executing SQL:", sql);
        const res = await pool.query(sql, values);
        console.log("Result rows count:", res.rows.length);
        console.log("Data:", JSON.stringify(res.rows, null, 2));
    } catch (err) {
        console.error("SQL Error:", err.message);
        // Try alternative join
        console.log("Trying alternative join with id_kh...");
        const sqlAlt = sql.replace(/id_khach_hang/g, "id_kh");
        try {
            const resAlt = await pool.query(sqlAlt, values);
            console.log("Alt Result rows count:", resAlt.rows.length);
        } catch (err2) {
            console.error("Alt SQL Error:", err2.message);
        }
    } finally {
        await pool.end();
    }
}

debugQuery();
