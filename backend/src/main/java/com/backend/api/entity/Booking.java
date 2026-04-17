package com.backend.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "dat_phong")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_dat_phong")
    private Integer id;

    @Column(name = "ngay_nhan")
    private LocalDateTime checkInDate;

    @Column(name = "ngay_tra")
    private LocalDateTime checkOutDate;

    @Column(name = "so_phong")
    private String roomNumber;

    @Column(name = "trang_thai")
    private String status;

    @Column(name = "ten_nguoi_dat")
    private String guestName;

    @Column(name = "sdt_nguoi_dat")
    private String phone;

    @Column(name = "email")
    private String email;
}
