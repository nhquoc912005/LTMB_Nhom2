package com.project_mobile.service;
import java.util.List;

public class FloorModel {
    private String floorName;
    private List<StayRoomModel> rooms;

    public FloorModel(String floorName, List<StayRoomModel> rooms) {
        this.floorName = floorName;
        this.rooms = rooms;
    }

    public String getFloorName() { return floorName; }
    public List<StayRoomModel> getRooms() { return rooms; }
}
