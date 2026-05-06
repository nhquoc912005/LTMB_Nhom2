// Module dịch vụ/tài sản Android.
// File này gom các phòng đang lưu trú theo tầng để render sơ đồ phòng.
// Dữ liệu chính là tên tầng và danh sách StayRoomModel thuộc tầng đó.
package com.project_mobile.service;
import java.util.List;

/** FloorModel là nhóm dữ liệu cho một section tầng trong RoomMapFragment. */
public class FloorModel {
    private final String floorName;
    private final List<StayRoomModel> rooms;

    public FloorModel(String floorName, List<StayRoomModel> rooms) {
        this.floorName = floorName;
        this.rooms = rooms;
    }

    public String getFloorName() { return floorName; }
    public List<StayRoomModel> getRooms() { return rooms; }
}
