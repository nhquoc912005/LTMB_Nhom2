package com.project_mobile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project_mobile.service.ServiceFragment;

import java.util.ArrayList;
import java.util.List;

public class RoomManagementFragment extends Fragment {

    private RecyclerView rvRooms;
    private RoomAdapter adapter;
    private List<RoomModel> roomList;
    private ImageView ivMenu;

    // ĐÃ THÊM: Biến kiểm tra tab để tránh lỗi đỏ ở dòng if (isServiceTab)
    private boolean isServiceTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_management, container, false);

        rvRooms = view.findViewById(R.id.rvRooms);
        ivMenu = view.findViewById(R.id.ivMenu);

        setupRecyclerView();
        setupMenu();

        return view;
    }

    private void setupMenu() {
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.getMenu().add("Trang chủ");
                popupMenu.getMenu().add("Lưu trú");
                popupMenu.getMenu().add("Đặt phòng");
                popupMenu.getMenu().add("Quản lý phòng");
                popupMenu.getMenu().add("Dịch vụ");

                popupMenu.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    if (title.equals("Dịch vụ")) {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ServiceFragment())
                                .addToBackStack(null)
                                .commit();
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }
    }

    private void setupRecyclerView() {
        roomList = new ArrayList<>();
        roomList.add(new RoomModel("101", "Standard", "Tầng 1", "2 người", "1.200.000đ", "Trống"));
        roomList.add(new RoomModel("102", "Standard", "Tầng 1", "2 người", "1.200.000đ", "Đang sử dụng", "Nguyễn Văn A", "0901234567", "14/02/2026 - 16/02/2026"));
        roomList.add(new RoomModel("103", "Standard", "Tầng 1", "2 người", "1.200.000đ", "Bảo trì"));
        roomList.add(new RoomModel("201", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", "Trống"));
        roomList.add(new RoomModel("202", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", "Đang sử dụng", "Trần Thị B", "0988777666", "15/02/2026 - 17/02/2026"));
        roomList.add(new RoomModel("203", "Deluxe", "Tầng 2", "2 người", "1.800.000đ", "Trống"));
        roomList.add(new RoomModel("301", "Suite", "Tầng 3", "4 người", "2.500.000đ", "Đang sử dụng", "Lê Văn C", "0912345678", "16/02/2026 - 20/02/2026"));
        roomList.add(new RoomModel("302", "Suite", "Tầng 3", "4 người", "2.500.000đ", "Trống"));

        // LƯU Ý TẠI ĐÂY: Trong RoomAdapter của bạn cần có interface để bắt sự kiện click
        adapter = new RoomAdapter(roomList);
        rvRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvRooms.setAdapter(adapter);
    }

    // --- MÀN HÌNH 1: HIỂN THỊ POPUP CHI TIẾT PHÒNG ---
    private void showRoomDetailDialog(String roomNumber) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.dialog_room_detail_full);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = dialog.findViewById(R.id.tvRoomTitle);
        tvTitle.setText("Phòng " + roomNumber);

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        FloatingActionButton fabAdd = dialog.findViewById(R.id.fabAddServiceToRoom);
        fabAdd.setOnClickListener(v -> {
            showBottomSheetServices(roomNumber);
        });

        dialog.show();
    }

    // --- MÀN HÌNH 2: BOTTOM SHEET TRƯỢT LÊN ---
    private void showBottomSheetServices(String roomNumber) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_bottom_sheet_options, null);
        bottomSheetDialog.setContentView(view);

        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);

        TextView tvTitle = view.findViewById(R.id.tvBottomSheetTitle);
        TextView tvOption1 = view.findViewById(R.id.tvOption1);
        TextView tvOption2 = view.findViewById(R.id.tvOption2);
        TextView tvOption3 = view.findViewById(R.id.tvOption3);
        TextView tvOption4 = view.findViewById(R.id.tvOption4);
        View lineOption4 = view.findViewById(R.id.lineOption4);

        if (isServiceTab) {
            tvTitle.setText("Dịch vụ");
            tvOption1.setText("Buffet sáng");
            tvOption2.setText("Giặt ủi");
            tvOption3.setText("Đưa đón sân bay");

            tvOption4.setVisibility(View.GONE);
            lineOption4.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Tài sản bồi thường");
            tvOption1.setText("Bàn");
            tvOption2.setText("Ghế");
            tvOption3.setText("Tủ đầu giường");

            tvOption4.setVisibility(View.VISIBLE);
            lineOption4.setVisibility(View.VISIBLE);
            tvOption4.setText("Trang trí");
        }

        View.OnClickListener optionClickListener = v -> {
            String selectedItem = ((TextView) v).getText().toString();
            String prefix = isServiceTab ? "dịch vụ" : "tài sản";

            Toast.makeText(getContext(), "Đã thêm " + prefix + " " + selectedItem + " vào phòng " + roomNumber, Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        };

        tvOption1.setOnClickListener(optionClickListener);
        tvOption2.setOnClickListener(optionClickListener);
        tvOption3.setOnClickListener(optionClickListener);
        tvOption4.setOnClickListener(optionClickListener);

        bottomSheetDialog.show();
    }
}