package com.project_mobile.datphong_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnBookingActionListener {
        void onCancel(Booking booking);
    }

    private final List<Booking> bookingList;
    private final OnBookingActionListener listener;

    public BookingAdapter(List<Booking> bookingList, OnBookingActionListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
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
        holder.tvCustomerName.setText(booking.getCustomerName());
        holder.tvCustomerEmail.setText(displayEmail(booking.getCustomerEmail()));
        holder.tvCustomerPhone.setText(booking.getCustomerPhone());
        holder.tvCheckInDate.setText(booking.getCheckInDate());
        holder.tvCheckOutDate.setText(booking.getCheckOutDate());
        holder.tvTotalPrice.setText(booking.getTotalPrice());
        holder.tvTotalGuests.setText(String.valueOf(booking.getTotalGuests()));
        holder.tvAdults.setText(String.valueOf(booking.getAdults()));
        holder.tvChildren.setText(String.valueOf(booking.getChildren()));

        // Thiết lập trạng thái hiển thị
        String status = booking.getStatus() != null ? booking.getStatus() : "";
        String displayStatus = status;
        
        if (isCancellableStatus(status)) {
            displayStatus = "Chờ nhận phòng";
            holder.tvBookingStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvBookingStatus.setTextColor(0xFF8B6D5A);
        } else if (isCheckedOutStatus(status)) {
            displayStatus = "Đã trả phòng";
            holder.tvBookingStatus.setBackgroundResource(R.drawable.bg_status_checked_out);
            holder.tvBookingStatus.setTextColor(0xFF4B5563);
        } else if (isCancelledStatus(status)) {
            displayStatus = "Đã hủy";
            holder.tvBookingStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
            holder.tvBookingStatus.setTextColor(0xFFC62828);
        } else if (isCheckedInStatus(status)) {
            displayStatus = "Đã nhận phòng";
            holder.tvBookingStatus.setBackgroundResource(R.drawable.bg_status_checked_in);
            holder.tvBookingStatus.setTextColor(0xFF2E7D32);
        } else {
            holder.tvBookingStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvBookingStatus.setTextColor(0xFF8B6D5A);
        }
        holder.tvBookingStatus.setText(displayStatus);

        // Hiện/Ẩn nút hủy dựa trên trạng thái
        holder.btnCancelBooking.setVisibility(isCancellableStatus(status) ? View.VISIBLE : View.GONE);
        holder.btnCancelBooking.setOnClickListener(v -> listener.onCancel(booking));

        holder.ivMenu.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() { return bookingList.size(); }

    private static boolean isCancellableStatus(String status) {
        return status.contains("Đã đặt cọc") || status.contains("Chờ check-in") || status.contains("Chờ nhận phòng");
    }

    private static boolean isCheckedOutStatus(String status) {
        return status.contains("Đã trả") || status.contains("Đã check-out") || status.contains("Đã thanh toán");
    }

    private static boolean isCancelledStatus(String status) {
        return status.contains("Đã hủy") || status.contains("Hủy");
    }

    private static boolean isCheckedInStatus(String status) {
        return status.contains("Đang ở") || status.contains("Đã check-in") || status.contains("Đã nhận phòng");
    }

    private static String displayEmail(String email) {
        return email == null || email.trim().isEmpty() || "-".equals(email.trim()) ? "Chưa có email" : email;
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvBookingStatus, tvCustomerName, tvCustomerEmail, tvCustomerPhone, tvCheckInDate, tvCheckOutDate, tvTotalPrice;
        TextView tvTotalGuests, tvAdults, tvChildren;
        Button btnCancelBooking;
        ImageView ivMenu;

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
            tvTotalGuests = itemView.findViewById(R.id.tvTotalGuests);
            tvAdults = itemView.findViewById(R.id.tvAdults);
            tvChildren = itemView.findViewById(R.id.tvChildren);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
            ivMenu = itemView.findViewById(R.id.ivMenu);
        }
    }
}
