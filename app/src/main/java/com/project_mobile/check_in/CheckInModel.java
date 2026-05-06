// Module nhận phòng Android.
// File này là model UI cho một đơn đặt phòng đang chờ nhận phòng.
// Dữ liệu chính gồm mã đặt phòng, thông tin khách, phòng, thời gian lưu trú và id phòng cũ.
/*
 * File: CheckInModel.java
 * Module: Nhận phòng.
 *
 * Model này là dữ liệu đã rút gọn từ BookingDto cho màn check-in.
 * Fragment dùng model này để hiển thị card, mở dialog nhận phòng và đổi phòng.
 */
package com.project_mobile.check_in;

/**
 * CheckInModel chứa dữ liệu đã được chuẩn hóa để CheckInAdapter hiển thị.
 * oldRoomId được dùng khi gọi API đổi phòng trước lúc nhận phòng.
 */
public class CheckInModel {
    // Mã đặt phòng dùng làm path param cho API confirm/change-room.
    private final String bookingId;
    // Thông tin khách/phòng hiển thị trên card và dialog.
    private final String guestName;
    private final String roomNumber;
    private final String phoneNumber;
    private final String email;
    private final String stayPeriod;
    private final int totalGuests;
    private final int adults;
    private final int children;
    // Id phòng hiện tại dùng khi gửi request đổi phòng.
    private final Integer oldRoomId;

    public CheckInModel(String bookingId, String guestName, String roomNumber, String phoneNumber, String email, String stayPeriod, int totalGuests, int adults, int children) {
        this(bookingId, guestName, roomNumber, phoneNumber, email, stayPeriod, totalGuests, adults, children, null);
    }

    public CheckInModel(String bookingId, String guestName, String roomNumber, String phoneNumber, String email, String stayPeriod, int totalGuests, int adults, int children, Integer oldRoomId) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.stayPeriod = stayPeriod;
        this.totalGuests = totalGuests;
        this.adults = adults;
        this.children = children;
        this.oldRoomId = oldRoomId;
    }

    public String getBookingId() { return bookingId; }
    public String getGuestName() { return guestName; }
    public String getRoomNumber() { return roomNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getStayPeriod() { return stayPeriod; }
    public int getTotalGuests() { return totalGuests; }
    public int getAdults() { return adults; }
    public int getChildren() { return children; }
    public Integer getOldRoomId() { return oldRoomId; }
}
