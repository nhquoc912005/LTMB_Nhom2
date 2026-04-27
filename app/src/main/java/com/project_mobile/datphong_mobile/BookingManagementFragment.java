package com.project_mobile.datphong_mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
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
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingManagementFragment extends Fragment {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private List<BookingDto> allData = new ArrayList<>();
    private List<View> statBoxes = new ArrayList<>();
    private TextView tvCountTotal, tvCountPending, tvCountCheckedIn, tvCountCancelled;
    private TextView tvBookingCurrentDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_management, container, false);
        
        rvBookings = view.findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        
        tvCountTotal = view.findViewById(R.id.tvCountTotal);
        tvCountPending = view.findViewById(R.id.tvCountPending);
        tvCountCheckedIn = view.findViewById(R.id.tvCountCheckedIn);
        tvCountCancelled = view.findViewById(R.id.tvCountCancelled);
        tvBookingCurrentDate = view.findViewById(R.id.tvBookingCurrentDate);

        statBoxes.clear();
        statBoxes.add(view.findViewById(R.id.boxTotal));
        statBoxes.add(view.findViewById(R.id.boxPending));
        statBoxes.add(view.findViewById(R.id.boxCheckedIn));
        statBoxes.add(view.findViewById(R.id.boxCancelled));

        setHeaderDate();
        loadBookings();
        setupFilters(view);

        return view;
    }

    private void setHeaderDate() {
        if (tvBookingCurrentDate == null) return;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM, yyyy", new Locale("vi", "VN"));
        String date = sdf.format(new Date());
        if (date.length() > 0) {
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
        }
        tvBookingCurrentDate.setText(date);
    }

    private void loadBookings() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getBookings().enqueue(new Callback<ApiResponse<List<BookingDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDto>>> call, Response<ApiResponse<List<BookingDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    allData = response.body().data;
                    updateStats(allData);
                    filterList("Tất cả");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<BookingDto>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(String statusFilter) {
        bookingList.clear();
        long now = System.currentTimeMillis();
        long oneWeekMs = 7L * 24 * 60 * 60 * 1000;
        
        for (BookingDto b : allData) {
            String s = b.status != null ? b.status : "";
            boolean matches = false;
            
            // Kiểm tra thời gian nếu là "Tất cả" (trong vòng 1 tuần từ nay)
            boolean withinWeek = true;
            if (statusFilter.equals("Tất cả")) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date checkInDate = sdf.parse(b.checkIn);
                    long diff = checkInDate.getTime() - now;
                    // Lấy các booking đã nhận phòng (nhưng chưa checkout) hoặc sắp nhận trong 7 ngày tới
                    // Hoặc đơn giản là check-in trong [now - 1 day, now + 7 days]
                    withinWeek = (diff >= -24 * 60 * 60 * 1000 && diff <= oneWeekMs);
                } catch (Exception e) { withinWeek = true; }
            }

            if (statusFilter.equals("Tất cả")) matches = withinWeek;
            else if (statusFilter.equals("Chờ check-in") && (s.contains("Đã đặt cọc") || s.contains("Chờ check-in"))) matches = true;
            else if (statusFilter.equals("Đã check-in") && (s.contains("Đang ở") || s.contains("Đã check-in") || s.contains("nhận phòng"))) matches = true;
            else if (statusFilter.equals("Đã hủy") && (s.contains("Đã hủy") || s.contains("Hủy"))) matches = true;

            if (matches) {
                String roomNum = b.roomNumber;
                if (roomNum == null || roomNum.isEmpty()) roomNum = "N/A";
                if (!roomNum.startsWith("Phòng")) roomNum = "Phòng " + roomNum;

                String status = b.status != null ? b.status : "Chờ check-in";
                if (status.equals("Đang ở")) status = "Đã check-in";

                bookingList.add(new Booking(
                        roomNum,
                        status,
                        b.customerName != null ? b.customerName : "Khách vãng lai",
                        b.email != null ? b.email : "-",
                        b.phone != null ? b.phone : "-",
                        formatDate(b.checkIn),
                        formatDate(b.checkOut),
                        formatCurrency(b.totalAmount != null ? b.totalAmount : 0),
                        b.totalGuests != null ? b.totalGuests : 0,
                        b.adults != null ? b.adults : 0,
                        b.children != null ? b.children : 0
                ));
            }
        }
        adapter = new BookingAdapter(bookingList, booking -> {
            // Logic cancel
        });
        rvBookings.setAdapter(adapter);
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
        if (tvCountTotal != null) tvCountTotal.setText(String.format(Locale.US, "%02d", total));
        if (tvCountPending != null) tvCountPending.setText(String.format(Locale.US, "%02d", waiting));
        if (tvCountCheckedIn != null) tvCountCheckedIn.setText(String.format(Locale.US, "%02d", checkedIn));
        if (tvCountCancelled != null) tvCountCancelled.setText(String.format(Locale.US, "%02d", cancelled));
    }

    private void setupFilters(View view) {
        View boxTotal = view.findViewById(R.id.boxTotal);
        View boxPending = view.findViewById(R.id.boxPending);
        View boxCheckedIn = view.findViewById(R.id.boxCheckedIn);
        View boxCancelled = view.findViewById(R.id.boxCancelled);

        boxTotal.setOnClickListener(v -> { filterList("Tất cả"); selectBox(v); });
        boxPending.setOnClickListener(v -> { filterList("Chờ check-in"); selectBox(v); });
        boxCheckedIn.setOnClickListener(v -> { filterList("Đã check-in"); selectBox(v); });
        boxCancelled.setOnClickListener(v -> { filterList("Đã hủy"); selectBox(v); });

        view.findViewById(R.id.btnFilterAll).setOnClickListener(v -> loadBookings());
    }

    private void selectBox(View selected) {
        for (View v : statBoxes) {
            if (v == null) continue;
            boolean isSelected = (v == selected);
            com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) v;
            card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(isSelected ? 0xFFA3734D : 0xFFFFFFFF));
            
            ViewGroup layout = (ViewGroup) card.getChildAt(0);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(isSelected ? 0xFFFFFFFF : 0xFF333333);
                }
            }
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "-";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("dd 'Th'MM, yyyy", new Locale("vi", "VN"));
            Date d = in.parse(isoDate);
            return out.format(d);
        } catch (Exception e) { return isoDate; }
    }

    private String formatCurrency(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(amount) + "đ";
    }
}
