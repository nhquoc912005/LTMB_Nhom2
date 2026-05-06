// Module nhận phòng Android.
// File này hiển thị danh sách đơn đang chờ nhận phòng, xác nhận check-in và đổi phòng trước khi nhận.
// Dữ liệu chính lấy từ /api/check-in/bookings, /available-rooms, /confirm và /change-room.
/*
 * File: CheckInFragment.java
 * Module: Nhận phòng.
 *
 * Luồng chính:
 * 1. Tải các booking đủ điều kiện nhận phòng từ GET /api/check-in/bookings.
 * 2. Map BookingDto sang CheckInModel để hiển thị trong RecyclerView.
 * 3. Người dùng có thể xác nhận nhận phòng hoặc đổi phòng trước khi nhận.
 * 4. Xác nhận nhận phòng gọi POST /api/check-in/bookings/{maDatPhong}/confirm.
 * 5. Đổi phòng gọi POST /api/check-in/bookings/{maDatPhong}/change-room.
 *
 * Dữ liệu quan trọng:
 * - BookingDto.rooms chứa id phòng hiện tại, dùng khi đổi phòng.
 * - CheckInRequest chứa CCCD và ghi chú để backend tạo bản ghi lưu trú.
 */
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
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CheckInFragment phụ trách màn Nhận phòng trong tab Lưu trú.
 * Class này lọc đặt phòng theo ngày/từ khóa, map BookingDto sang CheckInModel và gọi API nghiệp vụ check-in.
 */
public class CheckInFragment extends Fragment {

    private RecyclerView rvCheckIn;
    private CheckInAdapter adapter;
    private List<CheckInModel> checkInList;
    private TextView tvCheckInDate, tvCheckOutDate;
    private LinearLayout llCheckInDate, llCheckOutDate;

    /** Lấy danh sách đơn đủ điều kiện nhận phòng theo khoảng ngày và từ khóa tìm kiếm, sau đó cập nhật RecyclerView. */
    /*
     * Gọi API lấy danh sách booking chờ nhận phòng.
     *
     * Input từ UI:
     * - Ngày nhận, ngày trả được chuyển từ dd/MM/yyyy sang yyyy-MM-dd.
     * - Từ khóa tìm kiếm lấy từ ô tìm kiếm.
     *
     * Output:
     * - checkInList được làm mới bằng CheckInModel.
     * - Adapter được notify để cập nhật RecyclerView.
     */
    private void loadData(View rootView) {
        if (tvCheckInDate == null || tvCheckOutDate == null) return;
        
        String fromDate = formatToApiDate(tvCheckInDate.getText().toString());
        String toDate = formatToApiDate(tvCheckOutDate.getText().toString());
        String query = "";
        
        View parentView = rootView != null ? rootView : getView();
        if (parentView != null) {
            android.widget.EditText etSearch = parentView.findViewById(R.id.etSearch);
            if (etSearch != null) query = etSearch.getText().toString();
        }

        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getCheckInBookings(fromDate, toDate, query).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.BookingDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.BookingDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.BookingDto>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().success) {
                        checkInList.clear();
                        List<com.project_mobile.network.ApiModels.BookingDto> data = response.body().data;
                        if (data != null) {
                            // Map dữ liệu API sang model màn hình; firstRoomId giữ id phòng hiện tại để đổi phòng.
                            for (com.project_mobile.network.ApiModels.BookingDto b : data) {
                                // BookingDto từ API có nhiều alias field; CheckInModel chỉ giữ các field cần cho card và dialog.
                                checkInList.add(new CheckInModel(
                                    b.bookingId,
                                    safeText(b.customerName, "Khách vãng lai"),
                                    safeText(b.roomNumber, "N/A"),
                                    b.phone,
                                    b.email,
                                    buildStayPeriod(b),
                                    b.totalGuests != null ? b.totalGuests : 0,
                                    b.adults != null ? b.adults : 0,
                                    b.children != null ? b.children : 0,
                                    firstRoomId(b)
                                ));
                            }
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                if (checkInList.isEmpty()) {
                                    android.widget.Toast.makeText(getContext(), "Không tìm thấy đơn đặt phòng nào", android.widget.Toast.LENGTH_SHORT).show();
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
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.BookingDto>>> call, Throwable t) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> 
                        android.widget.Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /** Chuyển ngày dạng dd/MM/yyyy từ UI sang yyyy-MM-dd để gửi query API. */
    private String formatToApiDate(String uiDate) {
        if (uiDate == null || uiDate.trim().isEmpty()) return null;
        try {
            String[] parts = uiDate.split("/");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                return String.format("%04d-%02d-%02d", year, month, day);
            }
        } catch (Exception e) {}
        return null;
    }

    /** Ưu tiên stayPeriod từ backend, nếu thiếu thì ghép ngày nhận - ngày trả. */
    private String buildStayPeriod(com.project_mobile.network.ApiModels.BookingDto booking) {
        if (booking == null) return "";
        if (booking.stayPeriod != null && !booking.stayPeriod.trim().isEmpty()) {
            return booking.stayPeriod;
        }
        String checkIn = booking.checkIn != null ? booking.checkIn.trim() : "";
        String checkOut = booking.checkOut != null ? booking.checkOut.trim() : "";
        if (!checkIn.isEmpty() && !checkOut.isEmpty()) {
            return checkIn + " - " + checkOut;
        }
        return !checkIn.isEmpty() ? checkIn : checkOut;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin, container, false);

        rvCheckIn = view.findViewById(R.id.rvCheckIn);
        tvCheckInDate = view.findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = view.findViewById(R.id.tvCheckOutDate);
        llCheckInDate = view.findViewById(R.id.llCheckInDate);
        llCheckOutDate = view.findViewById(R.id.llCheckOutDate);

        // Ban đầu không hiện ngày
        tvCheckInDate.setText("");
        tvCheckOutDate.setText("");

        llCheckInDate.setOnClickListener(v -> showDatePicker(tvCheckInDate));
        llCheckOutDate.setOnClickListener(v -> showDatePicker(tvCheckOutDate));

        view.findViewById(R.id.btnSearch).setOnClickListener(v -> loadData(view));

        rvCheckIn.setLayoutManager(new LinearLayoutManager(getContext()));
        checkInList = new ArrayList<>();
        adapter = new CheckInAdapter(checkInList, new CheckInAdapter.OnCheckInClickListener() {
            @Override
            public void onCheckInClick(CheckInModel item) {
                showConfirmCheckInDialog(item);
            }

            @Override
            public void onChangeRoomClick(CheckInModel item) {
                showChangeRoomDialog(item, item.getBookingId());
            }
        });
        rvCheckIn.setAdapter(adapter);

        loadData(view);
        return view;
    }

    /** Mở lịch chọn ngày và tự tải lại danh sách sau khi người dùng chọn. */
    private void showDatePicker(TextView targetTextView) {
        Dialog calendarDialog = new Dialog(requireContext());
        calendarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        calendarDialog.setContentView(R.layout.dialog_custom_calendar);
        if (calendarDialog.getWindow() != null) {
            calendarDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        CalendarView calendarView = calendarDialog.findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            targetTextView.setText(date);
            calendarDialog.dismiss();
            loadData(null); // Tự động load lại sau khi chọn ngày
        });

        calendarDialog.show();
    }

    /** Hiển thị dialog xác nhận nhận phòng, gồm thông tin khách, phòng và CMND/CCCD. */
    /*
     * Hiển thị dialog xác nhận nhận phòng.
     * Dialog hiển thị thông tin khách/phòng và thu CCCD, ghi chú trước khi gọi API confirm.
     */
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
        android.widget.EditText etIdCard = dialog.findViewById(R.id.etIdCard);
        android.widget.EditText etNote = dialog.findViewById(R.id.etNote);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        if (tvName != null) tvName.setText(item.getGuestName());
        if (tvRoom != null) {
            String roomText = item.getRoomNumber();
            if (!roomText.startsWith("Phòng")) {
                roomText = "Phòng " + roomText;
            }
            tvRoom.setText(roomText);
        }
        
        String[] dates = item.getStayPeriod().split(" - ");
        if (dates.length == 2) {
            if (tvIn != null) tvIn.setText(dates[0]);
            if (tvOut != null) tvOut.setText(dates[1]);
        }

        if (tvClose != null) tvClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (item != null) {
                    performCheckInConfirm(item, etIdCard, etNote, btnConfirm, dialog);
                    return;
                }
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    /** Kiểm tra CCCD 9/12 số rồi gọi API xác nhận nhận phòng; thành công sẽ refresh danh sách. */
    /*
     * Xác nhận nhận phòng.
     *
     * Nghiệp vụ quan trọng:
     * - CCCD/CMND bắt buộc 9 hoặc 12 chữ số.
     * - Backend sẽ kiểm tra lại trạng thái booking, phòng đang bận hay chưa,
     *   sau đó insert luu_tru và cập nhật trạng thái phòng/đặt phòng.
     */
    private void performCheckInConfirm(CheckInModel item, android.widget.EditText etIdCard, android.widget.EditText etNote, Button btnConfirm, Dialog dialog) {
        String cccd = etIdCard == null ? "" : etIdCard.getText().toString().trim();
        String note = etNote == null ? "" : etNote.getText().toString().trim();
        if (!cccd.matches("^(\\d{9}|\\d{12})$")) {
            android.widget.Toast.makeText(getContext(), "Vui lòng nhập CMND/CCCD 9 hoặc 12 chữ số", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        com.project_mobile.network.ApiModels.CheckInRequest req = new com.project_mobile.network.ApiModels.CheckInRequest();
        req.cccd = cccd;
        req.note = note;

        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.confirmCheckIn(item.getBookingId(), req).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<Object>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<Object>> response) {
                btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            showSuccessDialog(item, "Nhận phòng thành công");
                            loadData(null);
                        });
                    }
                } else {
                    String msg = response.body() != null && response.body().message != null
                            ? response.body().message
                            : buildErrorMessage(response, "Không thể nhận phòng");
                    if (isAdded()) {
                        android.widget.Toast.makeText(getContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<Object>> call, Throwable t) {
                btnConfirm.setEnabled(true);
                if (isAdded()) {
                    android.widget.Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** Mở dialog đổi phòng, tải danh sách phòng trống cho booking và gửi request đổi phòng khi xác nhận. */
    /*
     * Đổi phòng trước khi nhận phòng.
     *
     * Luồng xử lý:
     * 1. Gọi GET /api/check-in/bookings/{maDatPhong}/available-rooms.
     * 2. Backend chỉ trả các phòng đang trống và không bị trùng lịch.
     * 3. Người dùng chọn phòng mới trong Spinner.
     * 4. Gửi ChangeRoomRequest gồm oldRoomId, newRoomId và reason.
     */
    private void showChangeRoomDialog(CheckInModel item, String bookingId) {
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

        // Đặt trạng thái loading cho spinner trong lúc chờ API trả danh sách phòng trống.
        // Set loading state for spinner
        List<String> loadingList = new ArrayList<>();
        loadingList.add("Đang tải danh sách phòng...");
        if (getContext() != null) {
            ArrayAdapter<String> loadingAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, loadingList);
            loadingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (spinnerRooms != null) spinnerRooms.setAdapter(loadingAdapter);
        }

        // Lấy phòng trống thật từ backend; chỉ các phòng trả về ở đây mới được cho chọn.
        // Fetch real available rooms
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        final List<com.project_mobile.network.ApiModels.RoomDto> availableRooms = new ArrayList<>();
        
        api.getAvailableRooms(bookingId).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<String> roomNames = new ArrayList<>();
                    roomNames.add("-- Chọn phòng trống --");
                    if (response.body().data != null) {
                        availableRooms.addAll(response.body().data);
                        for (com.project_mobile.network.ApiModels.RoomDto r : response.body().data) {
                            roomNames.add(r.roomNumber + " (" + r.roomType + ")");
                        }
                    }
                    
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (getContext() != null) {
                                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, roomNames);
                                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                if (spinnerRooms != null) spinnerRooms.setAdapter(spinnerAdapter);
                            }
                        });
                    }
                } else {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            List<String> errorList = new ArrayList<>();
                            String msg = "Không tìm thấy phòng trống";
                            if (response.body() != null && response.body().message != null) {
                                msg = response.body().message;
                            } else if (response.errorBody() != null) {
                                try {
                                    msg = "Lỗi " + response.code() + ": " + response.errorBody().string();
                                } catch (Exception e) {}
                            }
                            errorList.add(msg);
                            if (getContext() != null) {
                                ArrayAdapter<String> errorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, errorList);
                                if (spinnerRooms != null) spinnerRooms.setAdapter(errorAdapter);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoomDto>>> call, Throwable t) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        List<String> errorList = new ArrayList<>();
                        errorList.add("Lỗi kết nối: " + t.getMessage());
                        if (getContext() != null) {
                            ArrayAdapter<String> errorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, errorList);
                            if (spinnerRooms != null) spinnerRooms.setAdapter(errorAdapter);
                        }
                    });
                }
            }
        });

        if (tvClose != null) tvClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (spinnerRooms == null) return;
                // selectedPos = 0 là placeholder "-- Chọn phòng trống --", nên phải chọn từ vị trí 1 trở đi.
                int selectedPos = spinnerRooms.getSelectedItemPosition();
                if (selectedPos <= 0) {
                    android.widget.Toast.makeText(getContext(), "Vui lòng chọn phòng trống", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                // selectedPos - 1 vì vị trí 0 của Spinner là dòng placeholder, không phải phòng thật.
                com.project_mobile.network.ApiModels.RoomDto selectedRoom = availableRooms.get(selectedPos - 1);
                com.project_mobile.network.ApiModels.ChangeRoomRequest req = new com.project_mobile.network.ApiModels.ChangeRoomRequest();
                req.newRoomId = selectedRoom.id;
                req.oldRoomId = item.getOldRoomId();
                req.reason = "Khách yêu cầu đổi phòng";

                api.changeRoom(bookingId, req).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<Void>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<Void>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    dialog.dismiss();
                                    showSuccessDialog(item, "Đổi sang phòng " + selectedRoom.roomNumber + " thành công");
                                    loadData(null); // Refresh list
                                });
                            }
                        } else {
                            String msg = response.body() != null ? response.body().message : "Lỗi đổi phòng";
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> 
                                    android.widget.Toast.makeText(getContext(), msg, android.widget.Toast.LENGTH_SHORT).show());
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<Void>> call, Throwable t) {
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> 
                                android.widget.Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            });
        }

        dialog.show();
    }

    /** Lấy id phòng đầu tiên của booking để backend biết phòng cũ khi đổi phòng. */
    private Integer firstRoomId(com.project_mobile.network.ApiModels.BookingDto booking) {
        if (booking == null || booking.rooms == null || booking.rooms.isEmpty()) return null;
        return booking.rooms.get(0).id;
    }

    /** Đọc errorBody JSON từ backend để hiển thị message nghiệp vụ thay vì chỉ mã HTTP. */
    private String buildErrorMessage(retrofit2.Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                if (raw != null && !raw.trim().isEmpty()) {
                    JSONObject json = new JSONObject(raw);
                    if (json.has("message") && !json.optString("message").trim().isEmpty()) {
                        return json.optString("message");
                    }
                    if (json.has("error") && !json.optString("error").trim().isEmpty()) {
                        return json.optString("error");
                    }
                    return raw;
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    /** Hiển thị dialog thành công sau nhận phòng hoặc đổi phòng. */
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

    /** Căn kích thước dialog theo màn hình để dialog check-in/đổi phòng không quá rộng. */
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
