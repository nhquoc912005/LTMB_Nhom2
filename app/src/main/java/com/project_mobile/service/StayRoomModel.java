package com.project_mobile.service;

import com.project_mobile.network.ApiModels.ActiveRoomDto;

public class StayRoomModel {
    private final int roomId;
    private final String roomNumber;
    private final String status;
    private final String expectedCheckIn;
    private final String expectedCheckOut;
    private final double roomFee;

    public StayRoomModel(int roomId, String roomNumber, String status, String expectedCheckIn, String expectedCheckOut, double roomFee) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.status = status;
        this.expectedCheckIn = expectedCheckIn;
        this.expectedCheckOut = expectedCheckOut;
        this.roomFee = roomFee;
    }

    public static StayRoomModel fromDto(ActiveRoomDto dto) {
        return new StayRoomModel(
                dto.roomId != null ? dto.roomId : 0,
                dto.roomNumber != null ? dto.roomNumber : "",
                dto.status != null ? dto.status : "Đang sử dụng",
                dto.checkIn,
                dto.checkOut,
                dto.roomFee != null ? dto.roomFee : 0
        );
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getExpectedCheckIn() {
        return expectedCheckIn;
    }

    public String getExpectedCheckOut() {
        return expectedCheckOut;
    }

    public double getRoomFee() {
        return roomFee;
    }
}
