const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function checkSpecific() {
    try {
        const res = await pool.query("SELECT ma_dat_phong, ten_nguoi_dat, id_kh FROM public.dat_phong WHERE ten_nguoi_dat LIKE '%Nguyễn Hữu Minh%'");
        console.log("Matching dat_phong:", JSON.stringify(res.rows, null, 2));
    } catch (err) {
        console.error(err);
    } finally {
        await pool.end();
    }
}

checkSpecific();
