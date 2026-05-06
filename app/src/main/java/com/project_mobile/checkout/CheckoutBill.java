// Module trả phòng Android.
// File này là model UI cho hóa đơn tạm tính khi trả phòng.
// Dữ liệu chính gồm phòng, khách, ngày lưu trú, phí phòng, phí dịch vụ, bồi thường, cọc và số tiền phải trả/hoàn.
/*
 * File: CheckoutBill.java
 * Module: Trả phòng/checkout.
 *
 * Model UI này gom dữ liệu hóa đơn trả phòng đã nhận từ CheckoutDto.
 * Các field định danh như idHoaDon/idLuutru/maDatPhong được giữ lại để CheckoutFragment gọi API thanh toán.
 */
package com.project_mobile.checkout;

import com.project_mobile.Quan_ly_phong.RoomModel;

/**
 * CheckoutBill gom dữ liệu đã tính từ backend để hiển thị card và dialog thanh toán.
 * idHoaDon, idLuutru và maDatPhong là khóa dùng khi gọi API thanh toán.
 */
public class CheckoutBill {
    // RoomModel chứa thông tin phòng/khách đã được format để tái sử dụng với UI quản lý phòng.
    private final RoomModel roomModel;
    // Thông tin khách và khoảng thời gian lưu trú hiển thị trong dialog thanh toán.
    private final String customerEmail;
    private final String checkInDate;
    private final String checkOutDate;
    private final double serviceFee;
    private final double totalFee;
    private final int adults;
    private final int children;
    // Khóa nghiệp vụ từ backend, dùng khi tạo hóa đơn nháp và thanh toán.
    private Integer idLuutru;
    private Integer idHoaDon;
    private String maDatPhong;
    private int chargeableNights;
    // Các thành phần tiền của hóa đơn checkout.
    private double roomFee;
    private double damageFee;
    private double deposit;
    private double grossTotal;
    private double amountDue;
    private double refundAmount;

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

    public CheckoutBill(RoomModel roomModel, String customerEmail, String checkInDate, String checkOutDate, double serviceFee, double totalFee, int adults, int children, Integer idLuutru, Integer idHoaDon, String maDatPhong, int chargeableNights, double roomFee, double damageFee, double deposit, double grossTotal, double amountDue, double refundAmount) {
        this(roomModel, customerEmail, checkInDate, checkOutDate, serviceFee, totalFee, adults, children);
        this.idLuutru = idLuutru;
        this.idHoaDon = idHoaDon;
        this.maDatPhong = maDatPhong;
        this.chargeableNights = chargeableNights;
        this.roomFee = roomFee;
        this.damageFee = damageFee;
        this.deposit = deposit;
        this.grossTotal = grossTotal;
        this.amountDue = amountDue;
        this.refundAmount = refundAmount;
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
    public int getChargeableNights() { return chargeableNights; }
    public double getRoomFee() { return roomFee; }
    public double getDamageFee() { return damageFee; }
    public double getDeposit() { return deposit; }
    public double getGrossTotal() { return grossTotal; }
    public double getAmountDue() { return amountDue; }
    public double getRefundAmount() { return refundAmount; }
}
