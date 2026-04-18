package com.project_mobile.checkout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.project_mobile.R;
import com.project_mobile.Quan_ly_phong.RoomModel;
import com.project_mobile.api.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CheckoutFragment extends Fragment {
        private RecyclerView recyclerView;
        private CheckoutAdapter adapter;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                        @Nullable Bundle savedInstanceState) {
                View view = inflater.inflate(R.layout.fragment_checkout, container, false);
                recyclerView = view.findViewById(R.id.recyclerViewCheckout);
                setupData();
                return view;
        }

        private void setupData() {
                List<CheckoutBill> list = new ArrayList<>();

                list.add(new CheckoutBill(
                                new RoomModel("402", "Deluxe", "Tầng 4", "2 người", "650,000đ", "Đang sử dụng",
                                                "Phạm Thị D", "0901234789", "3 ngày"),
                                "phamthid@email.com", "13/02/2026", "16/02/2026", 150000, 1950000, 2, 0));

                list.add(new CheckoutBill(
                                new RoomModel("503", "Standard", "Tầng 5", "2 người", "450,000đ", "Đang sử dụng",
                                                "Hoàng Văn E", "0901874567", "4 ngày"),
                                "hoangvane@email.com", "12/02/2026", "16/02/2026", 0, 1800000, 2, 1));

                list.add(new CheckoutBill(
                                new RoomModel("204", "Deluxe", "Tầng 2", "2 người", "650,000đ", "Đang sử dụng",
                                                "Vũ Hoàng F", "0901234567", "3 ngày"),
                                "vuhoangf@email.com", "14/02/2026", "16/02/2026", 150000, 1950000, 2, 0));

                list.add(new CheckoutBill(
                                new RoomModel("302", "Standard", "Tầng 3", "2 người", "450,000đ", "Đang sử dụng",
                                                "Hoàng Thị B", "0901234987", "2 ngày"),
                                "hoangthib@email.com", "14/02/2026", "16/02/2026", 0, 1200000, 2, 1));

                adapter = new CheckoutAdapter(requireContext(), list, this::showPaymentDialog);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(adapter);
        }

        private void showPaymentDialog(CheckoutBill bill) {
                Dialog dialog = new Dialog(requireContext());
                dialog.setContentView(R.layout.dialog_payment_checkout);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                ((TextView) dialog.findViewById(R.id.tvDialogName)).setText(bill.getRoomModel().getCustomerName());
                ((TextView) dialog.findViewById(R.id.tvDialogRoom))
                                .setText("Phòng " + bill.getRoomModel().getRoomNumber());
                ((TextView) dialog.findViewById(R.id.tvDialogDateRange))
                                .setText(bill.getCheckInDate() + " - " + bill.getCheckOutDate());
                ((TextView) dialog.findViewById(R.id.tvDialogTotalGuests))
                                .setText("Số người: " + bill.getTotalGuests());
                ((TextView) dialog.findViewById(R.id.tvDialogGuestDetails)).setText(bill.getAdults() + " người lớn"
                                + (bill.getChildren() > 0 ? ", " + bill.getChildren() + " trẻ em" : ""));
                ((TextView) dialog.findViewById(R.id.tvDialogTotal))
                                .setText(String.format("%,.0f", bill.getTotalFee()));
                ((TextView) dialog.findViewById(R.id.tvDialogRoomFee)).setText(bill.getRoomModel().getPrice());
                ((TextView) dialog.findViewById(R.id.tvDialogServiceFee))
                                .setText(String.format("%,.0f", bill.getServiceFee()));

                // Khởi tạo Spinner
                Spinner spinnerPayment = dialog.findViewById(R.id.spinnerPayment);
                String[] paymentMethods = { "Tiền mặt", "Chuyển khoản" };
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                paymentMethods);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPayment.setAdapter(spinnerAdapter);

                // Khai báo Retrofit Builder (Tạm thời anh thay URL bằng IP máy tính của anh)
                Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://192.168.x.x:3000") // TODO: Điền IPv4 thật của Server anh đang chạy
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                ApiService apiService = retrofit.create(ApiService.class);

                // Nút Xác Nhận Thanh Toán
                Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
                btnConfirm.setOnClickListener(v -> {

                        // Xây dựng gói dữ liệu gửi lên API
                        JsonObject paymentData = new JsonObject();
                        String ptThanhToan = spinnerPayment.getSelectedItem().toString().equals("Tiền mặt") ? "CASH"
                                        : "TRANSFER";
                        paymentData.addProperty("phuong_thuc", ptThanhToan);
                        paymentData.addProperty("so_tien", bill.getTotalFee()); // Tổng tiền thanh toán

                        int idHoaDon = 1; // TODO: Cần truyền id_hoadon thực tế nhận được từ API Lập Hóa Đơn

                        // Gọi API Thanh Toán
                        apiService.payInvoice(idHoaDon, paymentData).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        if (response.isSuccessful()) {
                                                Toast.makeText(requireContext(), "Thanh toán thành công!",
                                                                Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                        } else {
                                                Toast.makeText(requireContext(),
                                                                "Lỗi khi thanh toán: " + response.code(),
                                                                Toast.LENGTH_SHORT).show();
                                        }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                        Toast.makeText(requireContext(), "Mất kết nối server!", Toast.LENGTH_SHORT)
                                                        .show();
                                }
                        });
                });

                // Nút Hủy
                dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
                dialog.show();
        }
}
