const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function checkLuuTru() {
    try {
        const res = await pool.query("SELECT ma_dat_phong, thoi_gian_checkin_thuc_te FROM public.luu_tru");
        console.log("luu_tru entries:", JSON.stringify(res.rows, null, 2));
    } catch (err) {
        console.error(err);
    } finally {
        await pool.end();
    }
}

checkLuuTru();
