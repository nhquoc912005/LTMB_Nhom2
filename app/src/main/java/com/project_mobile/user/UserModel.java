// Module quản lý tài khoản Android.
// File này là model UI cho một tài khoản nhân viên.
// Dữ liệu chính gồm mã user, họ tên, liên hệ, vai trò, giới tính, id vai trò và trạng thái khóa.
package com.project_mobile.user;

/**
 * UserModel chứa dữ liệu đã chuẩn hóa cho UserAdapter.
 * roleId giúp form sửa tài khoản chọn lại đúng vai trò từ bảng vai_tro.
 */
public class UserModel {
    private final String userCode;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String gender;
    private Integer roleId;
    private boolean locked;

    public UserModel(String userCode, String fullName, String email, String phone, String role, boolean locked) {
        this(userCode, fullName, email, phone, role, null, null, locked);
    }

    public UserModel(String userCode, String fullName, String email, String phone, String role, String gender, Integer roleId, boolean locked) {
        this.userCode = userCode;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.gender = gender;
        this.roleId = roleId;
        this.locked = locked;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGender() {
        return gender;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /** Nhãn trạng thái hiển thị trên card người dùng. */
    public String getStatusLabel() {
        return locked ? "Tạm khóa" : "Hoạt động";
    }

    /** Nhãn nút hành động ngược với trạng thái khóa hiện tại. */
    public String getToggleLabel() {
        return locked ? "Mở khóa" : "Khóa";
    }
}
