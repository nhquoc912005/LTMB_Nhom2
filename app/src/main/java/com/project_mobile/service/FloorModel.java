package com.project_mobile.service;
import java.util.List;

public class FloorModel {
    private String floorName;
    private List<String> roomNumbers;

    public FloorModel(String floorName, List<String> roomNumbers) {
        this.floorName = floorName;
        this.roomNumbers = roomNumbers;
    }

    public String getFloorName() { return floorName; }
    public List<String> getRoomNumbers() { return roomNumbers; }
}