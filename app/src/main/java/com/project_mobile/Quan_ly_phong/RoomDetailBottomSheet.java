// Module quản lý phòng Android.
// File này là bottom sheet chi tiết phòng, cho phép trả phòng, đổi phòng hoặc chuyển bảo trì.
// Dữ liệu chính là RoomModel hiện tại và API cập nhật trạng thái phòng.
/*
 * File: RoomDetailBottomSheet.java
 * Module: Quản lý phòng.
 *
 * Chức năng:
 * - Hiển thị thông tin chi tiết của một phòng được chọn từ RoomManagementFragment.
 * - Tùy trạng thái phòng, hiển thị các hành động như cập nhật trạng thái, trả phòng hoặc đổi phòng.
 * - Gọi API cập nhật trạng thái phòng khi cần.
 *
 * Lưu ý:
 * - Luồng dịch vụ/tài sản theo phòng đang lưu trú nằm ở RoomMapFragment, không nằm trong file này.
 */
package com.project_mobile.Quan_ly_phong;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.project_mobile.MainActivity;
import com.project_mobile.R;
import com.project_mobile.common.AppDialog;
import com.project_mobile.network.ApiClient;
import com.project_mobile.network.ApiModels;
import com.project_mobile.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * RoomDetailBottomSheet hiển thị chi tiết một phòng được chọn trong lưới.
 * Class này quyết định action theo trạng thái phòng và gọi API khi cập nhật trạng thái.
 */
public class RoomDetailBottomSheet extends BottomSheetDialogFragment {

    private RoomModel room;
    private List<RoomModel> rooms = new ArrayList<>();
    private OnRoomsChangedListener onRoomsChangedListener;
    private ApiService api;

    public interface OnRoomsChangedListener {
        void onRoomsChanged();
    }

    public void setRooms(List<RoomModel> rooms) {
        this.rooms = rooms;
    }

    public void setOnRoomsChangedListener(OnRoomsChangedListener listener) {
        this.onRoomsChangedListener = listener;
    }

    public static RoomDetailBottomSheet newInstance(RoomModel room) {
        RoomDetailBottomSheet fragment = new RoomDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("room", room);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            room = (RoomModel) getArguments().getSerializable("room");
        }
        api = ApiClient.getClient().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_room_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        room = findLiveRoom(room);

        ((TextView) view.findViewById(R.id.tvDetailTitle)).setText("Chi tiết phòng " + room.getRoomNumber());
        view.findViewById(R.id.ivClose).setOnClickListener(v -> dismiss());

        ((TextView) view.findViewById(R.id.tvDetailRoomNumber)).setText(room.getRoomNumber());
        ((TextView) view.findViewById(R.id.tvDetailRoomType)).setText(room.getRoomType());
        ((TextView) view.findViewById(R.id.tvDetailFloor)).setText(room.getFloor());
        ((TextView) view.findViewById(R.id.tvDetailCapacity)).setText(room.getCapacity());
        ((TextView) view.findViewById(R.id.tvDetailPrice)).setText(room.getPrice() + "/đêm");

        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        tvStatus.setText(room.getStatus());
        updateStatusBadge(tvStatus);

        bindCustomerInfo(view);

        MaterialButton btnPrimary = view.findViewById(R.id.btnPrimaryAction);
        MaterialButton btnSecondary = view.findViewById(R.id.btnSecondaryAction);
        MaterialButton btnTertiary = view.findViewById(R.id.btnTertiaryAction);
        setupActions(btnPrimary, btnSecondary, btnTertiary);
    }

    /** Chỉ hiển thị thông tin khách khi phòng đang có khách/lưu trú. */
    /*
     * Hiển thị thông tin khách nếu phòng đang có khách.
     * Nếu phòng trống/bảo trì, các field khách sẽ dùng giá trị rỗng hoặc fallback từ RoomModel.
     */
    private void bindCustomerInfo(View view) {
        MaterialCardView cvCustomer = view.findViewById(R.id.cvCustomerInfo);
        if (room.isOccupied()) {
            cvCustomer.setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.tvCustomerName)).setText(" " + safe(room.getCustomerName()));
            ((TextView) view.findViewById(R.id.tvCustomerPhone)).setText(" " + safe(room.getCustomerPhone()));
            ((TextView) view.findViewById(R.id.tvCustomerDuration)).setText(" " + safe(room.getDuration()));
        } else {
            cvCustomer.setVisibility(View.GONE);
        }
    }

    /** Cập nhật màu badge theo trạng thái phòng hiện tại. */
    private void updateStatusBadge(TextView tvStatus) {
        int bgColor;
        int textColor = Color.parseColor("#C0410D");
        if (room.isEmpty()) {
            bgColor = Color.parseColor("#F5F1EA");
            textColor = Color.parseColor("#C58959");
        } else if (room.isMaintenance()) {
            bgColor = Color.parseColor("#FFEBD2");
        } else {
            bgColor = Color.parseColor("#FFF4E8");
        }
        tvStatus.setTextColor(textColor);
        GradientDrawable gd = (GradientDrawable) tvStatus.getBackground().mutate();
        gd.setColor(bgColor);
        tvStatus.setBackground(gd);
    }

    /** Thiết lập nút nghiệp vụ theo trạng thái: trả phòng/đổi phòng/bảo trì/hoàn tất bảo trì. */
    /*
     * Cấu hình các nút hành động theo trạng thái phòng.
     * Trạng thái phòng quyết định người dùng được đổi trạng thái, mở checkout hay đổi phòng.
     */
    private void setupActions(MaterialButton primary, MaterialButton secondary, MaterialButton tertiary) {
        primary.setVisibility(View.GONE);
        secondary.setVisibility(View.GONE);
        tertiary.setVisibility(View.GONE);

        // Phòng đang có khách: trả phòng phải chuyển sang luồng checkout; đổi phòng mở bottom sheet riêng.
        if (room.isOccupied()) {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Trả phòng");
            primary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9A7E5D")));
            primary.setOnClickListener(v -> AppDialog.showConfirm(
                    requireContext(),
                    "Trả phòng",
                    "Vui lòng thanh toán trước khi trả phòng " + room.getRoomNumber() + ".",
                    "Mở màn hình trả phòng",
                    false,
                    this::openCheckoutFlow
            ));

            secondary.setVisibility(View.VISIBLE);
            secondary.setText("Đổi phòng");
            secondary.setOnClickListener(v -> openChangeRoomSheet());

            tertiary.setVisibility(View.VISIBLE);
            tertiary.setText("Chuyển sang bảo trì");
            tertiary.setOnClickListener(v -> AppDialog.showConfirm(
                    requireContext(),
                    "Chuyển sang bảo trì",
                    "Phòng đang có khách. Bạn có chắc chắn muốn chuyển phòng này sang bảo trì?",
                    "Xác nhận",
                    true,
                    () -> updateRoomStatusOnServer(RoomModel.STATUS_MAINTENANCE, "Chuyển bảo trì thành công")
            ));
        } else if (room.isMaintenance()) {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Hoàn tất bảo trì");
            primary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1C19F")));
            primary.setOnClickListener(v -> updateRoomStatusOnServer(RoomModel.STATUS_EMPTY, "Bảo trì thành công"));
        } else {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Chuyển sang bảo trì");
            primary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1C19F")));
            primary.setOnClickListener(v -> updateRoomStatusOnServer(RoomModel.STATUS_MAINTENANCE, "Chuyển bảo trì thành công"));
        }
    }

    /** Gửi trạng thái phòng mới lên backend và refresh danh sách phòng sau khi thành công. */
    /*
     * Gửi trạng thái phòng mới lên backend.
     * API: PUT /api/rooms/{id}/status.
     * Thành công sẽ gọi callback để RoomManagementFragment tải lại danh sách phòng.
     */
    private void updateRoomStatusOnServer(String status, String successMessage) {
        AppDialog.showLoading(requireContext());
        ApiModels.StatusRequest req = new ApiModels.StatusRequest();
        req.status = status;
        api.updateRoomStatus(room.getId(), req).enqueue(new Callback<ApiModels.ApiResponse<ApiModels.RoomDto>>() {
            @Override
            public void onResponse(Call<ApiModels.ApiResponse<ApiModels.RoomDto>> call, Response<ApiModels.ApiResponse<ApiModels.RoomDto>> response) {
                AppDialog.hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    room.setStatus(status);
                    if (status.equals(RoomModel.STATUS_MAINTENANCE)) {
                        room.clearCustomer();
                    }
                    notifyRoomsChanged();
                    dismiss();
                    AppDialog.showSuccess(requireContext(), successMessage);
                } else {
                    AppDialog.showError(requireContext(), "Lỗi khi cập nhật trạng thái phòng");
                }
            }

            @Override
            public void onFailure(Call<ApiModels.ApiResponse<ApiModels.RoomDto>> call, Throwable t) {
                AppDialog.hideLoading();
                AppDialog.showError(requireContext(), "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /** Điều hướng người dùng sang tab Trả phòng để thanh toán trước khi đóng lưu trú. */
    private void openCheckoutFlow() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openStay(false);
            dismiss();
        } else {
            AppDialog.showError(requireContext(), "Vui lòng vào màn hình Trả phòng để thanh toán.");
        }
    }

    /** Mở bottom sheet đổi phòng, sau khi đổi xong sẽ báo Fragment cha reload dữ liệu. */
    private void openChangeRoomSheet() {
        ChangeRoomBottomSheet changeRoomBottomSheet = ChangeRoomBottomSheet.newInstance(room);
        changeRoomBottomSheet.setRooms(rooms);
        changeRoomBottomSheet.setOnRoomChangedListener(() -> {
            notifyRoomsChanged();
            dismiss();
        });
        changeRoomBottomSheet.show(getParentFragmentManager(), "ChangeRoomBottomSheet");
    }

    private void notifyRoomsChanged() {
        if (onRoomsChangedListener != null) {
            onRoomsChangedListener.onRoomsChanged();
        }
    }

    /** Tìm object RoomModel mới nhất trong danh sách hiện tại để tránh dùng snapshot cũ. */
    private RoomModel findLiveRoom(RoomModel fallback) {
        if (fallback == null) {
            return null;
        }
        for (RoomModel item : rooms) {
            if (item.getRoomNumber().equals(fallback.getRoomNumber())) {
                return item;
            }
        }
        return fallback;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
