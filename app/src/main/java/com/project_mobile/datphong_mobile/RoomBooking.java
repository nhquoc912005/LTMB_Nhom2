package com.project_mobile.datphong_mobile;

public class RoomBooking {
    private final String roomNumber;
    private final String status;
    private final String customerName;
    private final String email;
    private final String phone;
    private final String checkInDate;
    private final String checkOutDate;
    private final String price;
    private final String totalGuests;
    private final String adults;
    private final String children;

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
