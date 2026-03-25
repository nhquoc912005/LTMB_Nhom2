package com.project_mobile.checkout;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import com.project_mobile.RoomModel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CheckoutFragment extends Fragment {

    private RecyclerView recyclerView;
    private CheckoutAdapter adapter;
    private List<CheckoutBill> billList;
    private TextView tvSelectedDate;
    private LinearLayout llDatePicker;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        llDatePicker = view.findViewById(R.id.llDatePicker);
        recyclerView = view.findViewById(R.id.recyclerViewCheckout);

        View.OnClickListener dateClickListener = v -> showDatePicker();
        if (llDatePicker != null) {
            llDatePicker.setOnClickListener(dateClickListener);
        } else {
            tvSelectedDate.setOnClickListener(dateClickListener);
        }

        setupRecyclerView();

        return view;
    }

    private void showDatePicker() {
        Dialog calendarDialog = new Dialog(requireContext());
        calendarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        calendarDialog.setContentView(R.layout.dialog_custom_calendar);
        if (calendarDialog.getWindow() != null) {
            calendarDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        android.widget.CalendarView calendarView = calendarDialog.findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText(date);
            calendarDialog.dismiss();
        });

        calendarDialog.show();
    }

    private void setupRecyclerView() {
        billList = new ArrayList<>();

        RoomModel room1 = new RoomModel("402", "Standard", "4", "2", "1,800,000", "Đang lưu trú",
                "Phạm Thị D", "0901234789", "");
        billList.add(new CheckoutBill(room1, "phamthid@email.com", "13/02/2026", "16/02/2026", 150000, 1950000));

        RoomModel room2 = new RoomModel("503", "Standard", "5", "2", "1,800,000", "Đang lưu trú",
                "Hoàng Văn E", "0901874567", "");
        billList.add(new CheckoutBill(room2, "hoangvane@email.com", "12/02/2026", "16/02/2026", 0, 1800000));

        RoomModel room3 = new RoomModel("204", "Standard", "2", "2", "1,800,000", "Đang lưu trú",
                "Vũ Hoàng F", "0901234567", "");
        billList.add(new CheckoutBill(room3, "vuhoangf@email.com", "14/02/2026", "16/02/2026", 150000, 1950000));

        RoomModel room4 = new RoomModel("205", "Standard", "2", "2", "1,200,000", "Đang lưu trú",
                "Hoàng Thị B", "0901234987", "");
        billList.add(new CheckoutBill(room4, "hoangthib@email.com", "14/02/2026", "16/02/2026", 0, 1200000));

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CheckoutAdapter(requireContext(), billList, this::showPaymentDialog);
        recyclerView.setAdapter(adapter);
    }

    private void showPaymentDialog(CheckoutBill bill) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_checkout);
        
        setupDialogWindow(dialog);

        TextView tvName = dialog.findViewById(R.id.tvDialogName);
        TextView tvRoom = dialog.findViewById(R.id.tvDialogRoom);
        TextView tvDateRange = dialog.findViewById(R.id.tvDialogDateRange);
        TextView tvTotal = dialog.findViewById(R.id.tvDialogTotal);
        Spinner spinnerPayment = dialog.findViewById(R.id.spinnerPayment);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnRedInvoice = dialog.findViewById(R.id.btnRedInvoice);
        Button btnConfirmPrint = dialog.findViewById(R.id.btnConfirmPrint);

        if (tvName != null) tvName.setText(bill.getRoomModel().getCustomerName());
        if (tvRoom != null) tvRoom.setText("Phòng " + bill.getRoomModel().getRoomNumber());
        if (tvDateRange != null) tvDateRange.setText(bill.getCheckInDate() + " - " + bill.getCheckOutDate());
        if (tvTotal != null) tvTotal.setText(formatter.format(bill.getTotalFee()));

        String[] methods = {"Tiền mặt", "Chuyển khoản"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerPayment.setAdapter(spinnerAdapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnRedInvoice.setOnClickListener(v -> {
            dialog.dismiss();
            completeCheckout(bill);
        });

        btnConfirmPrint.setOnClickListener(v -> {
            dialog.dismiss();
            completeCheckout(bill);
            showSuccessDialog();
        });

        dialog.show();
    }

    private void completeCheckout(CheckoutBill bill) {
        billList.remove(bill);
        adapter.notifyDataSetChanged();
    }

    private void showSuccessDialog() {
        Dialog successDialog = new Dialog(requireContext());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.layout_dialog_success_print);
        
        setupDialogWindow(successDialog);
        successDialog.setCancelable(false);

        Button btnDone = successDialog.findViewById(R.id.btnDone);
        if (btnDone != null) btnDone.setOnClickListener(v -> successDialog.dismiss());

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
            // Updated to 16dp to match items as requested previously
            float marginPx = 16 * displayMetrics.density;
            lp.width = (int) (width - 2 * marginPx);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(lp);
        }
    }
}
