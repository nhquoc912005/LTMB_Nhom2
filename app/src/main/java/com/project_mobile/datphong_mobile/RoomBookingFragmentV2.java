package com.project_mobile.datphong_mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import com.project_mobile.network.ApiClient;
import com.project_mobile.network.ApiModels.ApiResponse;
import com.project_mobile.network.ApiModels.BookingDto;
import com.project_mobile.network.ApiService;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomBookingFragmentV2 extends Fragment {

    private RecyclerView rvRoomBookings;
    private RoomBookingAdapter adapter;
    private List<RoomBooking> bookingList;
    private TextView tvTotalBookings, tvWaitingCheckin, tvCheckedIn, tvCancelled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_booking_v2, container, false);

        rvRoomBookings = view.findViewById(R.id.rvRoomBookingsV2);
        rvRoomBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        tvTotalBookings = view.findViewById(R.id.tvTotalBookings);
        tvWaitingCheckin = view.findViewById(R.id.tvWaitingCheckin);
        tvCheckedIn = view.findViewById(R.id.tvCheckedIn);
        tvCancelled = view.findViewById(R.id.tvCancelled);

        bookingList = new ArrayList<>();
        adapter = new RoomBookingAdapter(bookingList);
        rvRoomBookings.setAdapter(adapter);

        loadBookings();
        setHeaderDate(view);

        return view;
    }

    private void updateStats(List<BookingDto> data) {
        int total = data.size();
        int waiting = 0;
        int checkedIn = 0;
        int cancelled = 0;
        for (BookingDto b : data) {
            String s = b.status != null ? b.status : "";
            if (s.contains("Đã đặt cọc") || s.contains("Chờ check-in")) waiting++;
            else if (s.contains("Đang ở") || s.contains("Đã check-in") || s.contains("nhận phòng")) checkedIn++;
            else if (s.contains("Đã hủy") || s.contains("Hủy")) cancelled++;
        }
        if (tvTotalBookings != null) tvTotalBookings.setText(String.format(Locale.US, "%02d", total));
        if (tvWaitingCheckin != null) tvWaitingCheckin.setText(String.format(Locale.US, "%02d", waiting));
        if (tvCheckedIn != null) tvCheckedIn.setText(String.format(Locale.US, "%02d", checkedIn));
        if (tvCancelled != null) tvCancelled.setText(String.format(Locale.US, "%02d", cancelled));
    }

    private void setHeaderDate(View view) {
        TextView tvDate = view.findViewById(R.id.tvBookingCurrentDate);
        if (tvDate == null) return;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM, yyyy", new Locale("vi", "VN"));
        String date = sdf.format(new Date());
        if (date.length() > 0) {
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
        }
        tvDate.setText(date);
    }

    private void loadBookings() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getBookings().enqueue(new Callback<ApiResponse<List<BookingDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDto>>> call, Response<ApiResponse<List<BookingDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<BookingDto> data = response.body().data;
                    updateStats(data);
                    bookingList.clear();
                    for (BookingDto b : data) {
                        String roomNum = b.roomNumber;
                        if (roomNum == null || roomNum.isEmpty()) roomNum = "N/A";
                        if (!roomNum.startsWith("Phòng")) roomNum = "Phòng " + roomNum;

                        bookingList.add(new RoomBooking(
                                roomNum,
                                b.status != null ? b.status : "Chờ check-in",
                                b.customerName != null ? b.customerName : "Khách vãng lai",
                                b.email != null ? b.email : "-",
                                b.phone != null ? b.phone : "-",
                                formatDate(b.checkIn),
                                formatDate(b.checkOut),
                                formatCurrency(b.totalAmount != null ? b.totalAmount : 0),
                                String.valueOf(b.totalGuests != null ? b.totalGuests : 0),
                                String.valueOf(b.adults != null ? b.adults : 0),
                                String.valueOf(b.children != null ? b.children : 0)
                        ));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingDto>>> call, Throwable t) {
                // Handle error
            }
        });
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "-";
        try {
            // Check if ISO format
            SimpleDateFormat inputFormat;
            if (dateStr.contains("T")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            } else {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            }
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd 'Th' MM, yyyy", new Locale("vi", "VN"));
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + "đ";
    }
}
