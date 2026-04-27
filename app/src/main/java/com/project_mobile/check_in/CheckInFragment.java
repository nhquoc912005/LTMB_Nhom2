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
                            for (com.project_mobile.network.ApiModels.BookingDto b : data) {
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
                            : "Không thể nhận phòng";
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

        // Set loading state for spinner
        List<String> loadingList = new ArrayList<>();
        loadingList.add("Đang tải danh sách phòng...");
        if (getContext() != null) {
            ArrayAdapter<String> loadingAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, loadingList);
            loadingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (spinnerRooms != null) spinnerRooms.setAdapter(loadingAdapter);
        }

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
                int selectedPos = spinnerRooms.getSelectedItemPosition();
                if (selectedPos <= 0) {
                    android.widget.Toast.makeText(getContext(), "Vui lòng chọn phòng trống", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

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

    private Integer firstRoomId(com.project_mobile.network.ApiModels.BookingDto booking) {
        if (booking == null || booking.rooms == null || booking.rooms.isEmpty()) return null;
        return booking.rooms.get(0).id;
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
