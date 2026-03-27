package com.project_mobile.dat_phong;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.project_mobile.R;

public class RoomDetailBottomSheet extends BottomSheetDialogFragment {

    private RoomModel room;

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

        // Header
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        tvTitle.setText("Chi tiết phòng " + room.getRoomNumber());
        view.findViewById(R.id.ivClose).setOnClickListener(v -> dismiss());

        // Room Info
        ((TextView) view.findViewById(R.id.tvDetailRoomNumber)).setText(room.getRoomNumber());
        ((TextView) view.findViewById(R.id.tvDetailRoomType)).setText(room.getRoomType());
        ((TextView) view.findViewById(R.id.tvDetailFloor)).setText(room.getFloor());
        ((TextView) view.findViewById(R.id.tvDetailCapacity)).setText(room.getCapacity());
        ((TextView) view.findViewById(R.id.tvDetailPrice)).setText(room.getPrice() + "/đêm");

        // Status Badge
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        tvStatus.setText(room.getStatus());
        updateStatusBadge(tvStatus, room.getStatus());

        // Customer Info (only if IN_USE)
        MaterialCardView cvCustomer = view.findViewById(R.id.cvCustomerInfo);
        if ("Đang sử dụng".equals(room.getStatus())) {
            cvCustomer.setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.tvCustomerName)).setText(" " + room.getCustomerName());
            ((TextView) view.findViewById(R.id.tvCustomerPhone)).setText(" " + room.getCustomerPhone());
            ((TextView) view.findViewById(R.id.tvCustomerDuration)).setText(" " + room.getDuration());
        } else {
            cvCustomer.setVisibility(View.GONE);
        }

        // Actions
        MaterialButton btnPrimary = view.findViewById(R.id.btnPrimaryAction);
        MaterialButton btnSecondary = view.findViewById(R.id.btnSecondaryAction);

        setupActions(btnPrimary, btnSecondary);
    }

    private void updateStatusBadge(TextView tvStatus, String status) {
        int bgColor, textColor;
        switch (status) {
            case "Đang sử dụng":
                bgColor = ContextCompat.getColor(getContext(), R.color.status_in_use_bg);
                textColor = ContextCompat.getColor(getContext(), R.color.status_in_use_text);
                break;
            case "Bảo trì":
                bgColor = ContextCompat.getColor(getContext(), R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(getContext(), R.color.status_maintenance_text);
                break;
            default:
                bgColor = ContextCompat.getColor(getContext(), R.color.status_empty_bg);
                textColor = ContextCompat.getColor(getContext(), R.color.status_empty_text);
                break;
        }
        tvStatus.setTextColor(textColor);
        android.graphics.drawable.GradientDrawable gd = (android.graphics.drawable.GradientDrawable) tvStatus.getBackground();
        gd.setColor(bgColor);
    }

    private void setupActions(MaterialButton primary, MaterialButton secondary) {
        switch (room.getStatus()) {
            case "Trống":
                primary.setText("Đặt phòng");
                primary.setOnClickListener(v -> {
                    dismiss();
                    SuccessDialog.show(getContext(), "Đặt phòng thành công");
                });
                secondary.setText("Chuyển sang bảo trì");
                secondary.setOnClickListener(v -> {
                    dismiss();
                    SuccessDialog.show(getContext(), "Chuyển bảo trì thành công");
                });
                break;
            case "Đang sử dụng":
                primary.setText("Trả phòng");
                primary.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8C7851"))); // Nâu đậm
                primary.setOnClickListener(v -> {
                    dismiss();
                    SuccessDialog.show(getContext(), "Trả phòng thành công");
                });
                secondary.setText("Đổi phòng");
                break;
            case "Bảo trì":
                primary.setText("Hoàn tất bảo trì");
                primary.setOnClickListener(v -> {
                    dismiss();
                    SuccessDialog.show(getContext(), "Bảo trì thành công");
                });
                secondary.setText("Chuyển sang bảo trì"); // Thêm option khác nếu cần
                break;
        }
    }
}
