package com.project_mobile.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import com.project_mobile.model.Booking;
import com.project_mobile.model.BookingStatus;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private Context context;

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.tvRoomName.setText(booking.getRoomName());
        holder.tvCustomerName.setText(booking.getCustomerName());
        holder.tvCustomerEmail.setText(booking.getEmail());
        holder.tvCustomerPhone.setText(booking.getPhone());
        holder.tvCheckInDate.setText(booking.getCheckInDate());
        holder.tvCheckOutDate.setText(booking.getCheckOutDate());
        holder.tvTotalPrice.setText(booking.getTotalPrice());

        // Status Badge Logic
        switch (booking.getStatus()) {
            case PENDING:
                holder.tvStatusBadge.setText("CHỜ CHECK-IN");
                holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_pending_bg)));
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text));
                holder.btnCancel.setVisibility(View.VISIBLE);
                break;
            case CHECKED_IN:
                holder.tvStatusBadge.setText("ĐÃ CHECK-IN");
                holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_checked_in_bg)));
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_checked_in_text));
                holder.btnCancel.setVisibility(View.GONE);
                break;
            case CANCELLED:
                holder.tvStatusBadge.setText("ĐÃ HỦY");
                holder.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_cancelled_bg)));
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_cancelled_text));
                holder.btnCancel.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateData(List<Booking> newList) {
        this.bookings = newList;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvStatusBadge, tvCustomerName, tvCustomerEmail, tvCustomerPhone;
        TextView tvCheckInDate, tvCheckOutDate, tvTotalPrice;
        View btnCancel;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerEmail = itemView.findViewById(R.id.tvCustomerEmail);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
