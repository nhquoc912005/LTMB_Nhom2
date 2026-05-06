// Module backend dịch vụ/tài sản.
// File này khai báo route danh mục dịch vụ/tài sản và các dòng phát sinh theo phòng đang lưu trú.
// Dữ liệu chính được xử lý qua ServiceManagementService.
const express = require("express");

const { requireAnyRole } = require("./role-middleware");
const { BusinessError, ServiceManagementService } = require("./service-management-service");

// Response thành công thống nhất cho các endpoint service/asset.
function ok(res, message, data, status = 200) {
  return res.status(status).json({
    success: true,
    message,
    data,
  });
}

function sendError(res, error) {
  if (error instanceof BusinessError) {
    return res.status(error.statusCode).json({
      success: false,
      message: error.message,
      error: error.error,
    });
  }

  console.error(error);
  return res.status(500).json({
    success: false,
    message: "Lỗi hệ thống, vui lòng thử lại sau.",
    error: "INTERNAL_ERROR",
  });
}

function asyncRoute(handler) {
  return async (req, res) => {
    try {
      await handler(req, res);
    } catch (error) {
      sendError(res, error);
    }
  };
}

// Hỗ trợ cả query search và q để tương thích Android cũ/mới.
function querySearch(req) {
  return req.query.search || req.query.q || "";
}

function createServiceRouter(pool) {
  const router = express.Router();
  const service = new ServiceManagementService(pool);

  const catalogManagers = requireAnyRole(["admin", "manager"]);
  const roomStaff = requireAnyRole(["admin", "manager", "staff", "employee", "receptionist"]);

  // GET /api/services
  // Chức năng: lấy danh mục dịch vụ để quản lý hoặc chọn thêm vào phòng.
  // Input query: q/search. Output: danh sách CatalogItemDto.
  // Lấy danh mục tất cả dịch vụ (Service Catalog)
  router.get("/services", asyncRoute(async (req, res) => {
    const data = await service.listCatalog("service", querySearch(req));
    ok(res, "Lấy danh sách dịch vụ thành công.", data);
  }));

  // POST /api/services
  // Chức năng: tạo mới một dịch vụ trong danh mục.
  // Input body: name, price, unit/icon nếu có. Output: CatalogItemDto mới.
  // Tạo mới một loại dịch vụ vào danh mục
  router.post("/services", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.createCatalog("service", req.body);
    ok(res, "Thêm dịch vụ thành công.", data, 201);
  }));

  // PUT /api/services/:id
  // Chức năng: cập nhật tên/giá dịch vụ.
  // Input path id, body name/price. Output: CatalogItemDto sau cập nhật.
  // Cập nhật thông tin của một loại dịch vụ trong danh mục
  router.put("/services/:id", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.updateCatalog("service", req.params.id, req.body);
    ok(res, "Cập nhật dịch vụ thành công.", data);
  }));

  // DELETE /api/services/:id
  // Chức năng: xóa dịch vụ khỏi danh mục nếu chưa phát sinh sử dụng.
  // Điều kiện nghiệp vụ nằm trong ServiceManagementService.deleteCatalog.
  // Xóa một loại dịch vụ khỏi danh mục
  router.delete("/services/:id", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.deleteCatalog("service", req.params.id);
    ok(res, "Xóa dịch vụ thành công.", data);
  }));

  // GET /api/assets
  // Chức năng: lấy danh mục tài sản/bồi thường.
  // Input query: q/search. Output: danh sách CatalogItemDto.
  // Lấy danh mục tất cả tài sản/bồi thường (Asset Catalog)
  router.get("/assets", asyncRoute(async (req, res) => {
    const data = await service.listCatalog("asset", querySearch(req));
    ok(res, "Lấy danh sách tài sản/bồi thường thành công.", data);
  }));

  // POST /api/assets
  // Chức năng: tạo mới một tài sản/bồi thường trong danh mục.
  router.post("/assets", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.createCatalog("asset", req.body);
    ok(res, "Thêm tài sản/bồi thường thành công.", data, 201);
  }));

  // PUT /api/assets/:id
  // Chức năng: cập nhật thông tin tài sản/bồi thường.
  router.put("/assets/:id", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.updateCatalog("asset", req.params.id, req.body);
    ok(res, "Cập nhật tài sản/bồi thường thành công.", data);
  }));

  // DELETE /api/assets/:id
  // Chức năng: xóa tài sản/bồi thường nếu chưa có bản ghi thiệt hại liên quan.
  router.delete("/assets/:id", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.deleteCatalog("asset", req.params.id);
    ok(res, "Xóa tài sản/bồi thường thành công.", data);
  }));

  // GET /api/rooms/occupied
  // Chức năng: lấy phòng đang có khách lưu trú để quản lý dịch vụ/tài sản phát sinh.
  // Lấy danh sách các phòng đang có khách lưu trú (Occupied Rooms)
  router.get("/rooms/occupied", asyncRoute(async (req, res) => {
    const data = await service.getOccupiedRooms(querySearch(req));
    ok(res, "Lấy danh sách phòng đang lưu trú thành công.", data);
  }));

  // GET /api/active-rooms
  // Chức năng: alias tương thích cho Android đang gọi ApiService.getActiveRooms().
  // Backward-compatible alias used by older mobile builds.
  // Alias cho app mobile cũ: Lấy danh sách phòng đang bận
  router.get("/active-rooms", asyncRoute(async (req, res) => {
    const data = await service.getOccupiedRooms(querySearch(req));
    ok(res, "Lấy danh sách phòng đang lưu trú thành công.", data);
  }));

  // GET /api/rooms/:roomId/detail
  // Chức năng: lấy chi tiết phòng và stay đang mở nếu có.
  // Lấy thông tin chi tiết của một phòng (bao gồm trạng thái lưu trú)
  router.get("/rooms/:roomId/detail", asyncRoute(async (req, res) => {
    const data = await service.getRoomDetail(req.params.roomId);
    ok(res, "Lấy chi tiết phòng thành công.", data);
  }));

  // GET /api/rooms/:roomId/services
  // Chức năng: lấy dịch vụ đang gắn với phòng.
  // Lấy danh sách các dịch vụ mà một phòng cụ thể đang sử dụng
  router.get("/rooms/:roomId/services", asyncRoute(async (req, res) => {
    const data = await service.listRoomLines("service", req.params.roomId);
    ok(res, "Lấy danh sách dịch vụ của phòng thành công.", data);
  }));

  // POST /api/rooms/:roomId/services
  // Chức năng: thêm dịch vụ vào phòng đang lưu trú.
  // Input body: catalog_id/service_id, quantity.
  // Thêm một dịch vụ mới vào phòng
  router.post("/rooms/:roomId/services", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.addRoomLine("service", req.params.roomId, req.body);
    ok(res, "Thêm dịch vụ vào phòng thành công.", data, 201);
  }));

  // PUT /api/rooms/:roomId/services/:roomServiceId
  // Chức năng: cập nhật số lượng dịch vụ trong phòng.
  router.put("/rooms/:roomId/services/:roomServiceId", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.updateRoomLine("service", req.params.roomId, req.params.roomServiceId, req.body);
    ok(res, "Cập nhật số lượng dịch vụ thành công.", data);
  }));

  // DELETE /api/rooms/:roomId/services/:roomServiceId
  // Chức năng: xóa một dòng dịch vụ khỏi phòng.
  router.delete("/rooms/:roomId/services/:roomServiceId", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.deleteRoomLine("service", req.params.roomId, req.params.roomServiceId);
    ok(res, "Xóa dịch vụ khỏi phòng thành công.", data);
  }));

  // GET /api/rooms/:roomId/assets
  // Chức năng: lấy tài sản/bồi thường đang gắn với phòng.
  router.get("/rooms/:roomId/assets", asyncRoute(async (req, res) => {
    const data = await service.listRoomLines("asset", req.params.roomId);
    ok(res, "Lấy danh sách tài sản/bồi thường của phòng thành công.", data);
  }));

  // POST /api/rooms/:roomId/assets
  // Chức năng: thêm tài sản/bồi thường vào phòng đang lưu trú.
  router.post("/rooms/:roomId/assets", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.addRoomLine("asset", req.params.roomId, req.body);
    ok(res, "Thêm tài sản/bồi thường vào phòng thành công.", data, 201);
  }));

  // PUT /api/rooms/:roomId/assets/:roomAssetId
  // Chức năng: cập nhật số lượng tài sản/bồi thường trong phòng.
  router.put("/rooms/:roomId/assets/:roomAssetId", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.updateRoomLine("asset", req.params.roomId, req.params.roomAssetId, req.body);
    ok(res, "Cập nhật số lượng tài sản/bồi thường thành công.", data);
  }));

  // DELETE /api/rooms/:roomId/assets/:roomAssetId
  // Chức năng: xóa một dòng tài sản/bồi thường khỏi phòng.
  router.delete("/rooms/:roomId/assets/:roomAssetId", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.deleteRoomLine("asset", req.params.roomId, req.params.roomAssetId);
    ok(res, "Xóa tài sản/bồi thường khỏi phòng thành công.", data);
  }));

  // Legacy aliases from the previous mobile integration.
  // Các route /room-services và /room-assets giữ tương thích với ApiService Android hiện tại.
  router.get("/rooms/:roomId/room-services", asyncRoute(async (req, res) => {
    const data = await service.listRoomLines("service", req.params.roomId);
    ok(res, "Lấy danh sách dịch vụ của phòng thành công.", data);
  }));

  router.post("/rooms/:roomId/room-services", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.addRoomLine("service", req.params.roomId, req.body);
    ok(res, "Thêm dịch vụ vào phòng thành công.", data, 201);
  }));

  // Cập nhật số lượng của một dịch vụ trong phòng
  router.put("/room-services/:id", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.updateRoomLine("service", null, req.params.id, req.body);
    ok(res, "Cập nhật số lượng dịch vụ thành công.", data);
  }));

  // Xóa dịch vụ khỏi phòng
  router.delete("/room-services/:id", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.deleteRoomLine("service", null, req.params.id);
    ok(res, "Xóa dịch vụ khỏi phòng thành công.", data);
  }));

  router.get("/rooms/:roomId/room-assets", asyncRoute(async (req, res) => {
    const data = await service.listRoomLines("asset", req.params.roomId);
    ok(res, "Lấy danh sách tài sản/bồi thường của phòng thành công.", data);
  }));

  router.post("/rooms/:roomId/room-assets", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.addRoomLine("asset", req.params.roomId, req.body);
    ok(res, "Thêm tài sản/bồi thường vào phòng thành công.", data, 201);
  }));

  router.put("/room-assets/:id", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.updateRoomLine("asset", null, req.params.id, req.body);
    ok(res, "Cập nhật số lượng tài sản/bồi thường thành công.", data);
  }));

  router.delete("/room-assets/:id", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.deleteRoomLine("asset", null, req.params.id);
    ok(res, "Xóa tài sản/bồi thường khỏi phòng thành công.", data);
  }));

  return router;
}

async function ensureServiceTables(pool) {
  const service = new ServiceManagementService(pool);
  await service.ensureSchema();
}

module.exports = {
  createServiceRouter,
  ensureServiceTables,
};
