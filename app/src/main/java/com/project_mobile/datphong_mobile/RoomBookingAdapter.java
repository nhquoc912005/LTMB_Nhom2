package com.project_mobile.datphong_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class RoomBookingAdapter extends RecyclerView.Adapter<RoomBookingAdapter.ViewHolder> {

    private List<RoomBooking> bookings;

    public RoomBookingAdapter(List<RoomBooking> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_booking_v2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomBooking booking = bookings.get(position);
        holder.tvRoomNumber.setText(booking.getRoomNumber());
        holder.tvStatus.setText(booking.getStatus());
        holder.tvName.setText(booking.getCustomerName());
        holder.tvEmail.setText(booking.getEmail());
        holder.tvPhone.setText(booking.getPhone());
        holder.tvCheckIn.setText(booking.getCheckInDate());
        holder.tvCheckOut.setText(booking.getCheckOutDate());
        holder.tvPrice.setText(booking.getPrice());
        holder.tvTotalGuests.setText(booking.getTotalGuests());
        holder.tvAdults.setText(booking.getAdults());
        holder.tvChildren.setText(booking.getChildren());
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber, tvStatus, tvName, tvEmail, tvPhone, tvCheckIn, tvCheckOut, tvPrice;
        TextView tvTotalGuests, tvAdults, tvChildren;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumberV2);
            tvStatus = itemView.findViewById(R.id.tvStatusBadgeV2);
            tvName = itemView.findViewById(R.id.tvCustomerNameV2);
            tvEmail = itemView.findViewById(R.id.tvCustomerEmailV2);
            tvPhone = itemView.findViewById(R.id.tvCustomerPhoneV2);
            tvCheckIn = itemView.findViewById(R.id.tvCheckInV2);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOutV2);
            tvPrice = itemView.findViewById(R.id.tvPriceV2);
            tvTotalGuests = itemView.findViewById(R.id.tvTotalGuestsV2);
            tvAdults = itemView.findViewById(R.id.tvAdultsV2);
            tvChildren = itemView.findViewById(R.id.tvChildrenV2);
        }
    }
}
