package com.project_mobile.datphong_mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;
import com.project_mobile.PostgreSQLConnection;
import android.util.Log;

public class BookingManagementFragment extends Fragment {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private List<Booking> fullList = new ArrayList<>();
    private TextView tvTotal, tvPending, tvCheckedIn, tvCancelled;
    private LinearLayout boxTotal, boxPending, boxCheckedIn, boxCancelled;
    private List<View> statBoxes = new ArrayList<>();
    private List<View> filterButtons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_management, container, false);
        initViews(view);
        setupData();
        setupFilters(view);
        return view;
    }

    private void initViews(View view) {
        rvBookings = view.findViewById(R.id.rvBookings);
        tvTotal = view.findViewById(R.id.tvCountTotal);
        tvPending = view.findViewById(R.id.tvCountPending);
        tvCheckedIn = view.findViewById(R.id.tvCountCheckedIn);
        tvCancelled = view.findViewById(R.id.tvCountCancelled);

        boxTotal = view.findViewById(R.id.boxTotal);
        boxPending = view.findViewById(R.id.boxPending);
        boxCheckedIn = view.findViewById(R.id.boxCheckedIn);
        boxCancelled = view.findViewById(R.id.boxCancelled);

        statBoxes.add(boxTotal);
        statBoxes.add(boxPending);
        statBoxes.add(boxCheckedIn);
        statBoxes.add(boxCancelled);

        filterButtons.add(view.findViewById(R.id.btnFilterAll));
        filterButtons.add(view.findViewById(R.id.btnFilterToday));
        filterButtons.add(view.findViewById(R.id.btnFilterMonth));

        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Mặc định chọn box đầu tiên
        boxTotal.setSelected(true);
        filterButtons.get(0).setSelected(true);
    }

    private void updateSelection(View selectedView, List<View> group) {
        for (View v : group) {
            v.setSelected(v == selectedView);
        }
    }

    private void setupData() {
        fullList.clear();
        
        // Show loading state if needed (optional)
        
        Executors.newSingleThreadExecutor().execute(() -> {
            Connection conn = PostgreSQLConnection.getConnection();
            if (conn != null) {
                try {
                    String query = "SELECT so_phong, trang_thai, ten_nguoi_dat, email, sdt_nguoi_dat, ngay_nhan, ngay_tra, tong_thanh_toan FROM dat_phong ORDER BY ma_dat_phong DESC";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    
                    while (rs.next()) {
                        String roomName = rs.getString("so_phong");
                        String status = rs.getString("trang_thai");
                        String customerName = rs.getString("ten_nguoi_dat");
                        String customerEmail = rs.getString("email");
                        String customerPhone = rs.getString("sdt_nguoi_dat");
                        String checkIn = rs.getTimestamp("ngay_nhan") != null ? sdf.format(rs.getTimestamp("ngay_nhan")) : "N/A";
                        String checkOut = rs.getTimestamp("ngay_tra") != null ? sdf.format(rs.getTimestamp("ngay_tra")) : "N/A";
                        String price = String.format(Locale.getDefault(), "%,.0fđ", rs.getDouble("tong_thanh_toan"));
                        
                        fullList.add(new Booking(roomName, status, customerName, customerEmail, customerPhone, checkIn, checkOut, price));
                    }
                    
                    rs.close();
                    stmt.close();
                    conn.close();
                } catch (Exception e) {
                    Log.e("BookingFragment", "Error fetching data: " + e.getMessage());
                }
            }
            
            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    updateStats();
                    filterList("Tất cả");
                });
            }
        });
    }

    private void updateStats() {
        tvTotal.setText(String.format("%02d", fullList.size()));
        tvPending.setText(String.format("%02d", (int) fullList.stream().filter(b -> b.getStatus().equals("Chờ check-in")).count()));
        tvCheckedIn.setText(String.format("%02d", (int) fullList.stream().filter(b -> b.getStatus().equals("Đã Check-in")).count()));
        tvCancelled.setText(String.format("%02d", (int) fullList.stream().filter(b -> b.getStatus().equals("Đã hủy")).count()));
    }

    private void filterList(String status) {
        List<Booking> filtered;
        if (status.equals("Tất cả")) {
            filtered = new ArrayList<>(fullList);
        } else {
            filtered = fullList.stream().filter(b -> b.getStatus().equalsIgnoreCase(status)).collect(Collectors.toList());
        }

        adapter = new BookingAdapter(filtered, booking -> {
            booking.setStatus("Đã hủy");
            updateStats();
            filterList("Tất cả");
        });
        rvBookings.setAdapter(adapter);
    }

    private void setupFilters(View view) {
        boxTotal.setOnClickListener(v -> { filterList("Tất cả"); updateSelection(v, statBoxes); });
        boxPending.setOnClickListener(v -> { filterList("Chờ check-in"); updateSelection(v, statBoxes); });
        boxCheckedIn.setOnClickListener(v -> { filterList("Đã Check-in"); updateSelection(v, statBoxes); });
        boxCancelled.setOnClickListener(v -> { filterList("Đã hủy"); updateSelection(v, statBoxes); });

        for (View btn : filterButtons) {
            btn.setOnClickListener(v -> {
                if (v.getId() == R.id.btnFilterAll) filterList("Tất cả");
                updateSelection(v, filterButtons);
            });
        }
    }
}
