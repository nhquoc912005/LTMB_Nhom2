const { Pool } = require('pg');
const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function debugCheckout() {
    try {
        console.log("--- 1. Check entries in luu_tru that haven't checked out ---");
        const activeStays = await pool.query(`
            SELECT lt.id_luutru, lt.ma_dat_phong, lt.thoi_gian_checkin_thuc_te, lt.thoi_gian_checkout_thuc_te
            FROM public.luu_tru lt
            WHERE lt.thoi_gian_checkout_thuc_te IS NULL
        `);
        console.log("Active stays count:", activeStays.rows.length);
        console.log("Active stays:", activeStays.rows);

        if (activeStays.rows.length > 0) {
            console.log("\n--- 2. Check room statuses for these stays ---");
            const maDatPhongs = activeStays.rows.map(r => r.ma_dat_phong);
            const rooms = await pool.query(`
                SELECT ctdp.ma_dat_phong, p.id_phong, p.ten_phong, p.trang_thai
                FROM public.chi_tiet_dat_phong ctdp
                JOIN public.phong p ON p.id_phong = ctdp.id_phong
                WHERE ctdp.ma_dat_phong = ANY($1)
            `, [maDatPhongs]);
            console.log("Rooms for active stays:");
            console.table(rooms.rows);

            console.log("\n--- 3. Check what statuses exist in phong table ---");
            const statuses = await pool.query(`SELECT DISTINCT trang_thai FROM public.phong`);
            console.log("Distinct room statuses:", statuses.rows.map(r => r.trang_thai));
        }

    } catch (err) {
        console.error("Error:", err.message);
    } finally {
        await pool.end();
    }
}

debugCheckout();
