package com.project_mobile.Quan_ly_phong;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project_mobile.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomManagementFragment extends Fragment implements RoomAdapter.OnRoomClickListener {

    private RecyclerView rvRooms;
    private RoomAdapter adapter;
    private EditText edtRoomSearch;
    private final List<RoomModel> allRooms = new ArrayList<>();
    private final List<RoomModel> filteredRooms = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_management, container, false);

        rvRooms = view.findViewById(R.id.rvRooms);
        edtRoomSearch = view.findViewById(R.id.edtRoomSearch);

        seedRooms();
        filteredRooms.addAll(allRooms);
        setupRecyclerView();
        setupSearch();
        updateSummaryCards(view);

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RoomAdapter(filteredRooms, this);
        rvRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRooms.setAdapter(adapter);
    }

    private void setupSearch() {
        edtRoomSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void seedRooms() {
        if (!allRooms.isEmpty()) {
            return;
        }
        allRooms.add(new RoomModel("101", "Standard", "Tầng 1", "2 người", "1.200.000đ", RoomModel.STATUS_EMPTY));
        allRooms.add(new RoomModel("102", "Standard", "Tầng 1", "2 người", "1.200.000đ", RoomModel.STATUS_STAYING, "Nguyễn Văn A", "0901234567", "14/02/2026 - 16/02/2026"));
        allRooms.add(new RoomModel("103", "Standard", "Tầng 1", "2 người", "1.200.000đ", RoomModel.STATUS_MAINTENANCE));
        allRooms.add(new RoomModel("201", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", RoomModel.STATUS_EMPTY));
        allRooms.add(new RoomModel("202", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", RoomModel.STATUS_STAYING, "Trần Thị B", "0988777666", "15/02/2026 - 17/02/2026"));
        allRooms.add(new RoomModel("203", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", RoomModel.STATUS_EMPTY));
        allRooms.add(new RoomModel("301", "Suite", "Tầng 3", "4 người", "2.500.000đ", RoomModel.STATUS_IN_USE, "Lê Văn C", "0912345678", "16/02/2026 - 20/02/2026"));
        allRooms.add(new RoomModel("302", "Suite", "Tầng 3", "4 người", "2.500.000đ", RoomModel.STATUS_EMPTY));
        allRooms.add(new RoomModel("401", "Suite", "Tầng 4", "4 người", "2.500.000đ", RoomModel.STATUS_IN_USE, "Phạm Minh D", "0909090909", "18/02/2026 - 21/02/2026"));
        allRooms.add(new RoomModel("402", "Deluxe", "Tầng 4", "2 người", "1.800.000đ", RoomModel.STATUS_MAINTENANCE));
    }

    private void applySearch(String rawQuery) {
        String query = normalize(rawQuery);
        filteredRooms.clear();
        for (RoomModel room : allRooms) {
            if (query.isEmpty()
                    || normalize(room.getRoomNumber()).contains(query)
                    || normalize(room.getRoomType()).contains(query)
                    || normalize(room.getFloor()).contains(query)
                    || normalize(room.getStatus()).contains(query)) {
                filteredRooms.add(room);
            }
        }
        adapter.submitList(filteredRooms);
    }

    private void reloadCurrentList() {
        applySearch(edtRoomSearch == null ? "" : edtRoomSearch.getText().toString());
        updateSummaryCards(requireView());
    }

    private void updateSummaryCards(View view) {
        bindSummary(view.findViewById(R.id.cardTotal), allRooms.size(), "Tổng\nphòng", "#D1C19F");
        bindSummary(view.findViewById(R.id.cardEmpty), countEmptyRooms(), "Trống", "#D1C19F");
        bindSummary(view.findViewById(R.id.cardInUse), countOccupiedRooms(), "Đang\nsử dụng", "#D1C19F");
        bindSummary(view.findViewById(R.id.cardMaintenance), countMaintenanceRooms(), "Bảo trì", "#C0410D");
    }

    private int countEmptyRooms() {
        int count = 0;
        for (RoomModel room : allRooms) {
            if (room.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private int countOccupiedRooms() {
        int count = 0;
        for (RoomModel room : allRooms) {
            if (room.isOccupied()) {
                count++;
            }
        }
        return count;
    }

    private int countMaintenanceRooms() {
        int count = 0;
        for (RoomModel room : allRooms) {
            if (room.isMaintenance()) {
                count++;
            }
        }
        return count;
    }

    private void bindSummary(View card, int value, String label, String valueColor) {
        TextView tvValue = card.findViewById(R.id.tvSummaryValue);
        TextView tvLabel = card.findViewById(R.id.tvSummaryLabel);
        tvValue.setText(String.valueOf(value));
        tvValue.setTextColor(Color.parseColor(valueColor));
        tvLabel.setText(label);
    }

    @Override
    public void onRoomClick(RoomModel room) {
        RoomDetailBottomSheet bottomSheet = RoomDetailBottomSheet.newInstance(room);
        bottomSheet.setRooms(allRooms);
        bottomSheet.setOnRoomsChangedListener(this::reloadCurrentList);
        bottomSheet.show(getChildFragmentManager(), "RoomDetailBottomSheet");
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
