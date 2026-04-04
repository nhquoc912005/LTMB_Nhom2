package com.project_mobile.check_in;

public class CheckInModel {
    private String guestName;
    private String roomNumber;
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

    public String getGuestName() { return guestName; }
    public String getRoomNumber() { return roomNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getStayPeriod() { return stayPeriod; }
}
