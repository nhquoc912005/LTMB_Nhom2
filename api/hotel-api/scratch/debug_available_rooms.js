const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function debugAvailableRooms() {
    const maDatPhong = "DPW104260423";
    const statuses = ["Trống"];
    const values = [statuses, maDatPhong];
    
    const sql = `
      SELECT
        p.id_phong,
        p.ten_phong,
        p.trang_thai
      FROM public.phong p
      WHERE p.trang_thai = ANY($1::text[])
        AND NOT EXISTS (
          SELECT 1
          FROM public.chi_tiet_dat_phong ctdp_current
          WHERE ctdp_current.ma_dat_phong = $2
            AND ctdp_current.id_phong = p.id_phong
        )
      ORDER BY p.ten_phong ASC
    `;

    try {
        const res = await pool.query(sql, values);
        console.log("Available rooms count:", res.rows.length);
        console.log("Rooms:", JSON.stringify(res.rows, null, 2));
    } catch (err) {
        console.error(err);
    } finally {
        await pool.end();
    }
}

debugAvailableRooms();
