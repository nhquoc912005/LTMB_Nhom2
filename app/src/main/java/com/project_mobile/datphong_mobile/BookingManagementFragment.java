// Module đặt phòng Android.
// File này là màn quản lý danh sách đặt phòng, thống kê trạng thái và hủy booking khi còn được phép.
// Dữ liệu chính lấy từ /api/bookings và thao tác hủy qua /api/bookings/{id}/cancel.
/*
 * File: BookingManagementFragment.java
 * Module: Quản lý đặt phòng.
 *
 * Luồng chính của màn hình:
 * 1. onCreateView khởi tạo RecyclerView, ô thống kê và bộ lọc.
 * 2. loadBookings gọi API /api/bookings để lấy dữ liệu đặt phòng.
 * 3. filterList lọc BookingDto theo trạng thái/ngày rồi map sang Booking model cho UI.
 * 4. BookingAdapter hiển thị từng card và phát sự kiện hủy đặt phòng về Fragment.
 * 5. performCancelBooking gọi API /api/bookings/{id}/cancel để backend kiểm tra và cập nhật trạng thái.
 *
 * Dữ liệu xử lý chính:
 * - BookingDto: dữ liệu thô từ API, giữ nguyên field backend trả về.
 * - Booking: dữ liệu đã format cho giao diện Android.
 * - currentFilter/allData/bookingList: lần lượt là bộ lọc hiện tại, dữ liệu gốc và dữ liệu đang hiển thị.
 */
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

/**
 * BookingManagementFragment là màn quản lý booking đang dùng trong MainActivity.
 * Class này lọc booking theo trạng thái/ngày, map BookingDto sang Booking UI model và xử lý hủy đặt phòng.
 */
public class BookingManagementFragment extends Fragment {

    private static final String FILTER_ALL = "Tất cả";
    private static final String FILTER_TODAY = "Hôm nay";
    private static final String FILTER_MONTH = "Tháng này";
    private static final String FILTER_PENDING = "Chờ nhận phòng";
    private static final String FILTER_CHECKED_IN = "Đã nhận phòng";
    private static final String FILTER_CANCELLED = "Đã hủy";

    private RecyclerView rvBookings;
    private BookingAdapter adapter;
    // Danh sách đang hiển thị trên RecyclerView sau khi đã áp dụng filter.
    private final List<Booking> bookingList = new ArrayList<>();
    // Danh sách gốc từ API; giữ lại để đổi filter mà không cần gọi API lại ngay.
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

    /** Hiển thị ngày hiện tại ở header của màn đặt phòng. */
    private void setHeaderDate() {
        if (tvBookingCurrentDate == null) return;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM, yyyy", new Locale("vi", "VN"));
        String date = sdf.format(new Date());
        if (!date.isEmpty()) {
            date = date.substring(0, 1).toUpperCase() + date.substring(1);
        }
        tvBookingCurrentDate.setText(date);
    }

    /** Tải toàn bộ booking từ backend, cập nhật thống kê rồi áp dụng filter hiện tại. */
    /*
     * Luồng tải dữ liệu:
     * - Gọi GET /api/bookings.
     * - Lưu response.data vào allData để làm nguồn dữ liệu gốc.
     * - updateStats đếm số lượng theo trạng thái.
     * - filterList dùng currentFilter để map BookingDto sang Booking và cập nhật RecyclerView.
     */
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

    /** Lọc danh sách booking theo trạng thái hoặc khoảng ngày, sau đó bind vào BookingAdapter. */
    /*
     * Map dữ liệu API sang model UI:
     * - BookingDto giữ field dạng API/backend.
     * - Booking giữ chuỗi đã format như "Phòng 101", "01 Th02, 2026", "1,200,000đ".
     * - totalGuests được tự tính từ adults + children nếu backend không trả đủ.
     */
    private void filterList(String statusFilter) {
        currentFilter = statusFilter;
        bookingList.clear();

        for (BookingDto bookingDto : allData) {
            String statusValue = bookingDto.status != null ? bookingDto.status : "";
            // Kiểm tra booking có thuộc filter đang chọn không trước khi map sang card UI.
            boolean matches = matchesFilter(bookingDto, statusValue, statusFilter);

            if (!matches) {
                continue;
            }

            // Chuẩn hóa số phòng để card luôn hiển thị cùng một dạng "Phòng xxx".
            String roomNum = safeText(bookingDto.roomNumber, "N/A");
            if (!"N/A".equals(roomNum) && !roomNum.startsWith("Phòng")) {
                roomNum = "Phòng " + roomNum;
            }

            String normalizedStatus = safeText(bookingDto.status);
            if ("Đang ở".equals(normalizedStatus)) {
                normalizedStatus = FILTER_CHECKED_IN;
            }

            // Tổng số khách ưu tiên field totalGuests từ API.
            // Nếu API không trả hoặc trả 0, app tự cộng người lớn + trẻ em để tránh hiển thị sai.
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

    /** Đếm tổng, chờ nhận phòng, đã nhận phòng và đã hủy để hiển thị trên các ô thống kê. */
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

    /** Gắn sự kiện cho các ô thống kê và nút lọc theo tất cả/hôm nay/tháng này. */
    /*
     * Thiết lập sự kiện cho nhóm bộ lọc:
     * - Các ô thống kê lọc theo trạng thái đặt phòng.
     * - Các nút Tất cả/Hôm nay/Tháng này lọc theo khoảng thời gian lưu trú.
     * - Sau mỗi lần chọn, RecyclerView và màu active được cập nhật cùng lúc.
     */
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

    /** Đổi màu ô thống kê đang chọn để người dùng biết filter nào đang active. */
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

    /** Mở dialog xác nhận hủy booking và chặn trường hợp thiếu mã đặt phòng. */
    /*
     * Dialog này chỉ xác nhận ý định hủy của người dùng.
     * Điều kiện hủy thật sự vẫn được backend kiểm tra khi gọi API cancelBooking.
     */
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

    /** Gọi API hủy booking; backend sẽ kiểm tra trạng thái có còn được hủy hay không. */
    /*
     * Luồng hủy đặt phòng:
     * 1. Khóa nút xác nhận để tránh gửi trùng request.
     * 2. Gọi PUT /api/bookings/{id}/cancel.
     * 3. Backend kiểm tra booking có còn ở trạng thái được hủy không.
     * 4. Nếu thành công, tải lại danh sách để thống kê và card được đồng bộ.
     */
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

    /** Hiển thị dialog hủy đặt phòng thành công sau khi backend cập nhật xong. */
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

    /** Nhận diện các trạng thái được xem là chờ nhận phòng/chưa check-in. */
    private boolean isPendingStatus(String status) {
        return status.contains("Đã đặt cọc") || status.contains("Chờ check-in") || status.contains("Chờ nhận phòng");
    }

    /** Nhận diện các trạng thái booking đã bước sang lưu trú thực tế. */
    private boolean isCheckedInStatus(String status) {
        return status.contains("Đang ở") || status.contains("Đã check-in") || status.contains("Đã nhận phòng");
    }

    /** Nhận diện các trạng thái đã hủy từ backend để lọc và thống kê. */
    private boolean isCancelledStatus(String status) {
        return status.contains("Đã hủy") || status.contains("Hủy");
    }

    /** Quyết định một booking có xuất hiện trong filter trạng thái/ngày hiện tại hay không. */
    /*
     * Filter theo ngày không chỉ so sánh ngày bắt đầu.
     * Một booking được xem là thuộc hôm nay/tháng này nếu khoảng lưu trú giao với khoảng lọc.
     */
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

    /** Kiểm tra khoảng lưu trú có giao với ngày hôm nay hoặc tháng hiện tại không. */
    /*
     * Kiểm tra giao nhau giữa khoảng lưu trú và khoảng lọc.
     * Ví dụ: booking check-in hôm qua, check-out ngày mai vẫn phải xuất hiện ở filter Hôm nay.
     */
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

    /** Format ngày từ API sang chuỗi ngắn để hiển thị trong card đặt phòng. */
    private String formatDate(String apiDate) {
        Date date = parseApiDate(apiDate);
        if (date == null) return safeText(apiDate);

        SimpleDateFormat out = new SimpleDateFormat("dd 'Th'MM, yyyy", new Locale("vi", "VN"));
        return out.format(date);
    }

    /** Parse nhiều định dạng date/datetime vì backend có thể trả ngày thuần hoặc ISO timestamp. */
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

    /** Ưu tiên message/error từ backend để Toast báo lỗi hủy/tải dữ liệu dễ hiểu hơn. */
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
