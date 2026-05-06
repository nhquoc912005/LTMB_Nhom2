// Module phân quyền backend.
// File này cung cấp middleware kiểm tra role cho service/asset endpoints khi bật ENABLE_ROLE_AUTH.
// Dữ liệu chính là role từ req.user hoặc header x-user-role/x-role.
function roleAuthEnabled() {
  return String(process.env.ENABLE_ROLE_AUTH || "").toLowerCase() === "true";
}

// Lấy role từ nhiều nguồn để tương thích khi chưa có auth middleware chính thức.
function getRequestRole(req) {
  return (
    req.user?.role ||
    req.user?.ten_vaitro ||
    req.headers["x-user-role"] ||
    req.headers["x-role"] ||
    ""
  ).toString().trim().toLowerCase();
}

// Middleware chỉ cho phép các role trong allowedRoles khi ENABLE_ROLE_AUTH=true.
function requireAnyRole(allowedRoles) {
  const normalized = allowedRoles.map((role) => role.toLowerCase());

  return (req, res, next) => {
    // The current project has no auth middleware yet. Turn this on with
    // ENABLE_ROLE_AUTH=true when real auth starts attaching req.user/role.
    if (!roleAuthEnabled()) return next();

    const role = getRequestRole(req);
    if (!normalized.includes(role)) {
      return res.status(403).json({
        success: false,
        message: "Bạn không có quyền thực hiện thao tác này.",
        error: "FORBIDDEN",
      });
    }

    return next();
  };
}

module.exports = {
  requireAnyRole,
};
