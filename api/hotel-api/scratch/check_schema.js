const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function checkColumns() {
    try {
        const resKH = await pool.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'khach_hang'");
        console.log("khach_hang columns:", resKH.rows.map(r => r.column_name).join(", "));
        
        const resDP = await pool.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'dat_phong'");
        console.log("dat_phong columns:", resDP.rows.map(r => r.column_name).join(", "));
    } catch (err) {
        console.error(err);
    } finally {
        await pool.end();
    }
}

checkColumns();
