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
import com.project_mobile.R;
import com.project_mobile.common.AppDialog;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailBottomSheet extends BottomSheetDialogFragment {

    private RoomModel room;
    private List<RoomModel> rooms = new ArrayList<>();
    private OnRoomsChangedListener onRoomsChangedListener;

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

    private void setupActions(MaterialButton primary, MaterialButton secondary, MaterialButton tertiary) {
        primary.setVisibility(View.GONE);
        secondary.setVisibility(View.GONE);
        tertiary.setVisibility(View.GONE);

        if (room.isOccupied()) {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Trả phòng");
            primary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9A7E5D")));
            primary.setOnClickListener(v -> AppDialog.showConfirm(
                    requireContext(),
                    "Trả phòng",
                    "Xác nhận trả phòng " + room.getRoomNumber() + "?",
                    "Trả phòng",
                    true,
                    () -> {
                        room.setStatus(RoomModel.STATUS_EMPTY);
                        room.clearCustomer();
                        notifyRoomsChanged();
                        dismiss();
                        AppDialog.showSuccess(requireContext(), "Trả phòng thành công");
                    }
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
                    () -> {
                        room.setStatus(RoomModel.STATUS_MAINTENANCE);
                        room.clearCustomer();
                        notifyRoomsChanged();
                        dismiss();
                        AppDialog.showSuccess(requireContext(), "Chuyển bảo trì thành công");
                    }
            ));
        } else if (room.isMaintenance()) {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Hoàn tất bảo trì");
            primary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1C19F")));
            primary.setOnClickListener(v -> {
                room.setStatus(RoomModel.STATUS_EMPTY);
                notifyRoomsChanged();
                dismiss();
                AppDialog.showSuccess(requireContext(), "Bảo trì thành công");
            });
        } else {
            primary.setVisibility(View.VISIBLE);
            primary.setText("Chuyển sang bảo trì");
            primary.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D1C19F")));
            primary.setOnClickListener(v -> {
                room.setStatus(RoomModel.STATUS_MAINTENANCE);
                notifyRoomsChanged();
                dismiss();
                AppDialog.showSuccess(requireContext(), "Chuyển bảo trì thành công");
            });
        }
    }

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
