package com.project_mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvRecentActivities;
    private RecentActivityAdapter adapter;
    private List<RecentActivityModel> activityList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRecentActivities = view.findViewById(R.id.rvRecentActivities);
        setupRecyclerView();
        setHeaderDate(view);
        loadStats(view);
        loadRecentActivities();

        // Listeners cho Thao tác nhanh
        view.findViewById(R.id.cardCheckIn).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openStay(true);
            }
        });

        view.findViewById(R.id.cardCheckOut).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openStay(false);
            }
        });
    }

    private void setupRecyclerView() {
        activityList = new ArrayList<>();
        adapter = new RecentActivityAdapter(activityList);
        rvRecentActivities.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentActivities.setAdapter(adapter);
    }

    private void setHeaderDate(View view) {
        android.widget.TextView tvDate = view.findViewById(R.id.tvCurrentDate);
        if (tvDate == null) return;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'Tháng' MM, yyyy", new Locale("vi", "VN"));
        String date = sdf.format(new Date());
        // Capitalize first letter
        if (date.length() > 0) {
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
        }
        tvDate.setText(date);
    }

    private void loadStats(View view) {
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getStats().enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.DashboardStatsDto>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.DashboardStatsDto>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.DashboardStatsDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    com.project_mobile.network.ApiModels.DashboardStatsDto stats = response.body().data;
                    ((android.widget.TextView) view.findViewById(R.id.tvTotalRooms)).setText(String.valueOf(stats.totalRooms));
                    ((android.widget.TextView) view.findViewById(R.id.tvOccupiedRooms)).setText(String.valueOf(stats.occupiedRooms));
                    ((android.widget.TextView) view.findViewById(R.id.tvAvailableRooms)).setText(String.valueOf(stats.availableRooms));
                    ((android.widget.TextView) view.findViewById(R.id.tvMaintenanceRooms)).setText(String.valueOf(stats.maintenanceRooms));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.DashboardStatsDto>> call, Throwable t) {}
        });
    }

    private void loadRecentActivities() {
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getDashboardActivities().enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.BookingDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.BookingDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.BookingDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<com.project_mobile.network.ApiModels.BookingDto> data = response.body().data;
                    activityList.clear();
                    // Show last 5 activities
                    for (int i = 0; i < Math.min(data.size(), 5); i++) {
                        com.project_mobile.network.ApiModels.BookingDto b = data.get(i);
                        
                        int colorRes = R.color.status_pending_text;
                        int bgRes = R.drawable.bg_status_yellow;
                        String statusStr = b.status != null ? b.status : "Chờ check-in";

                        if (statusStr.contains("nhận phòng") || statusStr.contains("trả phòng") || statusStr.contains("Thành công")) {
                            colorRes = R.color.status_checked_in_text;
                            bgRes = R.drawable.bg_status_green;
                        } else if (statusStr.contains("Đang ở") || statusStr.contains("Bận")) {
                            colorRes = R.color.status_in_use_text;
                            bgRes = R.drawable.bg_status_blue;
                        }

                        // Extract time from activity_time
                        String timeStr = "Gần đây";
                        try {
                            // Extract time HH:mm from activity_time string (ISO or custom)
                            if (b.checkIn != null) {
                                // Try extract from b.checkIn which maps to activity_time from API
                                String rawTime = b.checkIn;
                                if (rawTime.contains("T")) {
                                    String[] parts = rawTime.split("T")[1].split(":");
                                    timeStr = parts[0] + ":" + parts[1];
                                } else if (rawTime.contains(" ")) {
                                    String[] parts = rawTime.split(" ")[1].split(":");
                                    timeStr = parts[0] + ":" + parts[1];
                                }
                            }
                        } catch (Exception e) {}

                        // Format room display
                        String roomDisplay = b.roomNumber;
                        if (roomDisplay == null || roomDisplay.equals("Chưa gán") || roomDisplay.equals("N/A")) {
                            roomDisplay = "Phòng chưa gán";
                        } else if (!roomDisplay.startsWith("Phòng")) {
                            roomDisplay = "Phòng " + roomDisplay;
                        }

                        activityList.add(new RecentActivityModel(
                            roomDisplay,
                            b.customerName != null ? b.customerName : "Khách vãng lai",
                            statusStr,
                            timeStr,
                            colorRes,
                            bgRes
                        ));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.BookingDto>>> call, Throwable t) {}
        });
    }
}
