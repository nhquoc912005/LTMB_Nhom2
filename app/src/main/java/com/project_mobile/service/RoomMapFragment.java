package com.project_mobile.service;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project_mobile.R;

import java.util.ArrayList;
import java.util.List;

public class RoomMapFragment extends Fragment {

    // Màn hình chính
    private LinearLayout layoutRoomMap;
    private RecyclerView rcvRoomMap;

    // Màn hình chi tiết
    private RelativeLayout layoutRoomDetail;
    private TextView tvDetailRoomTitle;
    private LinearLayout layoutAddedServices, layoutAddedAssets;
    private LinearLayout sectionServices, sectionAssets;
    private LinearLayout layoutActionButtons;

    // Thanh Tab
    private TextView tvTabService, tvTabAsset;
    private boolean isServiceTab = true;
    private String currentRoomNumber = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_map, container, false);

        // Ánh xạ các view Tab
        tvTabService = view.findViewById(R.id.tvTabService);
        tvTabAsset = view.findViewById(R.id.tvTabAsset);

        // Ánh xạ màn hình sơ đồ
        layoutRoomMap = view.findViewById(R.id.layoutRoomMap);
        rcvRoomMap = view.findViewById(R.id.rcvRoomMap);

        // Ánh xạ màn hình chi tiết và các phần tách biệt
        layoutRoomDetail = view.findViewById(R.id.layoutRoomDetail);
        tvDetailRoomTitle = view.findViewById(R.id.tvDetailRoomTitle);
        
        sectionServices = view.findViewById(R.id.sectionServices);
        sectionAssets = view.findViewById(R.id.sectionAssets);
        layoutAddedServices = view.findViewById(R.id.layoutAddedServices);
        layoutAddedAssets = view.findViewById(R.id.layoutAddedAssets);
        
        layoutActionButtons = view.findViewById(R.id.layoutActionButtons);

        setupTabs();
        loadFloorData();
        setupDetailActions(view);

        return view;
    }

    private void setupTabs() {
        tvTabService.setOnClickListener(v -> {
            isServiceTab = true;
            updateTabUI();
            updateDetailSectionVisibility();
        });
        tvTabAsset.setOnClickListener(v -> {
            isServiceTab = false;
            updateTabUI();
            updateDetailSectionVisibility();
        });
        updateTabUI();
    }

    private void updateTabUI() {
        if (isServiceTab) {
            tvTabService.setBackgroundResource(R.drawable.bg_tab_left_active);
            tvTabService.setTextColor(Color.parseColor("#C58959"));
            tvTabAsset.setBackgroundResource(0);
            tvTabAsset.setTextColor(Color.BLACK);
        } else {
            tvTabAsset.setBackgroundResource(R.drawable.bg_tab_right_active);
            tvTabAsset.setTextColor(Color.parseColor("#C58959"));
            tvTabService.setBackgroundResource(0);
            tvTabService.setTextColor(Color.BLACK);
        }
    }

    private void updateDetailSectionVisibility() {
        if (layoutRoomDetail.getVisibility() == View.VISIBLE) {
            sectionServices.setVisibility(isServiceTab ? View.VISIBLE : View.GONE);
            sectionAssets.setVisibility(isServiceTab ? View.GONE : View.VISIBLE);
            
            // CHỈNH TẠI ĐÂY: Chỉ hiện nút Lưu/Hủy nếu Tab đang mở có ít nhất 1 item
            int itemCount = isServiceTab ? layoutAddedServices.getChildCount() : layoutAddedAssets.getChildCount();
            layoutActionButtons.setVisibility(itemCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void loadFloorData() {
        List<FloorModel> floorList = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            List<String> rooms = new ArrayList<>();
            rooms.add(i + "01"); rooms.add(i + "02"); rooms.add(i + "03");
            floorList.add(new FloorModel("Tầng " + i, rooms));
        }

        rcvRoomMap.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvRoomMap.setAdapter(new FloorAdapter(floorList, roomNumber -> showRoomDetailView(roomNumber)));
    }

    private void showRoomDetailView(String roomNumber) {
        currentRoomNumber = roomNumber;
        tvDetailRoomTitle.setText("Phòng " + roomNumber);

        // Reset dữ liệu cũ khi mở phòng mới
        layoutAddedServices.removeAllViews();
        layoutAddedAssets.removeAllViews();
        
        layoutRoomMap.setVisibility(View.GONE);
        layoutRoomDetail.setVisibility(View.VISIBLE);
        updateDetailSectionVisibility();
    }

    private void setupDetailActions(View view) {
        view.findViewById(R.id.btnDetailClose).setOnClickListener(v -> closeDetail());
        view.findViewById(R.id.btnCancelChanges).setOnClickListener(v -> closeDetail());
        view.findViewById(R.id.btnSaveChanges).setOnClickListener(v -> {
            String type = isServiceTab ? "Dịch vụ" : "Bồi thường";
            Toast.makeText(getContext(), "Đã lưu thông tin cho phòng " + currentRoomNumber, Toast.LENGTH_SHORT).show();
            closeDetail();
        });

        view.findViewById(R.id.btnAddServiceInline).setOnClickListener(v -> showBottomSheetOptions());
        view.findViewById(R.id.btnAddAssetInline).setOnClickListener(v -> showBottomSheetOptions());
    }

    private void closeDetail() {
        layoutRoomDetail.setVisibility(View.GONE);
        layoutRoomMap.setVisibility(View.VISIBLE);
    }

    private void showBottomSheetOptions() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_bottom_sheet_options, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);

        TextView tvTitle = view.findViewById(R.id.tvBottomSheetTitle);
        TextView tvOp1 = view.findViewById(R.id.tvOption1);
        TextView tvOp2 = view.findViewById(R.id.tvOption2);
        TextView tvOp3 = view.findViewById(R.id.tvOption3);
        TextView tvOp4 = view.findViewById(R.id.tvOption4);
        View line4 = view.findViewById(R.id.lineOption4);

        if (isServiceTab) {
            tvTitle.setText("Chọn dịch vụ");
            tvOp1.setText("Buffet sáng"); tvOp2.setText("Giặt ủi"); tvOp3.setText("Đưa đón sân bay");
            tvOp4.setVisibility(View.GONE); line4.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Chọn tài sản bồi thường");
            tvOp1.setText("Bàn"); tvOp2.setText("Ghế"); tvOp3.setText("Tủ đầu giường");
            tvOp4.setVisibility(View.VISIBLE); line4.setVisibility(View.VISIBLE); tvOp4.setText("Trang trí");
        }

        View.OnClickListener listener = v -> {
            addItemToRoom(((TextView) v).getText().toString());
            dialog.dismiss();
        };

        tvOp1.setOnClickListener(listener); tvOp2.setOnClickListener(listener);
        tvOp3.setOnClickListener(listener); tvOp4.setOnClickListener(listener);

        dialog.show();
    }

    private void addItemToRoom(String itemName) {
        LinearLayout container = isServiceTab ? layoutAddedServices : layoutAddedAssets;

        View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_added_service, null);
        TextView tvName = itemView.findViewById(R.id.tvItemName);
        int count = container.getChildCount() + 1;
        tvName.setText(count + ". " + itemName);

        itemView.findViewById(R.id.btnDeleteItem).setOnClickListener(v -> {
            container.removeView(itemView);
            renumberItems(container);
            updateDetailSectionVisibility(); // Cập nhật ẩn hiện nút Lưu khi xóa hết
        });

        container.addView(itemView);
        updateDetailSectionVisibility(); // Hiện nút Lưu ngay khi thêm 1 item
    }

    private void renumberItems(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView tv = container.getChildAt(i).findViewById(R.id.tvItemName);
            String text = tv.getText().toString();
            tv.setText((i + 1) + ". " + text.substring(text.indexOf(".") + 2));
        }
    }
}