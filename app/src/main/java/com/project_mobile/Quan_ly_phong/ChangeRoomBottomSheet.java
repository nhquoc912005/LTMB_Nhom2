package com.project_mobile.Quan_ly_phong;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.project_mobile.R;
import com.project_mobile.common.AppDialog;

import java.util.ArrayList;
import java.util.List;

public class ChangeRoomBottomSheet extends BottomSheetDialogFragment {

    private RoomModel currentRoom;
    private List<RoomModel> rooms = new ArrayList<>();
    private List<RoomModel> emptyRooms = new ArrayList<>();
    private RoomModel selectedRoom;
    private OnRoomChangedListener listener;

    public interface OnRoomChangedListener {
        void onRoomChanged();
    }

    public static ChangeRoomBottomSheet newInstance(RoomModel room) {
        ChangeRoomBottomSheet fragment = new ChangeRoomBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("room", room);
        fragment.setArguments(args);
        return fragment;
    }

    public void setRooms(List<RoomModel> rooms) {
        this.rooms = rooms;
    }

    public void setOnRoomChangedListener(OnRoomChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentRoom = (RoomModel) getArguments().getSerializable("room");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_change_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentRoom = findLiveRoom(currentRoom);
        buildEmptyRoomList();

        ((TextView) view.findViewById(R.id.tvChangeRoomTitle)).setText("Đổi phòng cho " + safe(currentRoom.getCustomerName()));
        ((TextView) view.findViewById(R.id.tvCurrentCustomer)).setText(" " + safe(currentRoom.getCustomerName()));
        ((TextView) view.findViewById(R.id.tvCurrentRoom)).setText(" Phòng hiện tại: " + currentRoom.getRoomNumber() + " (" + currentRoom.getRoomType() + ")");
        ((TextView) view.findViewById(R.id.tvCurrentDuration)).setText(" " + safe(currentRoom.getDuration()));

        MaterialCardView cvNewRoomInfo = view.findViewById(R.id.cvNewRoomInfo);
        Spinner spEmptyRooms = view.findViewById(R.id.spEmptyRooms);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, buildRoomLabels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEmptyRooms.setAdapter(adapter);
        spEmptyRooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                selectedRoom = position >= 0 && position < emptyRooms.size() ? emptyRooms.get(position) : null;
                bindNewRoomInfo(view, cvNewRoomInfo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRoom = null;
                cvNewRoomInfo.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.btnCloseChangeRoom).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnCancelChangeRoom).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnConfirmChangeRoom).setOnClickListener(v -> confirmChangeRoom());
    }

    private void confirmChangeRoom() {
        if (selectedRoom == null) {
            AppDialog.showError(requireContext(), "Vui lòng chọn phòng trống để đổi.");
            return;
        }

        String oldStatus = currentRoom.getStatus();
        selectedRoom.copyCustomerFrom(currentRoom);
        selectedRoom.setStatus(oldStatus);
        currentRoom.clearCustomer();
        currentRoom.setStatus(RoomModel.STATUS_EMPTY);

        if (listener != null) {
            listener.onRoomChanged();
        }
        dismiss();
        AppDialog.showSuccess(requireContext(), "Đổi phòng thành công");
    }

    private void bindNewRoomInfo(View view, MaterialCardView card) {
        if (selectedRoom == null) {
            card.setVisibility(View.GONE);
            return;
        }
        card.setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.tvNewRoomNumber)).setText(selectedRoom.getRoomNumber());
        ((TextView) view.findViewById(R.id.tvNewRoomType)).setText(selectedRoom.getRoomType());
        ((TextView) view.findViewById(R.id.tvNewRoomFloor)).setText(selectedRoom.getFloor());
        ((TextView) view.findViewById(R.id.tvNewRoomCapacity)).setText(selectedRoom.getCapacity());
        ((TextView) view.findViewById(R.id.tvNewRoomPrice)).setText(selectedRoom.getPrice() + "/đêm");
    }

    private void buildEmptyRoomList() {
        emptyRooms.clear();
        for (RoomModel room : rooms) {
            if (room.isEmpty() && !room.getRoomNumber().equals(currentRoom.getRoomNumber())) {
                emptyRooms.add(room);
            }
        }
    }

    private List<String> buildRoomLabels() {
        List<String> labels = new ArrayList<>();
        for (RoomModel room : emptyRooms) {
            labels.add("Phòng " + room.getRoomNumber() + " - " + room.getRoomType() + " (" + room.getPrice() + "/đêm)");
        }
        if (labels.isEmpty()) {
            labels.add("--Không có phòng trống--");
        }
        return labels;
    }

    private RoomModel findLiveRoom(RoomModel fallback) {
        if (fallback == null) {
            return null;
        }
        for (RoomModel room : rooms) {
            if (room.getRoomNumber().equals(fallback.getRoomNumber())) {
                return room;
            }
        }
        return fallback;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
