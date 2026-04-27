package com.project_mobile.model;

public class Booking {
    private final String id;
    private final String roomName;
    private final String customerName;
    private final String email;
    private final String phone;
    private final String checkInDate;
    private final String checkOutDate;
    private final String totalPrice;
    private final BookingStatus status;

    public Booking(String id, String roomName, String customerName, String email, String phone, String checkInDate, String checkOutDate, String totalPrice, BookingStatus status) {
        this.id = id;
        this.roomName = roomName;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getId() { return id; }
    public String getRoomName() { return roomName; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public String getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }
}
