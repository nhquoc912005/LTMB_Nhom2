package com.project_mobile.Quan_ly_phong.datphong_mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.ArrayList;
import java.util.List;

public class RoomBookingFragmentV2 extends Fragment {

    private RecyclerView rvRoomBookings;
    private RoomBookingAdapter adapter;
    private List<RoomBooking> bookingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_booking_v2, container, false);

        rvRoomBookings = view.findViewById(R.id.rvRoomBookingsV2);
        rvRoomBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        // Dữ liệu mẫu khớp hoàn toàn với hình ảnh thứ 2
        bookingList.add(new RoomBooking(
                "Phòng 109",
                "Chờ check-in",
                "Nguyễn Thị B",
                "nguyenthib@gmail.com",
                "090 843 4347",
                "01 Th2, 2026",
                "03 Th2, 2026",
                "2,000,000đ"
        ));

        adapter = new RoomBookingAdapter(bookingList);
        rvRoomBookings.setAdapter(adapter);

        return view;
    }
}
