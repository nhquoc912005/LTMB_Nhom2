package com.backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInDTO {
    private Integer bookingId;
    private String guestName;
    private String roomNumber;
    private String phone;
    private String email;
    private String stayPeriod;
}
