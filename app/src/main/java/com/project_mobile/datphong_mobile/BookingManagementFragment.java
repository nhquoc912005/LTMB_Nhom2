package com.project_mobile.datphong_mobile;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project_mobile.R;
import com.project_mobile.network.ApiClient;
import com.project_mobile.network.ApiModels.ApiResponse;
import com.project_mobile.network.ApiModels.BookingDto;
import com.project_mobile.network.ApiService;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingManagementFragment extends Fragment {

    private static final String FILTER_ALL = "Tất cả";
    private static final String FILTER_PENDING = "Chờ nhận phòng";
    private static final String FILTER_CHECKED_IN = "Đã nhận phòng";
    private static final String FILTER_CANCELLED = "Đã hủy";

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    private final List<Booking> bookingList = new ArrayList<>();
    private List<BookingDto> allData = new ArrayList<>();
    private final List<View> statBoxes = new ArrayList<>();
    private TextView tvCountTotal;
    private TextView tvCountPending;
    private TextView tvCountCheckedIn;
    private TextView tvCountCancelled;
    private TextView tvBookingCurrentDate;
    private String currentFilter = FILTER_ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_management, container, false);

        rvBookings = view.findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        tvCountTotal = view.findViewById(R.id.tvCountTotal);
        tvCountPending = view.findViewById(R.id.tvCountPending);
        tvCountCheckedIn = view.findViewById(R.id.tvCountCheckedIn);
        tvCountCancelled = view.findViewById(R.id.tvCountCancelled);
        tvBookingCurrentDate = view.findViewById(R.id.tvBookingCurrentDate);

        statBoxes.clear();
        statBoxes.add(view.findViewById(R.id.boxTotal));
        statBoxes.add(view.findViewById(R.id.boxPending));
        statBoxes.add(view.findViewById(R.id.boxCheckedIn));
        statBoxes.add(view.findViewById(R.id.boxCancelled));

        setHeaderDate();
        setupFilters(view);
        selectBox(view.findViewById(R.id.boxTotal));
        loadBookings();

        return view;
    }

    private void setHeaderDate() {
        if (tvBookingCurrentDate == null) return;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM, yyyy", new Locale("vi", "VN"));
        String date = sdf.format(new Date());
        if (!date.isEmpty()) {
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
        }
        tvBookingCurrentDate.setText(date);
    }

    private void loadBookings() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getBookings().enqueue(new Callback<ApiResponse<List<BookingDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDto>>> call, Response<ApiResponse<List<BookingDto>>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    allData = response.body().data != null ? response.body().data : new ArrayList<>();
                    updateStats(allData);
                    filterList(currentFilter);
                    return;
                }

                Toast.makeText(getContext(), buildErrorMessage(response, "Không thể tải danh sách đặt phòng"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingDto>>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + safeText(t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(String statusFilter) {
        currentFilter = statusFilter;
        bookingList.clear();

        long now = System.currentTimeMillis();
        long oneWeekMs = 7L * 24 * 60 * 60 * 1000;

        for (BookingDto bookingDto : allData) {
            String statusValue = bookingDto.status != null ? bookingDto.status : "";
            boolean matches = false;

            boolean withinWeek = true;
            if (FILTER_ALL.equals(statusFilter)) {
                Date checkInDate = parseApiDate(bookingDto.checkIn);
                if (checkInDate != null) {
                    long diff = checkInDate.getTime() - now;
                    withinWeek = diff >= -24L * 60 * 60 * 1000 && diff <= oneWeekMs;
                }
            }

            if (FILTER_ALL.equals(statusFilter)) {
                matches = withinWeek;
            } else if (FILTER_PENDING.equals(statusFilter) && isPendingStatus(statusValue)) {
                matches = true;
            } else if (FILTER_CHECKED_IN.equals(statusFilter) && isCheckedInStatus(statusValue)) {
                matches = true;
            } else if (FILTER_CANCELLED.equals(statusFilter) && isCancelledStatus(statusValue)) {
                matches = true;
            }

            if (!matches) {
                continue;
            }

            String roomNum = safeText(bookingDto.roomNumber, "N/A");
            if (!"N/A".equals(roomNum) && !roomNum.startsWith("Phòng")) {
                roomNum = "Phòng " + roomNum;
            }

            String normalizedStatus = safeText(bookingDto.status);
            if ("Đang ở".equals(normalizedStatus)) {
                normalizedStatus = FILTER_CHECKED_IN;
            }

            bookingList.add(new Booking(
                    bookingDto.bookingId,
                    roomNum,
                    normalizedStatus,
                    safeText(bookingDto.customerName, "Khách vãng lai"),
                    safeText(bookingDto.email),
                    safeText(bookingDto.phone),
                    formatDate(bookingDto.checkIn),
                    formatDate(bookingDto.checkOut),
                    formatCurrency(bookingDto.totalAmount != null ? bookingDto.totalAmount : 0),
                    bookingDto.totalGuests != null ? bookingDto.totalGuests : 0,
                    bookingDto.adults != null ? bookingDto.adults : 0,
                    bookingDto.children != null ? bookingDto.children : 0
            ));
        }

        adapter = new BookingAdapter(bookingList, this::showCancelBookingDialog);
        rvBookings.setAdapter(adapter);
    }

    private void updateStats(List<BookingDto> data) {
        int total = data.size();
        int waiting = 0;
        int checkedIn = 0;
        int cancelled = 0;

        for (BookingDto bookingDto : data) {
            String status = bookingDto.status != null ? bookingDto.status : "";
            if (isPendingStatus(status)) waiting++;
            else if (isCheckedInStatus(status)) checkedIn++;
            else if (isCancelledStatus(status)) cancelled++;
        }

        if (tvCountTotal != null) tvCountTotal.setText(String.format(Locale.US, "%02d", total));
        if (tvCountPending != null) tvCountPending.setText(String.format(Locale.US, "%02d", waiting));
        if (tvCountCheckedIn != null) tvCountCheckedIn.setText(String.format(Locale.US, "%02d", checkedIn));
        if (tvCountCancelled != null) tvCountCancelled.setText(String.format(Locale.US, "%02d", cancelled));
    }

    private void setupFilters(View view) {
        View boxTotal = view.findViewById(R.id.boxTotal);
        View boxPending = view.findViewById(R.id.boxPending);
        View boxCheckedIn = view.findViewById(R.id.boxCheckedIn);
        View boxCancelled = view.findViewById(R.id.boxCancelled);

        boxTotal.setOnClickListener(v -> {
            filterList(FILTER_ALL);
            selectBox(v);
        });
        boxPending.setOnClickListener(v -> {
            filterList(FILTER_PENDING);
            selectBox(v);
        });
        boxCheckedIn.setOnClickListener(v -> {
            filterList(FILTER_CHECKED_IN);
            selectBox(v);
        });
        boxCancelled.setOnClickListener(v -> {
            filterList(FILTER_CANCELLED);
            selectBox(v);
        });

        view.findViewById(R.id.btnFilterAll).setOnClickListener(v -> loadBookings());
    }

    private void selectBox(View selected) {
        for (View v : statBoxes) {
            if (v == null) continue;
            boolean isSelected = v == selected;
            com.google.android.material.card.MaterialCardView card =
                    (com.google.android.material.card.MaterialCardView) v;
            card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(
                    isSelected ? 0xFFA3734D : 0xFFFFFFFF
            ));

            ViewGroup layout = (ViewGroup) card.getChildAt(0);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(isSelected ? 0xFFFFFFFF : 0xFF333333);
                }
            }
        }
    }

    private void showCancelBookingDialog(Booking booking) {
        if (!isAdded()) return;
        if (booking.getBookingId() == null || booking.getBookingId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy mã đặt phòng để hủy", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cancel_booking_confirm);
        dialog.setCancelable(true);
        setupDialogWindow(dialog, 0.92f);

        TextView tvGuestName = dialog.findViewById(R.id.tvCancelGuestName);
        TextView tvGuestEmail = dialog.findViewById(R.id.tvCancelGuestEmail);
        TextView tvGuestPhone = dialog.findViewById(R.id.tvCancelGuestPhone);
        TextView tvCheckIn = dialog.findViewById(R.id.tvCancelCheckInDate);
        TextView tvCheckOut = dialog.findViewById(R.id.tvCancelCheckOutDate);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmCancelBooking);

        tvGuestName.setText("Họ và tên: " + safeText(booking.getCustomerName(), "Khách vãng lai"));
        tvGuestEmail.setText("Email: " + safeText(booking.getCustomerEmail()));
        tvGuestPhone.setText("SĐT: " + safeText(booking.getCustomerPhone()));
        tvCheckIn.setText(safeText(booking.getCheckInDate()));
        tvCheckOut.setText(safeText(booking.getCheckOutDate()));

        btnConfirm.setOnClickListener(v -> performCancelBooking(booking, dialog, btnConfirm));
        dialog.show();
    }

    private void performCancelBooking(Booking booking, Dialog dialog, Button btnConfirm) {
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang hủy...");

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.cancelBooking(booking.getBookingId()).enqueue(new Callback<ApiResponse<BookingDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDto>> call, Response<ApiResponse<BookingDto>> response) {
                if (!isAdded()) return;

                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác nhận hủy");

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    dialog.dismiss();
                    loadBookings();
                    showCancelSuccessDialog();
                    return;
                }

                Toast.makeText(getContext(), buildErrorMessage(response, "Không thể hủy đặt phòng"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDto>> call, Throwable t) {
                if (!isAdded()) return;
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác nhận hủy");
                Toast.makeText(getContext(), "Lỗi kết nối: " + safeText(t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelSuccessDialog() {
        if (!isAdded()) return;

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cancel_booking_success);
        dialog.setCancelable(true);
        setupDialogWindow(dialog, 0.92f);

        View btnClose = dialog.findViewById(R.id.btnCloseCancelSuccess);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupDialogWindow(Dialog dialog, float widthRatio) {
        Window window = dialog.getWindow();
        if (window == null) return;

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());
        lp.width = (int) (displayMetrics.widthPixels * widthRatio);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);
    }

    private boolean isPendingStatus(String status) {
        return status.contains("Đã đặt cọc") || status.contains("Chờ check-in") || status.contains("Chờ nhận phòng");
    }

    private boolean isCheckedInStatus(String status) {
        return status.contains("Đang ở") || status.contains("Đã check-in") || status.contains("Đã nhận phòng") || status.contains("nhận phòng");
    }

    private boolean isCancelledStatus(String status) {
        return status.contains("Đã hủy") || status.contains("Hủy");
    }

    private String formatDate(String apiDate) {
        Date date = parseApiDate(apiDate);
        if (date == null) return safeText(apiDate);

        SimpleDateFormat out = new SimpleDateFormat("dd 'Th'MM, yyyy", new Locale("vi", "VN"));
        return out.format(date);
    }

    private Date parseApiDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return null;

        String[] patterns = {
                "yyyy-MM-dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                return format.parse(rawDate);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String formatCurrency(double amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(amount) + "đ";
    }

    private String buildErrorMessage(Response<?> response, String fallback) {
        if (response.body() instanceof ApiResponse) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) response.body();
            if (apiResponse.message != null && !apiResponse.message.trim().isEmpty()) {
                return apiResponse.message;
            }
            if (apiResponse.error != null && !apiResponse.error.trim().isEmpty()) {
                return apiResponse.error;
            }
        }

        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                if (errorBody != null && !errorBody.trim().isEmpty()) {
                    return errorBody;
                }
            }
        } catch (IOException ignored) {
        }

        return fallback;
    }

    private String safeText(String value) {
        return safeText(value, "-");
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value;
    }
}
