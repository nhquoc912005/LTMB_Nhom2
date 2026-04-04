package com.project_mobile;

public class RecentActivityModel {
    private String roomNumber;
    private String customerName;
    private String status;
    private String time;
    private int statusColorRes;
    private int statusBgRes;

    public RecentActivityModel(String roomNumber, String customerName, String status, String time, int statusColorRes, int statusBgRes) {
        this.roomNumber = roomNumber;
        this.customerName = customerName;
        this.status = status;
        this.time = time;
        this.statusColorRes = statusColorRes;
        this.statusBgRes = statusBgRes;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getCustomerName() { return customerName; }
    public String getStatus() { return status; }
    public String getTime() { return time; }
    public int getStatusColorRes() { return statusColorRes; }
    public int getStatusBgRes() { return statusBgRes; }
}
