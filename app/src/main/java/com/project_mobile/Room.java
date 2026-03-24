package com.project_mobile;

public class Room {
    public enum Status {
        EMPTY, IN_USE, MAINTENANCE
    }

    private String roomNumber;
    private String roomType;
    private String floor;
    private String price;
    private Status status;

    public Room(String roomNumber, String roomType, String floor, String price, Status status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.floor = floor;
        this.price = price;
        this.status = status;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public String getFloor() { return floor; }
    public String getPrice() { return price; }
    public Status getStatus() { return status; }
}
