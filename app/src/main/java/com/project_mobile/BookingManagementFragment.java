package com.project_mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.project_mobile.adapter.BookingAdapter;
import com.project_mobile.model.Booking;
import com.project_mobile.model.BookingStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy mock booking fragment.
 *
 * NOTE: This fragment contains hardcoded/mock data and is kept for reference only.
 * The active booking screen used by the app is
 * `com.project_mobile.datphong_mobile.BookingManagementFragment` (see MainActivity imports).
 * Do not use this fragment for real API integration. Marked deprecated to avoid confusion.
 */
@Deprecated
public class BookingManagementFragment extends Fragment {

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private List<Booking> allBookings = new ArrayList<>();
    
    private LinearLayout boxTotal, boxPending, boxCheckedIn, boxCancelled;
    private TextView tvCountTotal, tvCountPending, tvCountCheckedIn, tvCountCancelled;
    private MaterialButton btnFilterAll, btnFilterToday, btnFilterMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        loadMockData();
        setupListeners();
        
        return view;
    }

    private void initViews(View v) {
        rvBookings = v.findViewById(R.id.rvBookings);
        boxTotal = v.findViewById(R.id.boxTotal);
        boxPending = v.findViewById(R.id.boxPending);
        boxCheckedIn = v.findViewById(R.id.boxCheckedIn);
        boxCancelled = v.findViewById(R.id.boxCancelled);
        
        tvCountTotal = v.findViewById(R.id.tvCountTotal);
        tvCountPending = v.findViewById(R.id.tvCountPending);
        tvCountCheckedIn = v.findViewById(R.id.tvCountCheckedIn);
        tvCountCancelled = v.findViewById(R.id.tvCountCancelled);
        
        btnFilterAll = v.findViewById(R.id.btnFilterAll);
        btnFilterToday = v.findViewById(R.id.btnFilterToday);
        btnFilterMonth = v.findViewById(R.id.btnFilterMonth);
    }

    private void setupRecyclerView() {
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookingAdapter(new ArrayList<>());
        rvBookings.setAdapter(adapter);
    }

    private void loadMockData() {
        allBookings.add(new Booking("1", "Phòng 101", "Nguyễn Văn A", "nguyenvana@gmail.com", "090 123 4567", "01 Th2, 2026", "03 Th2, 2026", "2,000,000đ", BookingStatus.PENDING));
        allBookings.add(new Booking("2", "Phòng 109", "Nguyễn Thị B", "nguyenthib@gmail.com", "090 843 4347", "01 Th2, 2026", "03 Th2, 2026", "2,000,000đ", BookingStatus.PENDING));
        allBookings.add(new Booking("3", "Phòng 104", "Nguyễn Đình C", "nguyendinhc@gmail.com", "020 323 9873", "01 Th2, 2026", "05 Th2, 2026", "4,000,000đ", BookingStatus.CHECKED_IN));
        allBookings.add(new Booking("4", "Phòng 102", "Nguyễn Hà D", "nguyenhad@gmail.com", "020 323 4924", "01 Th2, 2026", "02 Th2, 2026", "1,300,000đ", BookingStatus.CANCELLED));
        allBookings.add(new Booking("5", "Phòng 105", "Trần Văn E", "tranvane@gmail.com", "091 234 5678", "05 Th2, 2026", "07 Th2, 2026", "2,500,000đ", BookingStatus.PENDING));

        updateUI(allBookings);
    }

    private void setupListeners() {
        boxTotal.setOnClickListener(v -> {
            highlightBox(boxTotal);
            updateUI(allBookings);
        });

        boxPending.setOnClickListener(v -> {
            highlightBox(boxPending);
            filterByStatus(BookingStatus.PENDING);
        });

        boxCheckedIn.setOnClickListener(v -> {
            highlightBox(boxCheckedIn);
            filterByStatus(BookingStatus.CHECKED_IN);
        });

        boxCancelled.setOnClickListener(v -> {
            highlightBox(boxCancelled);
            filterByStatus(BookingStatus.CANCELLED);
        });
    }

    private void filterByStatus(BookingStatus status) {
        List<Booking> filtered = new ArrayList<>();
        for (Booking b : allBookings) {
            if (b.getStatus() == status) filtered.add(b);
        }
        adapter.updateData(filtered);
    }

    private void updateUI(List<Booking> list) {
        adapter.updateData(list);
        
        long pending = 0;
        long checkedIn = 0;
        long cancelled = 0;
        for (Booking b : allBookings) {
            if (b.getStatus() == BookingStatus.PENDING) pending++;
            else if (b.getStatus() == BookingStatus.CHECKED_IN) checkedIn++;
            else if (b.getStatus() == BookingStatus.CANCELLED) cancelled++;
        }
        
        tvCountTotal.setText(String.format("%02d", allBookings.size()));
        tvCountPending.setText(String.format("%02d", pending));
        tvCountCheckedIn.setText(String.format("%02d", checkedIn));
        tvCountCancelled.setText(String.format("%02d", cancelled));
    }

    private void highlightBox(LinearLayout selectedBox) {
        resetBoxStyle(boxTotal, tvCountTotal);
        resetBoxStyle(boxPending, tvCountPending);
        resetBoxStyle(boxCheckedIn, tvCountCheckedIn);
        resetBoxStyle(boxCancelled, tvCountCancelled);

        selectedBox.setBackgroundResource(R.drawable.bg_stat_box_active);
        for (int i = 0; i < selectedBox.getChildCount(); i++) {
            View child = selectedBox.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            }
        }
    }

    private void resetBoxStyle(LinearLayout box, TextView countTv) {
        box.setBackgroundResource(R.drawable.bg_stat_box_inactive);
        countTv.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_text_main));
        if (box.getChildAt(0) instanceof TextView) {
            ((TextView) box.getChildAt(0)).setTextColor(ContextCompat.getColor(getContext(), R.color.brand_text_main));
        }
    }
}
