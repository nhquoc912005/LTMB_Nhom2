const express = require("express");

function mapUserRow(row) {
  const locked = row.trang_thai === "Tạm khóa" || row.trang_thai === "Khóa";
  return {
    id: row.id_taikhoan,
    username: row.ten_dang_nhap,
    fullName: row.ho_ten,
    role: row.ten_vaitro || row.chuc_vu || "Nhân viên",
    email: row.email,
    phone: row.so_dien_thoai,
    position: row.chuc_vu || row.ten_vaitro || "Nhân viên",
    id_vaitro: row.id_vaitro,
    active: !locked,
    locked,
  };
}

async function ensureAccountStatusColumn(pool) {
  await pool.query(
    "ALTER TABLE public.tai_khoan ADD COLUMN IF NOT EXISTS trang_thai character varying DEFAULT 'Hoạt động'"
  );
}

async function resolveRoleId(pool, role, idVaitro) {
  if (idVaitro) return idVaitro;
  if (!role) return null;
  const result = await pool.query(
    "SELECT id_vaitro FROM public.vai_tro WHERE ten_vaitro = $1 OR ten_vaitro ILIKE $2 LIMIT 1",
    [role, role]
  );
  return result.rows[0]?.id_vaitro || null;
}

async function getUserById(pool, id) {
  const result = await pool.query(
    `SELECT tk.*, vt.ten_vaitro
     FROM public.tai_khoan tk
     LEFT JOIN public.vai_tro vt ON tk.id_vaitro = vt.id_vaitro
     WHERE tk.id_taikhoan = $1`,
    [id]
  );
  return result.rows[0] ? mapUserRow(result.rows[0]) : null;
}

module.exports = function (pool) {
  const router = express.Router();
  const schemaReady = ensureAccountStatusColumn(pool).catch((error) => {
    console.error("Không thể bảo đảm cột trạng thái tài khoản:", error.message);
  });

  router.get("/", async (req, res) => {
    try {
      await schemaReady;
      const result = await pool.query(
        `SELECT tk.*, vt.ten_vaitro
         FROM public.tai_khoan tk
         LEFT JOIN public.vai_tro vt ON tk.id_vaitro = vt.id_vaitro
         ORDER BY tk.id_taikhoan ASC`
      );
      res.json({ success: true, data: result.rows.map(mapUserRow) });
    } catch (error) {
      console.error("Lỗi lấy danh sách nhân viên:", error);
      res.status(500).json({ success: false, message: "Lỗi lấy danh sách nhân viên" });
    }
  });

  router.post("/", async (req, res) => {
    const {
      username,
      password,
      fullName,
      email,
      phone,
      role,
      position,
      id_vaitro,
      idVaitro,
      active = true,
      locked = false,
    } = req.body;
    try {
      await schemaReady;
      const roleLabel = role || position || "Nhân viên";
      const roleId = await resolveRoleId(pool, roleLabel, id_vaitro || idVaitro);
      const status = locked || active === false ? "Tạm khóa" : "Hoạt động";
      const result = await pool.query(
        `INSERT INTO public.tai_khoan
           (ten_dang_nhap, mat_khau, ho_ten, email, so_dien_thoai, chuc_vu, id_vaitro, trang_thai)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
         RETURNING id_taikhoan`,
        [username, password, fullName, email, phone, roleLabel, roleId, status]
      );
      const user = await getUserById(pool, result.rows[0].id_taikhoan);
      res.status(201).json({ success: true, data: user });
    } catch (error) {
      console.error("Lỗi thêm nhân viên:", error);
      res.status(500).json({ success: false, message: "Lỗi thêm nhân viên" });
    }
  });

  router.put("/:id", async (req, res) => {
    const { id } = req.params;
    const { fullName, email, phone, role, position, password, id_vaitro, idVaitro, active, locked } = req.body;
    try {
      await schemaReady;
      const roleLabel = role || position;
      const roleId = await resolveRoleId(pool, roleLabel, id_vaitro || idVaitro);
      const status = locked === true || active === false ? "Tạm khóa" : locked === false || active === true ? "Hoạt động" : null;
      const result = await pool.query(
        `UPDATE public.tai_khoan
         SET ho_ten = COALESCE($1, ho_ten),
             email = COALESCE($2, email),
             so_dien_thoai = COALESCE($3, so_dien_thoai),
             chuc_vu = COALESCE($4, chuc_vu),
             mat_khau = COALESCE(NULLIF($5, ''), mat_khau),
             id_vaitro = COALESCE($6, id_vaitro),
             trang_thai = COALESCE($7, trang_thai)
         WHERE id_taikhoan = $8
         RETURNING id_taikhoan`,
        [fullName, email, phone, roleLabel, password, roleId, status, id]
      );
      if (result.rows.length === 0) {
        return res.status(404).json({ success: false, message: "Người dùng không tồn tại" });
      }
      const user = await getUserById(pool, id);
      res.json({ success: true, data: user });
    } catch (error) {
      console.error("Lỗi cập nhật nhân viên:", error);
      res.status(500).json({ success: false, message: "Lỗi cập nhật nhân viên" });
    }
  });

  router.put("/:id/lock", async (req, res) => {
    const { id } = req.params;
    const locked = req.body.locked === true || req.body.active === false;
    try {
      await schemaReady;
      const result = await pool.query(
        "UPDATE public.tai_khoan SET trang_thai = $1 WHERE id_taikhoan = $2 RETURNING id_taikhoan",
        [locked ? "Tạm khóa" : "Hoạt động", id]
      );
      if (result.rows.length === 0) {
        return res.status(404).json({ success: false, message: "Người dùng không tồn tại" });
      }
      const user = await getUserById(pool, id);
      res.json({ success: true, data: user, message: locked ? "Đã khóa tài khoản" : "Đã mở khóa tài khoản" });
    } catch (error) {
      console.error("Lỗi cập nhật trạng thái tài khoản:", error);
      res.status(500).json({ success: false, message: "Lỗi cập nhật trạng thái tài khoản" });
    }
  });

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

  router.put("/:id/change-password", async (req, res) => {
    const { id } = req.params;
    const { current_password, new_password } = req.body;
    try {
      const check = await pool.query("SELECT mat_khau FROM public.tai_khoan WHERE id_taikhoan = $1", [id]);
      if (check.rows.length === 0) {
        return res.status(404).json({ success: false, message: "Người dùng không tồn tại" });
      }

      if (check.rows[0].mat_khau !== current_password) {
        return res.status(400).json({ success: false, message: "Mật khẩu hiện tại không chính xác" });
      }

      await pool.query("UPDATE public.tai_khoan SET mat_khau = $1 WHERE id_taikhoan = $2", [new_password, id]);
      res.json({ success: true, message: "Đổi mật khẩu thành công" });
    } catch (error) {
      console.error("Lỗi đổi mật khẩu:", error);
      res.status(500).json({ success: false, message: "Lỗi hệ thống khi đổi mật khẩu" });
    }
  });

  return router;
};
