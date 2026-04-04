package com.project_mobile.datphong_mobile;

public class CheckedInBooking {
    private String roomName;
    private String status;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String checkInDate;
    private String checkOutDate;
    private String totalPrice;

    public CheckedInBooking(String roomName, String status, String customerName, String customerEmail, String customerPhone, String checkInDate, String checkOutDate, String totalPrice) {
        this.roomName = roomName;
        this.status = status;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
    }

    public String getRoomName() { return roomName; }
    public String getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public String getTotalPrice() { return totalPrice; }
}
