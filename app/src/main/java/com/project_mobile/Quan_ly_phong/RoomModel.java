// Module quản lý phòng Android.
// File này là model UI đại diện cho một phòng trong màn quản lý phòng.
// Dữ liệu chính gồm id, số phòng, loại, tầng, sức chứa, giá, trạng thái và thông tin khách nếu có.
package com.project_mobile.Quan_ly_phong;

import java.io.Serializable;

/**
 * RoomModel đóng vai trò dữ liệu hiển thị và chứa helper nhận diện trạng thái phòng.
 * Các helper isEmpty/isOccupied/isMaintenance gom cả nhãn tiếng Việt và mã tiếng Anh từ backend.
 */
public class RoomModel implements Serializable {
    public static final String STATUS_EMPTY = "Trống";
    public static final String STATUS_STAYING = "Đang lưu trú";
    public static final String STATUS_IN_USE = "Đang sử dụng";
    public static final String STATUS_MAINTENANCE = "Bảo trì";

    private final String roomNumber;
    private final String roomType;
    private final String floor;
    private final String capacity;
    private final String price;
    private String status;
    private String customerName;
    private String customerPhone;
    private String duration;
    private final int id;

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

    /** Kiểm tra phòng đang trống theo nhiều biến thể trạng thái từ DB/API. */
    public boolean isEmpty() {
        if (status == null) return false;
        return STATUS_EMPTY.equalsIgnoreCase(status) || "AVAILABLE".equalsIgnoreCase(status) || "EMPTY".equalsIgnoreCase(status);
    }

    /** Kiểm tra phòng đang có khách/đang sử dụng. */
    public boolean isOccupied() {
        if (status == null) return false;
        return STATUS_STAYING.equalsIgnoreCase(status) || STATUS_IN_USE.equalsIgnoreCase(status) 
                || "Bận".equalsIgnoreCase(status) || "OCCUPIED".equalsIgnoreCase(status) || "CHECKED_IN".equalsIgnoreCase(status);
    }

    /** Kiểm tra phòng đang bảo trì. */
    public boolean isMaintenance() {
        if (status == null) return false;
        return STATUS_MAINTENANCE.equalsIgnoreCase(status) || "MAINTENANCE".equalsIgnoreCase(status);
    }

    /** Xóa thông tin khách khi phòng được trả về trạng thái trống/bảo trì. */
    public void clearCustomer() {
        customerName = null;
        customerPhone = null;
        duration = null;
    }

    /** Copy thông tin khách từ phòng cũ sang phòng mới khi đổi phòng. */
    public void copyCustomerFrom(RoomModel source) {
        customerName = source.customerName;
        customerPhone = source.customerPhone;
        duration = source.duration;
    }
}
