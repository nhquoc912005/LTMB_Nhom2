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
    private List<CheckoutBill> checkoutList = new ArrayList<>();
    private TextView tvSelectedDate;
    private android.widget.EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewCheckout);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        etSearch = view.findViewById(R.id.etSearch);
        
        adapter = new CheckoutAdapter(requireContext(), checkoutList, this::showPaymentDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.ivCalendarIcon).setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.btnSearch).setOnClickListener(v -> fetchData());

        fetchData();
        return view;
    }

    private void showDatePicker() {
        android.app.Dialog calendarDialog = new android.app.Dialog(requireContext());
        calendarDialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        calendarDialog.setContentView(R.layout.dialog_custom_calendar);
        if (calendarDialog.getWindow() != null) {
            calendarDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        android.widget.CalendarView calendarView = calendarDialog.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvSelectedDate.setText(date);
            calendarDialog.dismiss();
            fetchData(); // Tự động load lại sau khi chọn ngày
        });

        calendarDialog.show();
    }

    private void fetchData() {
        String dateStr = tvSelectedDate.getText().toString();
        String apiDate = formatToApiDate(dateStr);
        String query = etSearch.getText().toString().trim();

        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getCheckouts(apiDate, query).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.CheckoutDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.CheckoutDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.CheckoutDto>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().success) {
                        checkoutList.clear();
                        if (response.body().data != null) {
                            for (com.project_mobile.network.ApiModels.CheckoutDto d : response.body().data) {
                                checkoutList.add(new CheckoutBill(
                                    new RoomModel(d.roomNames, "Standard", "Tầng 1", d.totalGuests + " người", String.valueOf(d.roomFee), "Đang sử dụng", d.customerName, d.customerPhone, "2 ngày"),
                                    d.email, d.checkinAt, d.expectedCheckoutAt, d.serviceFee + d.damageFee, d.amountDue, d.adults, d.children
                                ));
                            }
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                if (checkoutList.isEmpty()) {
                                    android.widget.Toast.makeText(getContext(), "Không có phòng nào đang lưu trú", android.widget.Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> 
                                android.widget.Toast.makeText(getContext(), "Lỗi: " + response.body().message, android.widget.Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            android.widget.Toast.makeText(getContext(), "Lỗi kết nối server: " + response.code(), android.widget.Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.CheckoutDto>>> call, Throwable t) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> 
                        android.widget.Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String formatToApiDate(String uiDate) {
        if (uiDate == null || uiDate.trim().isEmpty()) return null;
        try {
            String[] parts = uiDate.split("/");
            if (parts.length == 3) {
                return parts[2] + "-" + String.format("%02d", Integer.parseInt(parts[1])) + "-" + String.format("%02d", Integer.parseInt(parts[0]));
            }
        } catch (Exception e) {}
        return null;
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
