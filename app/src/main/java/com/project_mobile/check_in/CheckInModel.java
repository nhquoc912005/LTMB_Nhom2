package com.project_mobile.check_in;

import com.google.gson.annotations.SerializedName;

public class CheckInModel {
    private int bookingId;
    private String guestName;
    private String roomNumber;
    
    @SerializedName("phone")
    private String phoneNumber;
    
    private String email;
    private String stayPeriod;

    public CheckInModel(String guestName, String roomNumber, String phoneNumber, String email, String stayPeriod) {
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.stayPeriod = stayPeriod;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getGuestName() { return guestName; }
    public String getRoomNumber() { return roomNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getStayPeriod() { return stayPeriod; }
}
