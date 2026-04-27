const pool = require("./db");
const { ServiceManagementService } = require("./service-management-service");

async function init() {
  console.log("=== Đang khởi tạo Database trên Supabase ===");
  const service = new ServiceManagementService(pool);
  
  try {
    console.log("1. Đang kiểm tra kết nối...");
    await pool.query("SELECT NOW()");
    console.log("   -> Kết nối thành công!");

    console.log("2. Đang khởi tạo bảng và migration...");
    await service.ensureSchema();
    console.log("   -> Khởi tạo schema thành công!");

    console.log("3. Đang kiểm tra dữ liệu phòng...");
    const rooms = await pool.query("SELECT COUNT(*) FROM public.phong");
    console.log(`   -> Có ${rooms.rows[0].count} phòng trong hệ thống.`);

    console.log("\n=== HOÀN TẤT ===");
    console.log("Database của bạn đã sẵn sàng cho trang Dịch vụ.");
    process.exit(0);
  } catch (error) {
    console.error("\n[LỖI] Không thể khởi tạo database:");
    console.error(error.message);
    console.log("\nVui lòng kiểm tra lại SUPABASE_DB_URL trong file .env");
    process.exit(1);
  }
}

init();
