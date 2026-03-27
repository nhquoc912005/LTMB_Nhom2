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

public class BookingManagementFragment extends Fragment {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private List<Booking> bookingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_management, container, false);

        rvBookings = view.findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        // Dữ liệu mẫu y chang trong hình
        bookingList.add(new Booking(
                "Phòng 101",
                "Chờ check-in",
                "Nguyễn Văn A",
                "nguyenvana@gmail.com",
                "090 123 4567",
                "01 Th2, 2026",
                "03 Th2, 2026",
                "2,000,000đ"
        ));
        
        // Thêm vài mẫu nữa để cuộn được
        bookingList.add(new Booking(
                "Phòng 102",
                "Đã Check-in",
                "Trần Thị B",
                "tranb@gmail.com",
                "091 888 9999",
                "02 Th2, 2026",
                "04 Th2, 2026",
                "1,500,000đ"
        ));

        adapter = new BookingAdapter(bookingList);
        rvBookings.setAdapter(adapter);

        return view;
    }
}
