package com.project_mobile.Quan_ly_phong.datphong_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class CheckedInBookingAdapter extends RecyclerView.Adapter<CheckedInBookingAdapter.ViewHolder> {

    private List<CheckedInBooking> bookings;

    public CheckedInBookingAdapter(List<CheckedInBooking> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checked_in_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CheckedInBooking booking = bookings.get(position);
        holder.tvRoomName.setText(booking.getRoomName());
        holder.tvStatus.setText(booking.getStatus());
        holder.tvName.setText(booking.getCustomerName());
        holder.tvEmail.setText(booking.getCustomerEmail());
        holder.tvPhone.setText(booking.getCustomerPhone());
        holder.tvCheckIn.setText(booking.getCheckInDate());
        holder.tvCheckOut.setText(booking.getCheckOutDate());
        holder.tvPrice.setText(booking.getTotalPrice());
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvStatus, tvName, tvEmail, tvPhone, tvCheckIn, tvCheckOut, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomNameC3);
            tvStatus = itemView.findViewById(R.id.tvStatusBadgeC3);
            tvName = itemView.findViewById(R.id.tvCustomerNameC3);
            tvEmail = itemView.findViewById(R.id.tvCustomerEmailC3);
            tvPhone = itemView.findViewById(R.id.tvCustomerPhoneC3);
            tvCheckIn = itemView.findViewById(R.id.tvCheckInC3);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOutC3);
            tvPrice = itemView.findViewById(R.id.tvPriceC3);
        }
    }
}
