package com.project_mobile.service;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorViewHolder> {

    private List<FloorModel> floorList;
    // ĐÃ THÊM: Gọi Interface từ file RoomGridAdapter
    private RoomGridAdapter.OnRoomClickListener roomClickListener;

    // ĐÃ SỬA: Constructor nhận sự kiện
    public FloorAdapter(List<FloorModel> floorList, RoomGridAdapter.OnRoomClickListener listener) {
        this.floorList = floorList;
        this.roomClickListener = listener;
    }

    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_floor_map, parent, false);
        return new FloorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {
        FloorModel floor = floorList.get(position);
        holder.tvFloorName.setText(floor.getFloorName());

        // ĐÃ SỬA: Truyền sự kiện click xuống cho danh sách các phòng
        RoomGridAdapter roomAdapter = new RoomGridAdapter(floor.getRooms(), roomClickListener);

        holder.rcvRooms.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 3));
        holder.rcvRooms.setAdapter(roomAdapter);
        holder.rcvRooms.setNestedScrollingEnabled(false);
    }

    @Override
    public int getItemCount() {
        return floorList.size();
    }

    public static class FloorViewHolder extends RecyclerView.ViewHolder {
        TextView tvFloorName;
        RecyclerView rcvRooms;

        public FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFloorName = itemView.findViewById(R.id.tvFloorName);
            rcvRooms = itemView.findViewById(R.id.rcvRoomsInFloor);
        }
    }
}
