package com.project_mobile.dat_phong;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.ArrayList;
import java.util.List;

public class RoomManagementFragment extends Fragment {

    private RecyclerView rvRooms;
    private RoomAdapter adapter;
    private List<RoomModel> roomList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_management, container, false);

        rvRooms = view.findViewById(R.id.rvRooms);
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        roomList = new ArrayList<>();
        // Mock data
        roomList.add(new RoomModel("101", "Standard", "Tầng 1", "2 người", "1.200.000đ", "Trống"));
        roomList.add(new RoomModel("102", "Standard", "Tầng 1", "2 người", "1.200.000đ", "Đang sử dụng", "Nguyễn Văn A", "0901234567", "14/02/2026 - 16/02/2026"));
        roomList.add(new RoomModel("103", "Standard", "Tầng 1", "2 người", "1.200.000đ", "Bảo trì"));
        roomList.add(new RoomModel("201", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", "Trống"));
        roomList.add(new RoomModel("202", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", "Đang sử dụng", "Trần Thị B", "0988777666", "15/02/2026 - 17/02/2026"));
        roomList.add(new RoomModel("203", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", "Trống"));
        roomList.add(new RoomModel("301", "Suite", "Tầng 3", "4 người", "2.500.000đ", "Đang sử dụng", "Lê Văn C", "0912345678", "16/02/2026 - 20/02/2026"));
        roomList.add(new RoomModel("302", "Suite", "Tầng 3", "4 người", "2.500.000đ", "Trống"));

        adapter = new RoomAdapter(roomList);
        rvRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRooms.setAdapter(adapter);
    }
}
