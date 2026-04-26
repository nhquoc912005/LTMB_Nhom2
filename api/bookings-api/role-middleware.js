function roleAuthEnabled() {
  return String(process.env.ENABLE_ROLE_AUTH || "").toLowerCase() === "true";
}

function getRequestRole(req) {
  return (
    req.user?.role ||
    req.user?.ten_vaitro ||
    req.headers["x-user-role"] ||
    req.headers["x-role"] ||
    ""
  ).toString().trim().toLowerCase();
}

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
