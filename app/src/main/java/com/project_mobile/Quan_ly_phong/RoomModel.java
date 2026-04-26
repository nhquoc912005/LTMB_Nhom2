package com.project_mobile.Quan_ly_phong;

import java.io.Serializable;

public class RoomModel implements Serializable {
    public static final String STATUS_EMPTY = "Trống";
    public static final String STATUS_STAYING = "Đang lưu trú";
    public static final String STATUS_IN_USE = "Đang sử dụng";
    public static final String STATUS_MAINTENANCE = "Bảo trì";

    private String roomNumber;
    private String roomType;
    private String floor;
    private String capacity;
    private String price;
    private String status;
    private String customerName;
    private String customerPhone;
    private String duration;

    public RoomModel(String roomNumber, String roomType, String floor, String capacity, String price, String status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.floor = floor;
        this.capacity = capacity;
        this.price = price;
        this.status = status;
    }

    public RoomModel(String roomNumber, String roomType, String floor, String capacity, String price, String status, String customerName, String customerPhone, String duration) {
        this(roomNumber, roomType, floor, capacity, price, status);
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.duration = duration;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getFloor() {
        return floor;
    }

    public String getCapacity() {
        return capacity;
    }

    public String getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getDuration() {
        return duration;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isEmpty() {
        return STATUS_EMPTY.equals(status);
    }

    public boolean isOccupied() {
        return STATUS_STAYING.equals(status) || STATUS_IN_USE.equals(status);
    }

    public boolean isMaintenance() {
        return STATUS_MAINTENANCE.equals(status);
    }

    public void clearCustomer() {
        customerName = null;
        customerPhone = null;
        duration = null;
    }

    public void copyCustomerFrom(RoomModel source) {
        customerName = source.customerName;
        customerPhone = source.customerPhone;
        duration = source.duration;
    }
}
