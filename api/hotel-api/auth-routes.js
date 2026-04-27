const express = require("express");
const router = express.Router();

// Bộ nhớ tạm để lưu OTP (Trong thực tế nên dùng Redis hoặc DB có TTL)
const otpStore = new Map();

module.exports = function (pool) {

  // 1. Đăng nhập
  router.post("/login", async (req, res) => {
    const { username, password } = req.body;
    if (!username || !password) {
      return res.status(400).json({ success: false, message: "Vui lòng nhập tên đăng nhập và mật khẩu" });
    }
    try {
      const result = await pool.query(
        `SELECT tk.*, vt.ten_vaitro 
         FROM public.tai_khoan tk
         LEFT JOIN public.vai_tro vt ON tk.id_vaitro = vt.id_vaitro
         WHERE tk.ten_dang_nhap = $1 OR tk.email = $1`,
        [username]
      );
      if (result.rows.length === 0) {
        return res.status(401).json({ success: false, message: "Tên đăng nhập không tồn tại" });
      }
      const user = result.rows[0];
      if (user.trang_thai === "Tạm khóa" || user.trang_thai === "Khóa") {
        return res.status(403).json({ success: false, message: "Tài khoản đã bị khóa" });
      }
      if (user.mat_khau !== password) {
        return res.status(401).json({ success: false, message: "Mật khẩu không chính xác" });
      }
      res.json({
        success: true,
        message: "Đăng nhập thành công",
        data: {
          id: user.id_taikhoan,
          username: user.ten_dang_nhap,
          fullName: user.ho_ten,
          role: user.ten_vaitro || user.chuc_vu || "Nhân viên",
          email: user.email,
          phone: user.so_dien_thoai,
          position: user.chuc_vu || user.ten_vaitro,
          id_vaitro: user.id_vaitro,
          active: true,
          locked: false,
        },
      });
    } catch (error) {
      console.error("Lỗi đăng nhập:", error);
      res.status(500).json({ success: false, message: "Lỗi hệ thống" });
    }
  });

  // 2. Quên mật khẩu - Gửi OTP
  router.post("/forgot-password", async (req, res) => {
    const { identity } = req.body; // email hoặc số điện thoại
    try {
      const result = await pool.query(
        "SELECT id_taikhoan, email, so_dien_thoai FROM public.tai_khoan WHERE email = $1 OR so_dien_thoai = $1",
        [identity]
      );
      if (result.rows.length === 0) {
        return res.status(404).json({ success: false, message: "Không tìm thấy tài khoản với thông tin này" });
      }

      const user = result.rows[0];
      const otp = Math.floor(1000 + Math.random() * 9000).toString(); // Tạo OTP 4 số

      // Lưu OTP vào bộ nhớ tạm (hết hạn sau 5 phút)
      otpStore.set(identity, { otp, userId: user.id_taikhoan, expires: Date.now() + 300000 });

      console.log(`[OTP] Mã xác nhận cho ${identity} là: ${otp}`);

      res.json({
        success: true,
        message: "Mã xác nhận đã được gửi",
        data: { identity: identity } // Trả về để client biết đang verify cho ai
      });
    } catch (error) {
      res.status(500).json({ success: false, message: "Lỗi hệ thống, vui lòng thử lại sau" });
    }
  });

  // 3. Xác thực OTP
  router.post("/verify-otp", (req, res) => {
    const { identity, otp } = req.body;
    const stored = otpStore.get(identity);

    if (!stored || stored.expires < Date.now()) {
      return res.status(400).json({ success: false, message: "Mã OTP đã hết hạn hoặc không tồn tại" });
    }

    if (stored.otp !== otp) {
      return res.status(400).json({ success: false, message: "Mã OTP không chính xác" });
    }

    res.json({ success: true, message: "Xác thực thành công" });
  });

  // 4. Đặt lại mật khẩu
  router.post("/reset-password", async (req, res) => {
    const { identity, otp, newPassword } = req.body;
    const stored = otpStore.get(identity);

    if (!stored || stored.otp !== otp) {
      return res.status(400).json({ success: false, message: "Yêu cầu không hợp lệ" });
    }

    try {
      await pool.query("UPDATE public.tai_khoan SET mat_khau = $1 WHERE id_taikhoan = $2", [newPassword, stored.userId]);
      otpStore.delete(identity); // Xóa OTP sau khi dùng xong
      res.json({ success: true, message: "Đặt lại mật khẩu thành công" });
    } catch (error) {
      res.status(500).json({ success: false, message: "Lỗi khi cập nhật mật khẩu" });
    }
  });

  return router;
};
