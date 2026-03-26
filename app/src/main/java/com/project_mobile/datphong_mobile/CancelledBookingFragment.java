package com.project_mobile.datphong_mobile;

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

public class CancelledBookingFragment extends Fragment {

    private RecyclerView rvBookings;
    private CancelledBookingAdapter adapter;
    private List<CancelledBooking> bookingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cancelled_booking, container, false);

        rvBookings = view.findViewById(R.id.rvCancelledBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        // Dữ liệu mẫu khớp hoàn toàn với hình ảnh thứ 4 (Phòng 102 - Đã hủy)
        bookingList.add(new CancelledBooking(
                "Phòng 102",
                "Đã hủy",
                "Nguyễn Hà D",
                "nguyenhad@gmail.com",
                "020 323 4924",
                "01 Th2, 2026",
                "02 Th2, 2026",
                "1,300,000đ"
        ));

        adapter = new CancelledBookingAdapter(bookingList);
        rvBookings.setAdapter(adapter);

        return view;
    }
}
