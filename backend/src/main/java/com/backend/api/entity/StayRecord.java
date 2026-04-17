package com.backend.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "luu_tru")
@Data
public class StayRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_luutru")
    private Integer id;

    @Column(name = "ma_dat_phong")
    private Integer bookingId;

    @Column(name = "thoi_gian_checkin_thuc_te")
    private LocalDateTime actualCheckIn;

    @Column(name = "thoi_gian_checkout_thuc_te")
    private LocalDateTime actualCheckOut;

    @Column(name = "so_nguoi_thuc_te")
    private Integer actualGuestCount;
}
