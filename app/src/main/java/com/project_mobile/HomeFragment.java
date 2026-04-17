package com.project_mobile;

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
import java.util.ArrayList;
import java.util.List;

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

        // Listeners cho Thao tác nhanh
        view.findViewById(R.id.cardCheckIn).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Nhận phòng", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.cardCheckOut).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Trả phòng", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        activityList = new ArrayList<>();
        // Mock data
        activityList.add(new RecentActivityModel("Phòng 101", "Nguyễn Văn A", "Chờ check-in", "14:30", R.color.status_pending_text, R.drawable.bg_status_pending));
        activityList.add(new RecentActivityModel("Phòng 205", "Trần Thị B", "Đã check-in", "12:15", R.color.status_checked_in_text, R.drawable.bg_status_badge));
        
        adapter = new RecentActivityAdapter(activityList);
        rvRecentActivities.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentActivities.setAdapter(adapter);
    }
}
