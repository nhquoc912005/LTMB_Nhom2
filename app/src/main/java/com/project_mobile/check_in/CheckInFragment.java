package com.project_mobile.check_in;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.ArrayList;
import java.util.List;

public class CheckInFragment extends Fragment {

    private RecyclerView rvCheckIn;
    private CheckInAdapter adapter;
    private List<CheckInModel> checkInList;
    private TextView tvCheckInDate, tvCheckOutDate;
    private LinearLayout llCheckInDate, llCheckOutDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin, container, false);

        rvCheckIn = view.findViewById(R.id.rvCheckIn);
        tvCheckInDate = view.findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = view.findViewById(R.id.tvCheckOutDate);
        llCheckInDate = view.findViewById(R.id.llCheckInDate);
        llCheckOutDate = view.findViewById(R.id.llCheckOutDate);

        llCheckInDate.setOnClickListener(v -> showDatePicker(tvCheckInDate));
        llCheckOutDate.setOnClickListener(v -> showDatePicker(tvCheckOutDate));

        rvCheckIn.setLayoutManager(new LinearLayoutManager(getContext()));

        checkInList = new ArrayList<>();
        // Dữ liệu mẫu theo hình ảnh
        checkInList.add(new CheckInModel("Nguyễn Văn A", "Phòng 101", "0901234567", "nguyenvana@email.com", "14/02/2026 - 16/02/2026"));
        checkInList.add(new CheckInModel("Nguyễn Văn B", "Phòng 102", "0901234568", "nguyenvanb@email.com", "14/02/2026 - 16/02/2026"));
        checkInList.add(new CheckInModel("Nguyễn Văn C", "Phòng 103", "0901234569", "nguyenvanc@email.com", "14/02/2026 - 16/02/2026"));

        adapter = new CheckInAdapter(checkInList, new CheckInAdapter.OnCheckInClickListener() {
            @Override
            public void onCheckInClick(CheckInModel item) {
                showConfirmCheckInDialog(item);
            }

            @Override
            public void onChangeRoomClick(CheckInModel item) {
                showChangeRoomDialog(item);
            }
        });
        rvCheckIn.setAdapter(adapter);

        return view;
    }

    private void showDatePicker(TextView targetTextView) {
        Dialog calendarDialog = new Dialog(requireContext());
        calendarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        calendarDialog.setContentView(R.layout.dialog_custom_calendar);
        if (calendarDialog.getWindow() != null) {
            calendarDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        CalendarView calendarView = calendarDialog.findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            targetTextView.setText(date);
            calendarDialog.dismiss();
        });

        calendarDialog.show();
    }

    private void showConfirmCheckInDialog(CheckInModel item) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_checkin);
        
        setupDialogWindow(dialog);

        TextView tvName = dialog.findViewById(R.id.tvGuestName);
        TextView tvRoom = dialog.findViewById(R.id.tvRoomNumber);
        TextView tvIn = dialog.findViewById(R.id.tvCheckInDate);
        TextView tvOut = dialog.findViewById(R.id.tvCheckOutDate);
        TextView tvClose = dialog.findViewById(R.id.tvClose);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        if (tvName != null) tvName.setText(item.getGuestName());
        if (tvRoom != null) tvRoom.setText(item.getRoomNumber());
        
        String[] dates = item.getStayPeriod().split(" - ");
        if (dates.length == 2) {
            if (tvIn != null) tvIn.setText(dates[0]);
            if (tvOut != null) tvOut.setText(dates[1]);
        }

        if (tvClose != null) tvClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                showSuccessDialog(item, "Nhận phòng thành công");
            });
        }

        dialog.show();
    }

    private void showChangeRoomDialog(CheckInModel item) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_room);
        
        setupDialogWindow(dialog);

        TextView tvName = dialog.findViewById(R.id.tvGuestName);
        TextView tvCurrentRoom = dialog.findViewById(R.id.tvCurrentRoom);
        TextView tvStayPeriod = dialog.findViewById(R.id.tvStayPeriod);
        TextView tvClose = dialog.findViewById(R.id.tvClose);
        Spinner spinnerRooms = dialog.findViewById(R.id.spinnerRooms);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        if (tvName != null) tvName.setText(item.getGuestName());
        if (tvCurrentRoom != null) tvCurrentRoom.setText("Phòng hiện tại: " + item.getRoomNumber());
        if (tvStayPeriod != null) tvStayPeriod.setText(item.getStayPeriod());

        // Fake data cho spinner
        String[] emptyRooms = {"-- Chọn phòng --", "Phòng 104", "Phòng 105", "Phòng 206"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, emptyRooms);
        if (spinnerRooms != null) spinnerRooms.setAdapter(spinnerAdapter);

        if (tvClose != null) tvClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                showSuccessDialog(item, "Đổi phòng thành công");
            });
        }

        dialog.show();
    }

    private void showSuccessDialog(CheckInModel item, String message) {
        Dialog successDialog = new Dialog(requireContext());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.dialog_checkin_success);
        
        setupDialogWindow(successDialog);

        TextView tvMsg = successDialog.findViewById(R.id.tvSuccessMessage);
        if (tvMsg != null) tvMsg.setText(message);

        Button btnDone = successDialog.findViewById(R.id.btnDone);
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> {
                successDialog.dismiss();
                if (message.contains("Nhận phòng")) {
                    checkInList.remove(item);
                    adapter.notifyDataSetChanged();
                }
            });
        }

        successDialog.show();
    }

    private void setupDialogWindow(Dialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            float marginPx = 16 * displayMetrics.density;
            lp.width = (int) (width - 2 * marginPx);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(lp);
        }
    }
}
