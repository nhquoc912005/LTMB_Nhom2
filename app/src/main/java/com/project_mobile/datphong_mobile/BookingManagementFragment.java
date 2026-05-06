package com.project_mobile.datphong_mobile;

import android.app.Dialog;
import android.content.res.ColorStateList;
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
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingManagementFragment extends Fragment {

    private static final String FILTER_ALL = "Tất cả";
    private static final String FILTER_TODAY = "Hôm nay";
    private static final String FILTER_MONTH = "Tháng này";
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
    private MaterialButton btnFilterAll;
    private MaterialButton btnFilterToday;
    private MaterialButton btnFilterMonth;
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

        for (BookingDto bookingDto : allData) {
            String statusValue = bookingDto.status != null ? bookingDto.status : "";
            boolean matches = matchesFilter(bookingDto, statusValue, statusFilter);

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

            int adults = bookingDto.adults != null ? bookingDto.adults : 0;
            int children = bookingDto.children != null ? bookingDto.children : 0;
            int totalGuests = bookingDto.totalGuests != null ? bookingDto.totalGuests : 0;
            if (totalGuests <= 0) {
                totalGuests = adults + children;
            }

            bookingList.add(new Booking(
                    bookingDto.bookingId,
                    roomNum,
                    normalizedStatus,
                    safeText(bookingDto.customerName, "Khách vãng lai"),
                    safeText(bookingDto.email, "Chưa có email"),
                    safeText(bookingDto.phone),
                    formatDate(bookingDto.checkIn),
                    formatDate(bookingDto.checkOut),
                    formatCurrency(bookingDto.totalAmount != null ? bookingDto.totalAmount : 0),
                    totalGuests,
                    adults,
                    children
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
            updateDateFilterButtons(btnFilterAll);
        });
        boxPending.setOnClickListener(v -> {
            filterList(FILTER_PENDING);
            selectBox(v);
            updateDateFilterButtons(null);
        });
        boxCheckedIn.setOnClickListener(v -> {
            filterList(FILTER_CHECKED_IN);
            selectBox(v);
            updateDateFilterButtons(null);
        });
        boxCancelled.setOnClickListener(v -> {
            filterList(FILTER_CANCELLED);
            selectBox(v);
            updateDateFilterButtons(null);
        });

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterToday = view.findViewById(R.id.btnFilterToday);
        btnFilterMonth = view.findViewById(R.id.btnFilterMonth);

        btnFilterAll.setOnClickListener(v -> {
            filterList(FILTER_ALL);
            updateDateFilterButtons(btnFilterAll);
            selectBox(boxTotal);
        });
        btnFilterToday.setOnClickListener(v -> {
            filterList(FILTER_TODAY);
            updateDateFilterButtons(btnFilterToday);
            selectBox(null);
        });
        btnFilterMonth.setOnClickListener(v -> {
            filterList(FILTER_MONTH);
            updateDateFilterButtons(btnFilterMonth);
            selectBox(null);
        });
        updateDateFilterButtons(btnFilterAll);
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
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        TextView tvClose = dialog.findViewById(R.id.tvClose);

        tvGuestName.setText("Họ và tên: " + safeText(booking.getCustomerName(), "Khách vãng lai"));
        tvGuestEmail.setText("Email: " + safeText(booking.getCustomerEmail(), "Chưa có email"));
        tvGuestPhone.setText("SĐT: " + safeText(booking.getCustomerPhone()));
        tvCheckIn.setText(safeText(booking.getCheckInDate()));
        tvCheckOut.setText(safeText(booking.getCheckOutDate()));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        tvClose.setOnClickListener(v -> dialog.dismiss());
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
        return status.contains("Đang ở") || status.contains("Đã check-in") || status.contains("Đã nhận phòng");
    }

    private boolean isCancelledStatus(String status) {
        return status.contains("Đã hủy") || status.contains("Hủy");
    }

    private boolean matchesFilter(BookingDto bookingDto, String statusValue, String filter) {
        if (FILTER_ALL.equals(filter)) {
            return true;
        }
        if (FILTER_TODAY.equals(filter)) {
            Calendar today = Calendar.getInstance();
            return isStayOverlappingRange(bookingDto.checkIn, bookingDto.checkOut, startOfDay(today), endOfDay(today));
        }
        if (FILTER_MONTH.equals(filter)) {
            Calendar monthStart = Calendar.getInstance();
            monthStart.set(Calendar.DAY_OF_MONTH, 1);

            Calendar monthEnd = (Calendar) monthStart.clone();
            monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

            return isStayOverlappingRange(bookingDto.checkIn, bookingDto.checkOut, startOfDay(monthStart), endOfDay(monthEnd));
        }
        if (FILTER_PENDING.equals(filter)) {
            return isPendingStatus(statusValue);
        }
        if (FILTER_CHECKED_IN.equals(filter)) {
            return isCheckedInStatus(statusValue);
        }
        if (FILTER_CANCELLED.equals(filter)) {
            return isCancelledStatus(statusValue);
        }
        return false;
    }

    private boolean isStayOverlappingRange(String checkInRaw, String checkOutRaw, long rangeStart, long rangeEnd) {
        Date checkIn = parseApiDate(checkInRaw);
        Date checkOut = parseApiDate(checkOutRaw);
        if (checkIn == null && checkOut == null) {
            return false;
        }

        long stayStart = checkIn != null ? startOfDay(checkIn) : startOfDay(checkOut);
        long stayEnd = checkOut != null ? endOfDay(checkOut) : endOfDay(checkIn);
        if (stayStart > stayEnd) {
            long temp = stayStart;
            stayStart = stayEnd;
            stayEnd = temp;
        }
        return stayStart <= rangeEnd && stayEnd >= rangeStart;
    }

    private long startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return startOfDay(calendar);
    }

    private long endOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return endOfDay(calendar);
    }

    private long startOfDay(Calendar calendar) {
        Calendar copy = (Calendar) calendar.clone();
        copy.set(Calendar.HOUR_OF_DAY, 0);
        copy.set(Calendar.MINUTE, 0);
        copy.set(Calendar.SECOND, 0);
        copy.set(Calendar.MILLISECOND, 0);
        return copy.getTimeInMillis();
    }

    private long endOfDay(Calendar calendar) {
        Calendar copy = (Calendar) calendar.clone();
        copy.set(Calendar.HOUR_OF_DAY, 23);
        copy.set(Calendar.MINUTE, 59);
        copy.set(Calendar.SECOND, 59);
        copy.set(Calendar.MILLISECOND, 999);
        return copy.getTimeInMillis();
    }

    private void updateDateFilterButtons(@Nullable MaterialButton selectedButton) {
        updateDateFilterButton(btnFilterAll, selectedButton == btnFilterAll);
        updateDateFilterButton(btnFilterToday, selectedButton == btnFilterToday);
        updateDateFilterButton(btnFilterMonth, selectedButton == btnFilterMonth);
    }

    private void updateDateFilterButton(@Nullable MaterialButton button, boolean active) {
        if (button == null) return;
        button.setBackgroundTintList(ColorStateList.valueOf(active ? 0xFFA3734D : 0xFFFFFFFF));
        button.setTextColor(active ? 0xFFFFFFFF : 0xFF8B6D5A);
        button.setStrokeColor(ColorStateList.valueOf(active ? 0xFFA3734D : 0xFFD1C19F));
        button.setStrokeWidth(dpToPx(active ? 0 : 1));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
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
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
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
