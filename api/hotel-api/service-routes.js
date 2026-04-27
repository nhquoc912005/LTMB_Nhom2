const express = require("express");

const { requireAnyRole } = require("./role-middleware");
const { BusinessError, ServiceManagementService } = require("./service-management-service");

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

function querySearch(req) {
  return req.query.search || req.query.q || "";
}

function createServiceRouter(pool) {
  const router = express.Router();
  const service = new ServiceManagementService(pool);

  const catalogManagers = requireAnyRole(["admin", "manager"]);
  const roomStaff = requireAnyRole(["admin", "manager", "staff", "employee", "receptionist"]);

  // Lấy danh mục tất cả dịch vụ (Service Catalog)
  router.get("/services", asyncRoute(async (req, res) => {
    const data = await service.listCatalog("service", querySearch(req));
    ok(res, "Lấy danh sách dịch vụ thành công.", data);
  }));

  // Tạo mới một loại dịch vụ vào danh mục
  router.post("/services", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.createCatalog("service", req.body);
    ok(res, "Thêm dịch vụ thành công.", data, 201);
  }));

  // Cập nhật thông tin của một loại dịch vụ trong danh mục
  router.put("/services/:id", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.updateCatalog("service", req.params.id, req.body);
    ok(res, "Cập nhật dịch vụ thành công.", data);
  }));

  // Xoá một loại dịch vụ khỏi danh mục
  router.delete("/services/:id", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.deleteCatalog("service", req.params.id);
    ok(res, "Xoá dịch vụ thành công.", data);
  }));

  // Lấy danh mục tất cả tài sản/bồi thường (Asset Catalog)
  router.get("/assets", asyncRoute(async (req, res) => {
    const data = await service.listCatalog("asset", querySearch(req));
    ok(res, "Lấy danh sách tài sản/bồi thường thành công.", data);
  }));

  router.post("/assets", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.createCatalog("asset", req.body);
    ok(res, "Thêm tài sản/bồi thường thành công.", data, 201);
  }));

  router.put("/assets/:id", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.updateCatalog("asset", req.params.id, req.body);
    ok(res, "Cập nhật tài sản/bồi thường thành công.", data);
  }));

  router.delete("/assets/:id", catalogManagers, asyncRoute(async (req, res) => {
    const data = await service.deleteCatalog("asset", req.params.id);
    ok(res, "Xoá tài sản/bồi thường thành công.", data);
  }));

  // Lấy danh sách các phòng đang có khách lưu trú (Occupied Rooms)
  router.get("/rooms/occupied", asyncRoute(async (req, res) => {
    const data = await service.getOccupiedRooms(querySearch(req));
    ok(res, "Lấy danh sách phòng đang lưu trú thành công.", data);
  }));

  // Backward-compatible alias used by older mobile builds.
  // Alias cho app mobile cũ: Lấy danh sách phòng đang bận
  router.get("/active-rooms", asyncRoute(async (req, res) => {
    const data = await service.getOccupiedRooms(querySearch(req));
    ok(res, "Lấy danh sách phòng đang lưu trú thành công.", data);
  }));

  // Lấy thông tin chi tiết của một phòng (bao gồm trạng thái lưu trú)
  router.get("/rooms/:roomId/detail", asyncRoute(async (req, res) => {
    const data = await service.getRoomDetail(req.params.roomId);
    ok(res, "Lấy chi tiết phòng thành công.", data);
  }));

  // Lấy danh sách các dịch vụ mà một phòng cụ thể đang sử dụng
  router.get("/rooms/:roomId/services", asyncRoute(async (req, res) => {
    const data = await service.listRoomLines("service", req.params.roomId);
    ok(res, "Lấy danh sách dịch vụ của phòng thành công.", data);
  }));

  // Thêm một dịch vụ mới vào phòng
  router.post("/rooms/:roomId/services", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.addRoomLine("service", req.params.roomId, req.body);
    ok(res, "Thêm dịch vụ vào phòng thành công.", data, 201);
  }));

  router.put("/rooms/:roomId/services/:roomServiceId", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.updateRoomLine("service", req.params.roomId, req.params.roomServiceId, req.body);
    ok(res, "Cập nhật số lượng dịch vụ thành công.", data);
  }));

  router.delete("/rooms/:roomId/services/:roomServiceId", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.deleteRoomLine("service", req.params.roomId, req.params.roomServiceId);
    ok(res, "Xoá dịch vụ khỏi phòng thành công.", data);
  }));

  router.get("/rooms/:roomId/assets", asyncRoute(async (req, res) => {
    const data = await service.listRoomLines("asset", req.params.roomId);
    ok(res, "Lấy danh sách tài sản/bồi thường của phòng thành công.", data);
  }));

  router.post("/rooms/:roomId/assets", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.addRoomLine("asset", req.params.roomId, req.body);
    ok(res, "Thêm tài sản/bồi thường vào phòng thành công.", data, 201);
  }));

  router.put("/rooms/:roomId/assets/:roomAssetId", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.updateRoomLine("asset", req.params.roomId, req.params.roomAssetId, req.body);
    ok(res, "Cập nhật số lượng tài sản/bồi thường thành công.", data);
  }));

  router.delete("/rooms/:roomId/assets/:roomAssetId", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.deleteRoomLine("asset", req.params.roomId, req.params.roomAssetId);
    ok(res, "Xoá tài sản/bồi thường khỏi phòng thành công.", data);
  }));

  // Legacy aliases from the previous mobile integration.
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

  // Xoá dịch vụ khỏi phòng
  router.delete("/room-services/:id", roomStaff, asyncRoute(async (req, res) => {
    const data = await service.deleteRoomLine("service", null, req.params.id);
    ok(res, "Xoá dịch vụ khỏi phòng thành công.", data);
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
    ok(res, "Xoá tài sản/bồi thường khỏi phòng thành công.", data);
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
