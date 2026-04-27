package com.project_mobile.datphong_mobile;

public class RoomBooking {
    private String roomNumber;
    private String status;
    private String customerName;
    private String email;
    private String phone;
    private String checkInDate;
    private String checkOutDate;
    private String price;
    private String totalGuests;
    private String adults;
    private String children;

    public RoomBooking(String roomNumber, String status, String customerName, String email, String phone, String checkInDate, String checkOutDate, String price, String totalGuests, String adults, String children) {
        this.roomNumber = roomNumber;
        this.status = status;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.price = price;
        this.totalGuests = totalGuests;
        this.adults = adults;
        this.children = children;
    }

    // Getters
    public String getRoomNumber() { return roomNumber; }
    public String getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public String getPrice() { return price; }
    public String getTotalGuests() { return totalGuests; }
    public String getAdults() { return adults; }
    public String getChildren() { return children; }
}
