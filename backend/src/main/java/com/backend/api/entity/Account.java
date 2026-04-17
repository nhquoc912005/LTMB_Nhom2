package com.backend.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tai_khoan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @jakarta.persistence.Column(name = "id_taikhoan")
    private Integer id;
    
    @jakarta.persistence.Column(name = "ten_dang_nhap")
    private String username;
    
    @jakarta.persistence.Column(name = "mat_khau")
    private String password;

    @jakarta.persistence.Column(name = "ho_ten")
    private String fullName;
    
    @jakarta.persistence.Column(name = "id_vaitro")
    private Integer roleId;
}
