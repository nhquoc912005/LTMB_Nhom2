const express = require("express");
const router = express.Router();

/**
 * API đăng nhập người dùng
 * Endpoint: POST /api/auth/login
 */
module.exports = function (pool) {
  router.post("/login", async (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập tên đăng nhập và mật khẩu",
      });
    }

    try {
      // Tìm tài khoản trong bảng tai_khoan (và lấy thông tin vai trò nếu cần)
      // Sử dụng LOWER để tránh lỗi phân biệt hoa thường khi nhập username/email
      const result = await pool.query(
        `SELECT tk.*, vt.ten_vaitro 
         FROM public.tai_khoan tk
         LEFT JOIN public.vai_tro vt ON tk.id_vaitro = vt.id_vaitro
         WHERE tk.ten_dang_nhap = $1 OR tk.email = $1`,
        [username]
      );

      if (result.rows.length === 0) {
        return res.status(401).json({
          success: false,
          message: "Tên đăng nhập không tồn tại",
        });
      }

      const user = result.rows[0];

      // Kiểm tra mật khẩu (Lưu ý: Trong thực tế nên dùng bcrypt để hash mật khẩu)
      if (user.mat_khau !== password) {
        return res.status(401).json({
          success: false,
          message: "Mật khẩu không chính xác",
        });
      }

      // Đăng nhập thành công
      res.json({
        success: true,
        message: "Đăng nhập thành công",
        data: {
          id: user.id_taikhoan,
          username: user.ten_dang_nhap,
          fullName: user.ho_ten,
          role: user.ten_vaitro || "Nhân viên",
          email: user.email,
        },
      });
    } catch (error) {
      console.error("Lỗi đăng nhập:", error);
      res.status(500).json({
        success: false,
        message: "Lỗi hệ thống khi đăng nhập",
      });
    }
  });

  return router;
};
