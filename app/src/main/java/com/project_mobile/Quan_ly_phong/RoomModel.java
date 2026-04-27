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
    private int id;

    public RoomModel(int id, String roomNumber, String roomType, String floor, String capacity, String price, String status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.floor = floor;
        this.capacity = capacity;
        this.price = price;
        this.status = status;
    }

    public RoomModel(int id, String roomNumber, String roomType, String floor, String capacity, String price, String status, String customerName, String customerPhone, String duration) {
        this(id, roomNumber, roomType, floor, capacity, price, status);
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.duration = duration;
    }

    public RoomModel(String roomNumber, String roomType, String floor, String capacity, String price, String status) {
        this(0, roomNumber, roomType, floor, capacity, price, status);
    }

    public RoomModel(String roomNumber, String roomType, String floor, String capacity, String price, String status, String customerName, String customerPhone, String duration) {
        this(0, roomNumber, roomType, floor, capacity, price, status, customerName, customerPhone, duration);
    }

    public int getId() {
        return id;
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
        if (status == null) return false;
        return STATUS_EMPTY.equalsIgnoreCase(status) || "AVAILABLE".equalsIgnoreCase(status) || "EMPTY".equalsIgnoreCase(status);
    }

    public boolean isOccupied() {
        if (status == null) return false;
        return STATUS_STAYING.equalsIgnoreCase(status) || STATUS_IN_USE.equalsIgnoreCase(status) 
                || "Bận".equalsIgnoreCase(status) || "OCCUPIED".equalsIgnoreCase(status) || "CHECKED_IN".equalsIgnoreCase(status);
    }

    public boolean isMaintenance() {
        if (status == null) return false;
        return STATUS_MAINTENANCE.equalsIgnoreCase(status) || "MAINTENANCE".equalsIgnoreCase(status);
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
