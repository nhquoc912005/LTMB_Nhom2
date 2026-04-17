package com.project_mobile.checkout;

import com.project_mobile.Quan_ly_phong.RoomModel;

public class CheckoutBill {
    private RoomModel roomModel;
    private String customerEmail;
    private String checkInDate;
    private String checkOutDate;
    private double serviceFee;
    private double totalFee;
    private int adults;
    private int children;

    public CheckoutBill(RoomModel roomModel, String customerEmail, String checkInDate, String checkOutDate, double serviceFee, double totalFee, int adults, int children) {
        this.roomModel = roomModel;
        this.customerEmail = customerEmail;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.serviceFee = serviceFee;
        this.totalFee = totalFee;
        this.adults = adults;
        this.children = children;
    }

    public RoomModel getRoomModel() { return roomModel; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public double getServiceFee() { return serviceFee; }
    public double getTotalFee() { return totalFee; }

    public int getAdults() { return adults; }
    public int getChildren() { return children; }
    public int getTotalGuests() { return adults + children; }
}
