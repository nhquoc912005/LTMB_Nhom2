// Module đặt phòng Android.
// File này là model UI cho một đặt phòng trong màn quản lý booking.
// Dữ liệu chính gồm mã booking, phòng, trạng thái, khách, ngày ở, giá và số lượng khách.
/*
 * File: Booking.java
 * Module: Quản lý đặt phòng.
 *
 * Model này là dữ liệu trung gian cho giao diện Android, không phải DTO nhận trực tiếp từ API.
 * Fragment sẽ map BookingDto sang Booking sau khi chuẩn hóa ngày, tiền, trạng thái và số khách.
 */
package com.project_mobile.datphong_mobile;

/**
 * Booking là dữ liệu đã chuẩn hóa để BookingAdapter hiển thị.
 * Khác BookingDto ở chỗ các field đã được format sẵn cho UI.
 */
public class Booking {
    // Mã đặt phòng dùng khi gọi API hủy hoặc xem chi tiết booking.
    private final String bookingId;
    // Tên/số phòng đã được chuẩn hóa để hiển thị trên card.
    private final String roomName;
    // Trạng thái đặt phòng đã được Fragment/Adapter dùng để quyết định badge và nút hủy.
    private String status;
    // Thông tin liên hệ khách đặt phòng.
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    // Ngày nhận/trả đã format cho UI, không dùng để gửi ngược về API.
    private final String checkInDate;
    private final String checkOutDate;
    // Tổng tiền đã format dạng chuỗi tiền tệ để hiển thị.
    private final String totalPrice;
    // Số lượng khách; totalGuests có thể được tính từ adults + children nếu API thiếu.
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
