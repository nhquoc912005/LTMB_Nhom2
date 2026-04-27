package com.project_mobile.datphong_mobile;

public class Booking {
    private final String bookingId;
    private final String roomName;
    private String status;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    private final String checkInDate;
    private final String checkOutDate;
    private final String totalPrice;
    private final Integer totalGuests;
    private final Integer adults;
    private final Integer children;

    public Booking(String bookingId, String roomName, String status, String customerName, String customerEmail, String customerPhone, String checkInDate, String checkOutDate, String totalPrice, Integer totalGuests, Integer adults, Integer children) {
        this.bookingId = bookingId;
        this.roomName = roomName;
        this.status = status;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.totalGuests = totalGuests;
        this.adults = adults;
        this.children = children;
    }

    public String getBookingId() { return bookingId; }
    public String getRoomName() { return roomName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public String getTotalPrice() { return totalPrice; }
    public Integer getTotalGuests() { return totalGuests; }
    public Integer getAdults() { return adults; }
    public Integer getChildren() { return children; }
}
