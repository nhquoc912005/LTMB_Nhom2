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

        setupRecyclerView();
        setupSearch();
        fetchRooms(view);

        return view;
    }

    private void fetchRooms(View view) {
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getRooms().enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    allRooms.clear();
                    for (com.project_mobile.network.ApiModels.RoomDto dto : response.body().data) {
                            allRooms.add(new RoomModel(
                            dto.id,
                            dto.roomNumber,
                            dto.roomType,
                            "Tầng " + dto.roomNumber.charAt(0),
                            dto.capacity + " người",
                            String.format(Locale.US, "%,.0fđ", dto.price),
                            dto.status
                        ));
                    }
                    reloadCurrentList();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>> call, Throwable t) {}
        });
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
        // Obsolete
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
