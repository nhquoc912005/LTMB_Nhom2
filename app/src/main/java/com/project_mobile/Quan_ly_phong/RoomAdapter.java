package com.project_mobile.Quan_ly_phong;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private List<RoomModel> roomList;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(RoomModel room);
    }

    public RoomAdapter(List<RoomModel> roomList, OnRoomClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomModel room = roomList.get(position);
        holder.tvRoomNumber.setText(room.getRoomNumber());
        holder.tvRoomType.setText(room.getRoomType());
        holder.tvFloor.setText(room.getFloor());
        holder.tvPrice.setText(room.getPrice());
        holder.tvStatus.setText(room.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRoomClick(room);
        });

        int bgColor, textColor;
        switch (room.getStatus()) {
            case "Đang sử dụng":
                bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_in_use_bg);
                textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_in_use_text);
                break;
            case "Bảo trì":
                bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_maintenance_text);
                break;
            default:
                bgColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_empty_bg);
                textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_empty_text);
                break;
        }

        holder.tvStatus.setTextColor(textColor);
        GradientDrawable drawable = (GradientDrawable) holder.tvStatus.getBackground();
        if (drawable != null) {
            drawable.setColor(bgColor);
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber, tvRoomType, tvFloor, tvPrice, tvStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            tvFloor = itemView.findViewById(R.id.tvFloor);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
