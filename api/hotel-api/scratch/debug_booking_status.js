const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function debugBookingStatus() {
    try {
        console.log("--- Check dat_phong status for active stays ---");
        const result = await pool.query(`
            SELECT lt.id_luutru, lt.ma_dat_phong, dp.trang_thai as booking_status
            FROM public.luu_tru lt
            JOIN public.dat_phong dp ON dp.ma_dat_phong = lt.ma_dat_phong
            WHERE lt.thoi_gian_checkout_thuc_te IS NULL
        `);
        console.table(result.rows);

        const allStatuses = await pool.query(`SELECT DISTINCT trang_thai FROM public.dat_phong`);
        console.log("All booking statuses in DB:", allStatuses.rows.map(r => r.trang_thai));

    } catch (err) {
        console.error("Error:", err.message);
    } finally {
        await pool.end();
    }
}

debugBookingStatus();
