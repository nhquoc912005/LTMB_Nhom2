package com.project_mobile.service;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class RoomGridAdapter extends RecyclerView.Adapter<RoomGridAdapter.RoomViewHolder> {

    private List<StayRoomModel> rooms;
    // ĐÃ THÊM: Khai báo interface
    private OnRoomClickListener listener;

    // ĐÃ THÊM: Tạo interface để truyền sự kiện ra ngoài
    public interface OnRoomClickListener {
        void onRoomClick(StayRoomModel room);
    }

    // ĐÃ SỬA: Cập nhật Constructor để nhận interface
    public RoomGridAdapter(List<StayRoomModel> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_grid, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        StayRoomModel room = rooms.get(position);
        holder.tvRoomNumber.setText(room.getRoomNumber());

        // ĐÃ THÊM: Bắt sự kiện click vào ô phòng và ném ra ngoài
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoomClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber;
        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
        }
    }
}
