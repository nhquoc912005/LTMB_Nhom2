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
                                if (d == null) {
                                    continue;
                                }
                                if (d != null) {
                                    checkoutList.add(buildCheckoutBill(d));
                                    continue;
                                }
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
        ((TextView)dialog.findViewById(R.id.tvDialogTotal)).setText(formatMoney(bill.getTotalFee()));
        ((TextView)dialog.findViewById(R.id.tvDialogRoomFee)).setText(formatMoney(bill.getRoomFee()));
        ((TextView)dialog.findViewById(R.id.tvDialogServiceFee)).setText(formatMoney(bill.getServiceFee()));
        ((TextView)dialog.findViewById(R.id.tvDialogCheckoutDate)).setText(bill.getCheckOutDate());

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

        android.widget.EditText etNote = dialog.findViewById(R.id.etNote);
        View btnConfirmPrint = dialog.findViewById(R.id.btnConfirmPrint);
        View btnRedInvoice = dialog.findViewById(R.id.btnRedInvoice);

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        if (btnConfirmPrint != null) {
            btnConfirmPrint.setOnClickListener(v -> payCheckout(bill, spinnerPayment, etNote, false, dialog, btnConfirmPrint, btnRedInvoice));
        }
        if (btnRedInvoice != null) {
            btnRedInvoice.setOnClickListener(v -> payCheckout(bill, spinnerPayment, etNote, true, dialog, btnConfirmPrint, btnRedInvoice));
        }
        dialog.show();
    }

    private CheckoutBill buildCheckoutBill(com.project_mobile.network.ApiModels.CheckoutDto d) {
        double roomFee = money(d.roomFee);
        double serviceFee = money(d.serviceFee);
        double damageFee = money(d.damageFee);
        double amountDue = money(d.amountDue);
        return new CheckoutBill(
                new RoomModel(safe(d.roomNames), "Standard", "Táº§ng 1", totalGuests(d) + " ngÆ°á»i", formatMoney(roomFee), "Äang sá»­ dá»¥ng", safe(d.customerName), safe(d.customerPhone), "2 ngÃ y"),
                d.email,
                safe(d.checkinAt),
                safe(d.expectedCheckoutAt),
                serviceFee + damageFee,
                amountDue,
                d.adults != null ? d.adults : 0,
                d.children != null ? d.children : 0,
                d.idLuutru,
                d.idHoaDon,
                d.maDatPhong,
                roomFee,
                damageFee,
                money(d.deposit),
                money(d.grossTotal)
        );
    }

    private void payCheckout(CheckoutBill bill, Spinner spinnerPayment, android.widget.EditText etNote, boolean requestVat, Dialog dialog, View btnConfirmPrint, View btnRedInvoice) {
        if (bill.getMaDatPhong() == null || bill.getMaDatPhong().trim().isEmpty()) {
            android.widget.Toast.makeText(getContext(), "Thiáº¿u mÃ£ Ä‘áº·t phÃ²ng Ä‘á»ƒ thanh toÃ¡n", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        setPaymentButtonsEnabled(btnConfirmPrint, btnRedInvoice, false);
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.createCheckoutDraft(bill.getMaDatPhong()).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.CheckoutDto>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.CheckoutDto>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.CheckoutDto>> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().success || response.body().data == null || response.body().data.idHoaDon == null) {
                    setPaymentButtonsEnabled(btnConfirmPrint, btnRedInvoice, true);
                    String msg = response.body() != null && response.body().message != null ? response.body().message : "KhÃ´ng thá»ƒ táº¡o hÃ³a Ä‘Æ¡n nhÃ¡p";
                    android.widget.Toast.makeText(getContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                com.project_mobile.network.ApiModels.CheckoutDto draft = response.body().data;
                bill.setIdHoaDon(draft.idHoaDon);
                com.project_mobile.network.ApiModels.PaymentRequest req = new com.project_mobile.network.ApiModels.PaymentRequest();
                req.paymentMethod = normalizePaymentMethod(spinnerPayment == null || spinnerPayment.getSelectedItem() == null ? null : spinnerPayment.getSelectedItem().toString());
                req.amount = money(draft.amountDue);
                req.idLuutru = draft.idLuutru != null ? draft.idLuutru : bill.getIdLuutru();
                req.maDatPhong = draft.maDatPhong != null ? draft.maDatPhong : bill.getMaDatPhong();
                req.note = etNote == null ? null : etNote.getText().toString().trim();
                req.requestVat = requestVat;

                api.payInvoice(draft.idHoaDon, req).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<Object>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<Object>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<Object>> response) {
                        setPaymentButtonsEnabled(btnConfirmPrint, btnRedInvoice, true);
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            dialog.dismiss();
                            android.widget.Toast.makeText(getContext(), "Thanh toÃ¡n vÃ  tráº£ phÃ²ng thÃ nh cÃ´ng", android.widget.Toast.LENGTH_SHORT).show();
                            fetchData();
                        } else {
                            String msg = response.body() != null && response.body().message != null ? response.body().message : "Thanh toÃ¡n tháº¥t báº¡i";
                            android.widget.Toast.makeText(getContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<Object>> call, Throwable t) {
                        setPaymentButtonsEnabled(btnConfirmPrint, btnRedInvoice, true);
                        android.widget.Toast.makeText(getContext(), "Lá»—i káº¿t ná»‘i: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.CheckoutDto>> call, Throwable t) {
                setPaymentButtonsEnabled(btnConfirmPrint, btnRedInvoice, true);
                android.widget.Toast.makeText(getContext(), "Lá»—i táº¡o hÃ³a Ä‘Æ¡n: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setPaymentButtonsEnabled(View btnConfirmPrint, View btnRedInvoice, boolean enabled) {
        if (btnConfirmPrint != null) btnConfirmPrint.setEnabled(enabled);
        if (btnRedInvoice != null) btnRedInvoice.setEnabled(enabled);
    }

    private String normalizePaymentMethod(String label) {
        if (System.currentTimeMillis() >= 0) {
            return label != null && label.toLowerCase(java.util.Locale.ROOT).contains("chuy") ? "TRANSFER" : "CASH";
        }
        if (label != null && label.toLowerCase(java.util.Locale.ROOT).contains("chuy")) {
            return "Chuyá»ƒn khoáº£n";
        }
        return "Tiá»n máº·t";
    }

    private int totalGuests(com.project_mobile.network.ApiModels.CheckoutDto dto) {
        if (dto.totalGuests != null) return dto.totalGuests;
        return (dto.adults != null ? dto.adults : 0) + (dto.children != null ? dto.children : 0);
    }

    private double money(Double value) {
        return value == null ? 0d : value;
    }

    private String formatMoney(double value) {
        return String.format(java.util.Locale.US, "%,.0f", value);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
