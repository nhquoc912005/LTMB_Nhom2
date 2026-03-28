package com.project_mobile.datphong_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnBookingActionListener {
        void onCancel(Booking booking);
    }

    private List<Booking> bookingList;
    private OnBookingActionListener listener;

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
        holder.tvBookingStatus.setText(booking.getStatus());
        holder.tvCustomerName.setText(booking.getCustomerName());
        holder.tvCheckInDate.setText(booking.getCheckInDate());
        holder.tvCheckOutDate.setText(booking.getCheckOutDate());
        holder.tvTotalPrice.setText(booking.getTotalPrice());

        // Hiện/Ẩn nút hủy dựa trên trạng thái
        holder.btnCancelBooking.setVisibility(booking.getStatus().equals("Đã hủy") ? View.GONE : View.VISIBLE);
        holder.btnCancelBooking.setOnClickListener(v -> listener.onCancel(booking));

        holder.ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.ivMenu);
            popup.inflate(R.menu.booking_item_menu); // Tạo file menu này nếu cần hoặc code dynamic
            popup.setOnMenuItemClickListener(item -> {
                // Xử lý menu 3 chấm
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return bookingList.size(); }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvBookingStatus, tvCustomerName, tvCheckInDate, tvCheckOutDate, tvTotalPrice;
        Button btnCancelBooking;
        ImageView ivMenu;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
            ivMenu = itemView.findViewById(R.id.ivMenu); // Cần thêm ID này vào item_booking_card.xml cho icon 3 chấm
        }
    }
}