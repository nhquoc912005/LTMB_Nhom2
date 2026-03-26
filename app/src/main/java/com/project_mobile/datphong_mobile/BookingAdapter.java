package com.project_mobile.datphong_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_card, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.tvRoomName.setText(booking.getRoomName());
        holder.tvBookingStatus.setText(booking.getStatus());
        holder.tvCustomerName.setText(booking.getCustomerName());
        holder.tvCustomerEmail.setText(booking.getCustomerEmail());
        holder.tvCustomerPhone.setText(booking.getCustomerPhone());
        holder.tvCheckInDate.setText(booking.getCheckInDate());
        holder.tvCheckOutDate.setText(booking.getCheckOutDate());
        holder.tvTotalPrice.setText(booking.getTotalPrice());
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvBookingStatus, tvCustomerName, tvCustomerEmail, tvCustomerPhone, tvCheckInDate, tvCheckOutDate, tvTotalPrice;
        Button btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerEmail = itemView.findViewById(R.id.tvCustomerEmail);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }
    }
}
