package com.project_mobile.check_in;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CalendarView;
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

public class CheckInFragment extends Fragment {

    private RecyclerView rvCheckIn;
    private CheckInAdapter adapter;
    private List<CheckInModel> checkInList;
    private TextView tvCheckInDate, tvCheckOutDate;
    private LinearLayout llCheckInDate, llCheckOutDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin, container, false);

        rvCheckIn = view.findViewById(R.id.rvCheckIn);
        tvCheckInDate = view.findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = view.findViewById(R.id.tvCheckOutDate);
        llCheckInDate = view.findViewById(R.id.llCheckInDate);
        llCheckOutDate = view.findViewById(R.id.llCheckOutDate);

        llCheckInDate.setOnClickListener(v -> showDatePicker(tvCheckInDate));
        llCheckOutDate.setOnClickListener(v -> showDatePicker(tvCheckOutDate));

        rvCheckIn.setLayoutManager(new LinearLayoutManager(getContext()));

        checkInList = new ArrayList<>();
        // Dữ liệu mẫu theo hình ảnh
        checkInList.add(new CheckInModel("Nguyễn Văn A", "Phòng 101", "0901234567", "nguyenvana@email.com", "14/02/2026 - 16/02/2026"));
        checkInList.add(new CheckInModel("Nguyễn Văn B", "Phòng 101", "0901234567", "nguyenvana@email.com", "14/02/2026 - 16/02/2026"));
        checkInList.add(new CheckInModel("Nguyễn Văn C", "Phòng 101", "0901234567", "nguyenvana@email.com", "14/02/2026 - 16/02/2026"));

        adapter = new CheckInAdapter(checkInList);
        rvCheckIn.setAdapter(adapter);

        return view;
    }

    private void showDatePicker(TextView targetTextView) {
        Dialog calendarDialog = new Dialog(requireContext());
        calendarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        calendarDialog.setContentView(R.layout.dialog_custom_calendar);
        if (calendarDialog.getWindow() != null) {
            calendarDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        CalendarView calendarView = calendarDialog.findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            targetTextView.setText(date);
            calendarDialog.dismiss();
        });

        calendarDialog.show();
    }
}
