package com.project_mobile.checkout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // Đã thêm thư viện này cho Spinner
import android.widget.Spinner;      // Đã thêm thư viện này cho Spinner
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import com.project_mobile.Quan_ly_phong.RoomModel;
import java.util.ArrayList;
import java.util.List;

public class CheckoutFragment extends Fragment {
    private RecyclerView recyclerView;
    private CheckoutAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewCheckout);
        setupData();
        return view;
    }

    private void setupData() {
        List<CheckoutBill> list = new ArrayList<>();
        // RoomModel constructor: String roomNumber, String roomType, String floor, String capacity, String price, String status, String customerName, String customerPhone, String duration

        list.add(new CheckoutBill(
                new RoomModel("402", "Deluxe", "Tầng 4", "2 người", "650,000đ", "Đang sử dụng", "Phạm Thị D", "0901234789", "3 ngày"),
                "phamthid@email.com", "13/02/2026", "16/02/2026", 150000, 1950000, 2, 0));

        list.add(new CheckoutBill(
                new RoomModel("503", "Standard", "Tầng 5", "2 người", "450,000đ", "Đang sử dụng", "Hoàng Văn E", "0901874567", "4 ngày"),
                "hoangvane@email.com", "12/02/2026", "16/02/2026", 0, 1800000, 2, 1));

        list.add(new CheckoutBill(
                new RoomModel("204", "Deluxe", "Tầng 2", "2 người", "650,000đ", "Đang sử dụng", "Vũ Hoàng F", "0901234567", "3 ngày"),
                "vuhoangf@email.com", "14/02/2026", "16/02/2026", 150000, 1950000, 2, 0));

        list.add(new CheckoutBill(
                new RoomModel("302", "Standard", "Tầng 3", "2 người", "450,000đ", "Đang sử dụng", "Hoàng Thị B", "0901234987", "2 ngày"),
                "hoangthib@email.com", "14/02/2026", "16/02/2026", 0, 1200000, 2, 1));

        adapter = new CheckoutAdapter(requireContext(), list, this::showPaymentDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void showPaymentDialog(CheckoutBill bill) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_payment_checkout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ((TextView)dialog.findViewById(R.id.tvDialogName)).setText(bill.getRoomModel().getCustomerName());
        ((TextView)dialog.findViewById(R.id.tvDialogRoom)).setText("Phòng " + bill.getRoomModel().getRoomNumber());
        ((TextView)dialog.findViewById(R.id.tvDialogDateRange)).setText(bill.getCheckInDate() + " - " + bill.getCheckOutDate());
        ((TextView)dialog.findViewById(R.id.tvDialogTotalGuests)).setText("Số người: " + bill.getTotalGuests());
        ((TextView)dialog.findViewById(R.id.tvDialogGuestDetails)).setText(bill.getAdults() + " người lớn" + (bill.getChildren() > 0 ? ", " + bill.getChildren() + " trẻ em" : ""));
        ((TextView)dialog.findViewById(R.id.tvDialogTotal)).setText(String.format("%,.0f", bill.getTotalFee()));
        ((TextView)dialog.findViewById(R.id.tvDialogRoomFee)).setText(bill.getRoomModel().getPrice());
        ((TextView)dialog.findViewById(R.id.tvDialogServiceFee)).setText(String.format("%,.0f", bill.getServiceFee()));

        // ---> ĐOẠN XỬ LÝ CHỌN PHƯƠNG THỨC THANH TOÁN (MỚI) <---
        Spinner spinnerPayment = dialog.findViewById(R.id.spinnerPayment);
        String[] paymentMethods = {"Tiền mặt", "Chuyển khoản"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                paymentMethods
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayment.setAdapter(spinnerAdapter);
        // --------------------------------------------------------

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
