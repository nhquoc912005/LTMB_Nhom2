const express = require("express");
const router = express.Router();

/**
 * API quản lý nhân viên (tài khoản)
 * Các endpoint: GET /api/users, POST /api/users, PUT /api/users/:id, DELETE /api/users/:id
 */
module.exports = function (pool) {
  // Lấy danh sách nhân viên
  router.get("/", async (req, res) => {
    try {
      const result = await pool.query(
        `SELECT tk.*, vt.ten_vaitro 
         FROM public.tai_khoan tk
         LEFT JOIN public.vai_tro vt ON tk.id_vaitro = vt.id_vaitro
         ORDER BY tk.id_taikhoan ASC`
      );
      res.json({
        success: true,
        data: result.rows.map(row => ({
          id: row.id_taikhoan,
          username: row.ten_dang_nhap,
          fullName: row.ho_ten,
          role: row.ten_vaitro || "Nhân viên",
          email: row.email,
          phone: row.so_dien_thoai,
          position: row.chuc_vu
        }))
      });
    } catch (error) {
      console.error("Lỗi lấy danh sách nhân viên:", error);
      res.status(500).json({ success: false, message: "Lỗi lấy danh sách nhân viên" });
    }
  });

  // Thêm nhân viên mới
  router.post("/", async (req, res) => {
    const { username, password, fullName, email, phone, position } = req.body;
    try {
      const result = await pool.query(
        `INSERT INTO public.tai_khoan (ten_dang_nhap, mat_khau, ho_ten, email, so_dien_thoai, chuc_vu)
         VALUES ($1, $2, $3, $4, $5, $6)
         RETURNING *`,
        [username, password, fullName, email, phone, position]
      );
      res.status(201).json({ success: true, data: result.rows[0] });
    } catch (error) {
      console.error("Lỗi thêm nhân viên:", error);
      res.status(500).json({ success: false, message: "Lỗi thêm nhân viên" });
    }
  });

  // Cập nhật thông tin nhân viên
  router.put("/:id", async (req, res) => {
    const { id } = req.params;
    const { fullName, email, phone, position, password } = req.body;
    try {
      const result = await pool.query(
        `UPDATE public.tai_khoan 
         SET ho_ten = $1, email = $2, so_dien_thoai = $3, chuc_vu = $4, mat_khau = COALESCE($5, mat_khau)
         WHERE id_taikhoan = $6
         RETURNING *`,
        [fullName, email, phone, position, password, id]
      );
      res.json({ success: true, data: result.rows[0] });
    } catch (error) {
      console.error("Lỗi cập nhật nhân viên:", error);
      res.status(500).json({ success: false, message: "Lỗi cập nhật nhân viên" });
    }
  });

  // Xoá nhân viên
  router.delete("/:id", async (req, res) => {
    const { id } = req.params;
    try {
      await pool.query("DELETE FROM public.tai_khoan WHERE id_taikhoan = $1", [id]);
      res.json({ success: true, message: "Đã xoá nhân viên" });
    } catch (error) {
      console.error("Lỗi xoá nhân viên:", error);
      res.status(500).json({ success: false, message: "Lỗi xoá nhân viên" });
    }
  });

  // Đổi mật khẩu
  router.put("/:id/change-password", async (req, res) => {
    const { id } = req.params;
    const { current_password, new_password } = req.body;
    try {
      // Kiểm tra mật khẩu cũ
      const check = await pool.query("SELECT mat_khau FROM public.tai_khoan WHERE id_taikhoan = $1", [id]);
      if (check.rows.length === 0) return res.status(404).json({ success: false, message: "Người dùng không tồn tại" });
      
      if (check.rows[0].mat_khau !== current_password) {
        return res.status(400).json({ success: false, message: "Mật khẩu hiện tại không chính xác" });
      }

      // Cập nhật mật khẩu mới
      await pool.query("UPDATE public.tai_khoan SET mat_khau = $1 WHERE id_taikhoan = $2", [new_password, id]);
      res.json({ success: true, message: "Đổi mật khẩu thành công" });
    } catch (error) {
      console.error("Lỗi đổi mật khẩu:", error);
      res.status(500).json({ success: false, message: "Lỗi hệ thống khi đổi mật khẩu" });
    }
  });

  return router;
};
