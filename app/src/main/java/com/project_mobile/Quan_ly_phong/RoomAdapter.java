package com.project_mobile.Quan_ly_phong;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project_mobile.R;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private List<RoomModel> roomList;
    private final OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(RoomModel room);
    }

    public RoomAdapter(List<RoomModel> roomList, OnRoomClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    public void submitList(List<RoomModel> rooms) {
        this.roomList = rooms;
        notifyDataSetChanged();
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
            if (listener != null) {
                listener.onRoomClick(room);
            }
        });

        int bgColor;
        int textColor = Color.parseColor("#C0410D");
        if (room.isEmpty()) {
            bgColor = Color.parseColor("#F5F1EA");
            textColor = Color.parseColor("#C58959");
        } else if (room.isMaintenance()) {
            bgColor = Color.parseColor("#FFEBD2");
        } else {
            bgColor = Color.parseColor("#FFF4E8");
        }

        holder.tvStatus.setTextColor(textColor);
        GradientDrawable drawable = (GradientDrawable) holder.tvStatus.getBackground().mutate();
        drawable.setColor(bgColor);
        holder.tvStatus.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber;
        TextView tvRoomType;
        TextView tvFloor;
        TextView tvPrice;
        TextView tvStatus;

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
