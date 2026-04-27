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
    private Integer idLuutru;
    private Integer idHoaDon;
    private String maDatPhong;
    private double roomFee;
    private double damageFee;
    private double deposit;
    private double grossTotal;

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

    public CheckoutBill(RoomModel roomModel, String customerEmail, String checkInDate, String checkOutDate, double serviceFee, double totalFee, int adults, int children, Integer idLuutru, Integer idHoaDon, String maDatPhong, double roomFee, double damageFee, double deposit, double grossTotal) {
        this(roomModel, customerEmail, checkInDate, checkOutDate, serviceFee, totalFee, adults, children);
        this.idLuutru = idLuutru;
        this.idHoaDon = idHoaDon;
        this.maDatPhong = maDatPhong;
        this.roomFee = roomFee;
        this.damageFee = damageFee;
        this.deposit = deposit;
        this.grossTotal = grossTotal;
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
    public Integer getIdLuutru() { return idLuutru; }
    public Integer getIdHoaDon() { return idHoaDon; }
    public void setIdHoaDon(Integer idHoaDon) { this.idHoaDon = idHoaDon; }
    public String getMaDatPhong() { return maDatPhong; }
    public double getRoomFee() { return roomFee; }
    public double getDamageFee() { return damageFee; }
    public double getDeposit() { return deposit; }
    public double getGrossTotal() { return grossTotal; }
}
