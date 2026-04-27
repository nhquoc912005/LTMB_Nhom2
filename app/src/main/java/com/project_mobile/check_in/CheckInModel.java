package com.project_mobile.check_in;

public class CheckInModel {
    private final String bookingId;
    private final String guestName;
    private final String roomNumber;
    private final String phoneNumber;
    private final String email;
    private final String stayPeriod;
    private final int totalGuests;
    private final int adults;
    private final int children;
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
