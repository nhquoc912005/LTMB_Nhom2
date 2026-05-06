// Module network/API Android.
// File này gom các DTO request/response dùng để Gson map JSON backend sang object Java.
// Dữ liệu chính gồm booking, phòng, hóa đơn checkout, nhân viên, vai trò, dịch vụ và tài sản.
/*
 * File: ApiModels.java
 * Module: Network/API contract Android.
 *
 * Chức năng:
 * - Định nghĩa DTO request/response dùng bởi Retrofit.
 * - Khớp tên field JSON từ Node.js API qua @SerializedName.
 * - Gom các nhóm dữ liệu: đặt phòng, nhận phòng, trả phòng, phòng, tài khoản, dịch vụ/tài sản.
 *
 * Quy ước đọc file:
 * - DTO thường phản ánh dữ liệu backend trả về.
 * - UI model trong từng module có thể format lại dữ liệu trước khi hiển thị.
 */
package com.project_mobile.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * ApiModels là lớp chứa các model thuần dữ liệu cho Retrofit.
 * Các @SerializedName có alternate giúp app nhận được cả field tiếng Việt và field camel/snake_case từ API.
 */
public class ApiModels {

    /** Envelope chung của backend: success + data + message/error. */
    public static class ApiResponse<T> {
        public boolean success;
        public T data;
        public String message;
        public String error;
    }

    /**
     * DTO nhận dữ liệu đặt phòng từ API backend.
     *
     * Tên field cần khớp với JSON server trả về.
     * Sau khi nhận response, DTO này thường được map sang Booking/CheckInModel
     * để dễ hiển thị trên giao diện Android.
     */
    public static class BookingDto {
        // Thông tin định danh đặt phòng.
        @SerializedName(value = "ma_dat_phong", alternate = {"booking_id", "bookingId"})
        public String bookingId;

        // Thông tin phòng; một booking có thể có nhiều phòng nên backend có thể trả chuỗi room_names.
        @SerializedName(value = "room_number", alternate = {"room_names", "so_phong", "roomNumber"})
        public String roomNumber;

        // Thông tin khách hàng.
        @SerializedName(value = "customer_name", alternate = {"ten_nguoi_dat", "customerName"})
        public String customerName;

        @SerializedName(value = "email", alternate = {"customer_email"})
        public String email;
        @SerializedName(value = "customer_phone", alternate = {"sdt_nguoi_dat", "customerPhone", "phone"})
        public String phone;

        // Thông tin số lượng khách, dùng để hiển thị và đối chiếu khi nhận phòng.
        @SerializedName(value = "tong_so_nguoi", alternate = {"total_guests", "totalGuests"})
        public Integer totalGuests;

        @SerializedName(value = "so_nguoi_lon", alternate = {"adults"})
        public Integer adults;

        @SerializedName(value = "so_tre_em", alternate = {"children"})
        public Integer children;

        // Thông tin ngày nhận/trả phòng.
        @SerializedName(value = "ngay_nhan", alternate = {"check_in", "activity_time", "checkIn"})
        public String checkIn;

        @SerializedName(value = "ngay_tra", alternate = {"check_out", "checkOut"})
        public String checkOut;

        @SerializedName("stayPeriod")
        public String stayPeriod;

        // Thông tin thanh toán và trạng thái đặt phòng.
        @SerializedName("payment_method")
        public String paymentMethod;

        @SerializedName(value = "total_amount", alternate = {"tong_thanh_toan"})
        public Double totalAmount;

        public String note;
        @SerializedName(value = "trang_thai", alternate = {"status"})
        public String status;

        // Danh sách phòng gắn với đơn đặt phòng, dùng khi check-in cần biết id phòng cũ.
        public List<RoomDto> rooms;
    }

    /** Payload tạo đặt phòng mới từ màn đặt phòng. */
    public static class CreateBookingRequest {
        @SerializedName("room_number")
        public String roomNumber;

        @SerializedName("customer_name")
        public String customerName;

        public String email;
        public String phone;

        @SerializedName("total_guests")
        public Integer totalGuests;

        public Integer adults;
        public Integer children;

        @SerializedName("check_in")
        public String checkIn;

        @SerializedName("check_out")
        public String checkOut;

        @SerializedName("payment_method")
        public String paymentMethod;

        @SerializedName("total_amount")
        public Double totalAmount;

        public String note;
    }

    /** Item danh mục dịch vụ hoặc tài sản/bồi thường. */
    public static class CatalogItemDto {
        public String id;
        public String name;
        public Double price;
        public String unit;
        public String icon;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("updated_at")
        public String updatedAt;
    }

    /** Payload tạo/sửa một dịch vụ hoặc tài sản trong danh mục. */
    public static class CatalogItemRequest {
        public String name;
        public Double price;
        public String unit;
        public String icon;
    }

    /** Phòng đang có lưu trú mở, dùng cho màn sơ đồ phòng dịch vụ/tài sản. */
    public static class ActiveRoomDto {
        @SerializedName("room_id")
        public Integer roomId;

        @SerializedName("room_number")
        public String roomNumber;

        public String status;

        @SerializedName("booking_id")
        public String bookingId;

        @SerializedName("stay_id")
        public Integer stayId;

        @SerializedName("customer_name")
        public String customerName;

        @SerializedName("check_in")
        public String checkIn;

        @SerializedName("check_out")
        public String checkOut;

        @SerializedName("room_fee")
        public Double roomFee;
    }

    /** Một dòng dịch vụ/tài sản đã gắn vào phòng trong thời gian lưu trú. */
    public static class RoomLineDto {
        public String id;

        @SerializedName("room_id")
        public Integer roomId;

        @SerializedName(value = "catalog_id", alternate = {"id_dichvu", "id_taisan"})
        public String catalogId;

        @SerializedName("service_id")
        public String serviceId;

        @SerializedName("asset_id")
        public String assetId;

        public String name;
        public Integer quantity;
        public Double price;

        @SerializedName(value = "total", alternate = {"total_price", "thanh_tien", "so_tien_boi_thuong"})
        public Double total;
    }

    /**
     * Request thêm một dịch vụ/tài sản vào phòng đang lưu trú.
     * catalogId là id danh mục chung; serviceId/assetId là alias theo từng loại endpoint.
     */
    public static class RoomLineRequest {
        @SerializedName("catalog_id")
        public String catalogId;

        @SerializedName("service_id")
        public String serviceId;

        @SerializedName("asset_id")
        public String assetId;

        public Integer quantity;
    }

    /** Request cập nhật số lượng của một dòng dịch vụ/tài sản đã gắn vào phòng. */
    public static class QuantityRequest {
        public Integer quantity;
    }

    // --- AUTH & USER DTOs ---
    /** Request đăng nhập bằng username/password từ màn LoginActivity. */
    public static class LoginRequest {
        public String username;
        public String password;
    }

    /** Dữ liệu tài khoản nhân viên trả về từ login và màn quản lý người dùng. */
    public static class UserDto {
        public String id;
        public String username;
        public String password;
        public String fullName;
        public String role;
        public String email;
        public String phone;
        public String position;
        @SerializedName("id_vaitro")
        public Integer idVaitro;
        @SerializedName(value = "gender", alternate = {"gioi_tinh"})
        public String gender;
        public Boolean active;
        public Boolean locked;
    }

    /** Vai trò lấy từ bảng vai_tro để nạp spinner chọn quyền nhân viên. */
    public static class RoleDto {
        @SerializedName("id_vaitro")
        public Integer idVaitro;

        @SerializedName("ten_vaitro")
        public String name;
    }

    /** Request cập nhật trạng thái phòng, ví dụ Trống/Bận/Bảo trì. */
    public static class StatusRequest {
        public String status;
    }

    // --- DASHBOARD STATS ---
    public static class DashboardStatsDto {
        @SerializedName("totalRooms")
        public Integer totalRooms;
        @SerializedName("occupiedRooms")
        public Integer occupiedRooms;
        @SerializedName("availableRooms")
        public Integer availableRooms;
        @SerializedName("maintenanceRooms")
        public Integer maintenanceRooms;
    }

    /**
     * DTO phòng từ API.
     * Dùng ở quản lý phòng, nhận phòng đổi phòng, và các flow cần biết id_phong.
     */
    public static class RoomDto {
        @SerializedName(value = "id", alternate = {"id_phong"})
        public Integer id;
        @SerializedName(value = "room_number", alternate = {"ten_phong"})
        public String roomNumber;
        @SerializedName(value = "room_type", alternate = {"loai_phong"})
        public String roomType;
        @SerializedName(value = "capacity", alternate = {"suc_chua"})
        public Integer capacity;
        @SerializedName(value = "price", alternate = {"gia_phong"})
        public Double price;
        @SerializedName(value = "status", alternate = {"trang_thai"})
        public String status;
        @SerializedName("id_ct_dat_phong")
        public Integer bookingDetailId;
    }

    /**
     * Dữ liệu checkout đã được backend tính phí phòng, dịch vụ, bồi thường, cọc và số tiền cần trả/hoàn.
     *
     * Android chủ yếu hiển thị các số này, không tự ghi vào database.
     * Nếu cần thanh toán, CheckoutFragment sẽ dùng maDatPhong/idHoaDon/idLuutru để gọi endpoint thanh toán.
     */
    public static class CheckoutDto {
        // Định danh hóa đơn/lưu trú/đặt phòng dùng cho flow thanh toán.
        @SerializedName("id_hoadon")
        public Integer idHoaDon;

        @SerializedName("id_luutru")
        public Integer idLuutru;

        @SerializedName("ma_dat_phong")
        public String maDatPhong;

        // Thông tin khách và phòng đang checkout.
        @SerializedName("customer_name")
        public String customerName;

        @SerializedName("customer_phone")
        public String customerPhone;

        public String email;

        @SerializedName("room_names")
        public String roomNames;

        // Thời gian check-in/check-out dự kiến để hiển thị khoảng lưu trú.
        @SerializedName("checkin_at")
        public String checkinAt;

        @SerializedName("expected_checkout_at")
        public String expectedCheckoutAt;

        @SerializedName("total_guests")
        public Integer totalGuests;

        public Integer adults;
        public Integer children;

        // Thông tin số khách và số đêm tính phí.
        @SerializedName("chargeable_nights")
        public Integer chargeableNights;

        // Các thành phần tiền của hóa đơn.
        @SerializedName("room_fee")
        public Double roomFee;

        @SerializedName("service_fee")
        public Double serviceFee;

        @SerializedName("damage_fee")
        public Double damageFee;

        public Double deposit;

        @SerializedName("gross_total")
        public Double grossTotal;

        @SerializedName("amount_due")
        public Double amountDue;

        @SerializedName("refund_amount")
        public Double refundAmount;

        @SerializedName("invoice_status")
        public String invoiceStatus;

        @SerializedName("payment_url")
        public String paymentUrl;
    }

    /** Request xác nhận nhận phòng, gửi CCCD và ghi chú để backend tạo bản ghi luu_tru. */
    public static class CheckInRequest {
        public String cccd;
        public String note;
    }

    /**
     * Request thanh toán hóa đơn checkout.
     * Backend dùng idLuutru/maDatPhong để đóng lưu trú và cập nhật trạng thái phòng.
     */
    public static class PaymentRequest {
        @SerializedName("phuong_thuc")
        public String paymentMethod;

        @SerializedName("so_tien")
        public Double amount;

        @SerializedName("id_luutru")
        public Integer idLuutru;

        @SerializedName("ma_dat_phong")
        public String maDatPhong;

        @SerializedName("ghi_chu")
        public String note;

        @SerializedName("yeu_cau_vat")
        public Boolean requestVat;
    }

    /** Request khóa/mở khóa tài khoản nhân viên. */
    public static class UserLockRequest {
        public Boolean locked;
        public Boolean active;
    }

    /** Request đổi phòng trước khi nhận phòng. */
    public static class ChangeRoomRequest {
        @SerializedName("new_room_id")
        public Integer newRoomId;
        @SerializedName("old_room_id")
        public Integer oldRoomId;
        public String reason;
    }

    /** Request đổi mật khẩu từ màn Profile. */
    public static class ChangePasswordRequest {
        @SerializedName("current_password")
        public String currentPassword;
        @SerializedName("new_password")
        public String newPassword;
    }

    // --- FORGOT PASSWORD MODELS ---
    /** Request bắt đầu flow quên mật khẩu bằng username/email/số điện thoại. */
    public static class ForgotPasswordRequest {
        public String identity;
    }

    /** Request xác thực OTP trong flow quên mật khẩu. */
    public static class VerifyOtpRequest {
        public String identity;
        public String otp;
    }

    /** Request đặt lại mật khẩu sau khi OTP hợp lệ. */
    public static class ResetPasswordRequest {
        public String identity;
        public String otp;
        @SerializedName("newPassword")
        public String newPassword;
    }
    
    public static class IdentityResponse {
        public String identity;
    }
}
