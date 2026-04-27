package com.project_mobile.check_in;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class CheckInAdapter extends RecyclerView.Adapter<CheckInAdapter.CheckInViewHolder> {

    private final List<CheckInModel> checkInList;
    private final OnCheckInClickListener listener;

    public interface OnCheckInClickListener {
        void onCheckInClick(CheckInModel item);
        void onChangeRoomClick(CheckInModel item);
    }

    public CheckInAdapter(List<CheckInModel> checkInList, OnCheckInClickListener listener) {
        this.checkInList = checkInList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CheckInViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkin_card, parent, false);
        return new CheckInViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckInViewHolder holder, int position) {
        CheckInModel item = checkInList.get(position);
        holder.tvGuestName.setText(item.getGuestName());
        holder.tvRoomNumber.setText(item.getRoomNumber());
        holder.tvPhoneNumber.setText(item.getPhoneNumber());
        holder.tvEmail.setText(item.getEmail());
        holder.tvStayPeriod.setText(item.getStayPeriod());
        holder.tvTotalGuests.setText("Tổng số người: " + item.getTotalGuests());
        holder.tvAdults.setText("Người lớn: " + item.getAdults());
        holder.tvChildren.setText("Trẻ em: " + item.getChildren());

        holder.btnCheckIn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCheckInClick(item);
            }
        });

        holder.btnChangeRoom.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangeRoomClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkInList.size();
    }

    static class CheckInViewHolder extends RecyclerView.ViewHolder {
        TextView tvGuestName, tvRoomNumber, tvPhoneNumber, tvEmail, tvStayPeriod, tvTotalGuests, tvAdults, tvChildren;
        Button btnCheckIn, btnChangeRoom;

        public CheckInViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGuestName = itemView.findViewById(R.id.tvGuestName);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvStayPeriod = itemView.findViewById(R.id.tvStayPeriod);
            tvTotalGuests = itemView.findViewById(R.id.tvTotalGuests);
            tvAdults = itemView.findViewById(R.id.tvAdults);
            tvChildren = itemView.findViewById(R.id.tvChildren);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
            btnChangeRoom = itemView.findViewById(R.id.btnChangeRoom);
        }
    }
}
