package com.project_mobile.user;

public class UserModel {
    private final String userCode;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean locked;

    public UserModel(String userCode, String fullName, String email, String phone, String role, boolean locked) {
        this.userCode = userCode;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getStatusLabel() {
        return locked ? "Tạm khóa" : "Hoạt động";
    }

    public String getToggleLabel() {
        return locked ? "Mở khóa" : "Khóa";
    }
}
