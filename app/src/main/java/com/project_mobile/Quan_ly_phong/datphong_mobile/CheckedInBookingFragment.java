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

public class CheckedInBookingFragment extends Fragment {

    private RecyclerView rvBookings;
    private CheckedInBookingAdapter adapter;
    private List<CheckedInBooking> bookingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checked_in_booking, container, false);

        rvBookings = view.findViewById(R.id.rvCheckedInBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        // Dữ liệu mẫu khớp hoàn toàn với hình ảnh thứ 3
        bookingList.add(new CheckedInBooking(
                "Phòng 104",
                "Đã check-in",
                "Nguyễn Đình C",
                "nguyendinhc@gmail.com",
                "020 323 9873",
                "01 Th2, 2026",
                "05 Th2, 2026",
                "4,000,000đ"
        ));

        adapter = new CheckedInBookingAdapter(bookingList);
        rvBookings.setAdapter(adapter);

        return view;
    }
}
