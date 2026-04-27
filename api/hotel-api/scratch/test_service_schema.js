const { Pool } = require('pg');
const { ServiceManagementService } = require('../service-management-service');

const pool = new Pool({
    connectionString: 'postgresql://postgres.zjmyhgbrvkdnjasxvzrc:quan%40nam3_202526@aws-1-ap-northeast-1.pooler.supabase.com:6543/postgres'
});

async function test() {
    const service = new ServiceManagementService(pool);
    try {
        console.log("Listing services...");
        const services = await service.listCatalog('service');
        console.log("Found services:", services.length);
        if (services.length > 0) {
            console.log("First service:", services[0]);
        }

        console.log("\nListing assets...");
        const assets = await service.listCatalog('asset');
        console.log("Found assets:", assets.length);
        if (assets.length > 0) {
            console.log("First asset:", assets[0]);
        }

        console.log("\nGetting occupied rooms...");
        const rooms = await service.getOccupiedRooms();
        console.log("Found occupied rooms:", rooms.length);
        if (rooms.length > 0) {
            const room = rooms[0];
            console.log("First occupied room:", room);
            if (room.stay_id) {
                console.log(`Listing services for room ${room.room_number}...`);
                const lines = await service.listRoomLines('service', room.room_id);
                console.log("Service lines:", lines.length);
            }
        }
    } catch (err) {
        console.error("TEST FAILED:", err);
    } finally {
        await pool.end();
    }
}

test();
