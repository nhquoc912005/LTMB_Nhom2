package com.project_mobile.checkout;

import com.project_mobile.RoomModel;

public class CheckoutBill {
    private RoomModel roomModel;
    private String customerEmail;
    private String checkInDate;
    private String checkOutDate;
    private double serviceFee;
    private double totalFee;

    public CheckoutBill(RoomModel roomModel, String customerEmail, String checkInDate, String checkOutDate, double serviceFee, double totalFee) {
        this.roomModel = roomModel;
        this.customerEmail = customerEmail;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.serviceFee = serviceFee;
        this.totalFee = totalFee;
    }

    public RoomModel getRoomModel() { return roomModel; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public double getServiceFee() { return serviceFee; }
    public double getTotalFee() { return totalFee; }
}