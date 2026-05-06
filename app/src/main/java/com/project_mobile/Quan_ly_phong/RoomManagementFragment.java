// Module quản lý phòng Android.
// File này hiển thị lưới phòng, thống kê trạng thái phòng và mở bottom sheet chi tiết phòng.
// Dữ liệu chính lấy từ /api/rooms và map sang RoomModel.
/*
 * File: RoomManagementFragment.java
 * Module: Quản lý phòng.
 *
 * Luồng chính:
 * - fetchRooms gọi GET /api/rooms để lấy danh sách phòng từ backend.
 * - RoomDto được map sang RoomModel để hiển thị trong RecyclerView.
 * - Người dùng tìm kiếm theo số phòng, loại phòng hoặc trạng thái.
 * - Khi bấm vào một phòng, RoomDetailBottomSheet hiển thị chi tiết và hành động liên quan.
 *
 * Dữ liệu chính:
 * - allRooms: danh sách phòng gốc từ API.
 * - filteredRooms: danh sách sau tìm kiếm.
 */
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

/**
 * RoomManagementFragment phụ trách màn Quản lý phòng.
 * Class này tải danh sách phòng, lọc theo từ khóa và chuyển phòng được chọn sang RoomDetailBottomSheet.
 */
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

    /** Gọi API lấy phòng, map RoomDto sang RoomModel và cập nhật thống kê. */
    /*
     * Tải danh sách phòng từ backend.
     * API: GET /api/rooms.
     * Output: allRooms/filteredRooms được cập nhật và các card thống kê được tính lại.
     */
    private void fetchRooms(View view) {
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getRooms().enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    allRooms.clear();
                    for (com.project_mobile.network.ApiModels.RoomDto dto : response.body().data) {
                            // Suy luận tầng từ ký tự đầu của số phòng để khớp UI hiện tại.
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

    /** Thiết lập lưới 2 cột cho danh sách phòng. */
    private void setupRecyclerView() {
        adapter = new RoomAdapter(filteredRooms, this);
        rvRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRooms.setAdapter(adapter);
    }

    /** Gắn tìm kiếm realtime theo số phòng, loại phòng, tầng hoặc trạng thái. */
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

    /** Lọc phòng theo từ khóa đã normalize và gửi danh sách mới cho adapter. */
    /*
     * Lọc phòng theo từ khóa.
     * Từ khóa được so sánh với số phòng, loại phòng và trạng thái đã chuẩn hóa chữ thường.
     */
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

    /** Reload theo từ khóa hiện tại sau khi API hoặc bottom sheet cập nhật dữ liệu. */
    private void reloadCurrentList() {
        applySearch(edtRoomSearch == null ? "" : edtRoomSearch.getText().toString());
        updateSummaryCards(requireView());
    }

    /** Cập nhật các ô tổng phòng, trống, đang sử dụng và bảo trì. */
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
    /** Khi chọn phòng, mở bottom sheet chi tiết và truyền toàn bộ danh sách để đổi/trạng thái. */
    /*
     * Mở bottom sheet chi tiết phòng.
     * Bottom sheet nhận danh sách phòng hiện tại để có thể tìm lại phòng mới nhất khi đổi trạng thái.
     */
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
