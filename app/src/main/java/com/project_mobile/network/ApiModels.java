package com.project_mobile.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiModels {

    public static class ApiResponse<T> {
        public boolean success;
        public T data;
        public String message;
        public String error;
    }

    public static class BookingDto {
        @SerializedName(value = "ma_dat_phong", alternate = {"booking_id"})
        public String bookingId;

        @SerializedName(value = "room_number", alternate = {"room_names", "so_phong"})
        public String roomNumber;

        @SerializedName(value = "customer_name", alternate = {"ten_nguoi_dat"})
        public String customerName;

        public String email;
        @SerializedName(value = "customer_phone", alternate = {"sdt_nguoi_dat"})
        public String phone;

        @SerializedName("tong_so_nguoi")
        public Integer totalGuests;

        @SerializedName("so_nguoi_lon")
        public Integer adults;

        @SerializedName("so_tre_em")
        public Integer children;

        @SerializedName(value = "ngay_nhan", alternate = {"check_in", "activity_time"})
        public String checkIn;

        @SerializedName(value = "ngay_tra", alternate = {"check_out"})
        public String checkOut;

        @SerializedName("payment_method")
        public String paymentMethod;

        @SerializedName(value = "total_amount", alternate = {"tong_thanh_toan"})
        public Double totalAmount;

        public String note;
        @SerializedName("trang_thai")
        public String status;

        public List<RoomDto> rooms;
    }

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

    public static class CatalogItemRequest {
        public String name;
        public Double price;
        public String unit;
        public String icon;
    }

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

    public static class RoomLineDto {
        public String id;

        @SerializedName("room_id")
        public Integer roomId;

        @SerializedName("catalog_id")
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

    public static class RoomLineRequest {
        @SerializedName("catalog_id")
        public String catalogId;

        @SerializedName("service_id")
        public String serviceId;

        @SerializedName("asset_id")
        public String assetId;

        public Integer quantity;
    }

    public static class QuantityRequest {
        public Integer quantity;
    }

    // --- AUTH & USER DTOs ---
    public static class LoginRequest {
        public String username;
        public String password;
    }

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
        public Boolean active;
        public Boolean locked;
    }

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

    public static class CheckoutDto {
        @SerializedName("id_hoadon")
        public Integer idHoaDon;

        @SerializedName("id_luutru")
        public Integer idLuutru;

        @SerializedName("ma_dat_phong")
        public String maDatPhong;

        @SerializedName("customer_name")
        public String customerName;

        @SerializedName("customer_phone")
        public String customerPhone;

        public String email;

        @SerializedName("room_names")
        public String roomNames;

        @SerializedName("checkin_at")
        public String checkinAt;

        @SerializedName("expected_checkout_at")
        public String expectedCheckoutAt;

        @SerializedName("total_guests")
        public Integer totalGuests;

        public Integer adults;
        public Integer children;

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

    public static class CheckInRequest {
        public String cccd;
        public String note;
    }

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

    public static class UserLockRequest {
        public Boolean locked;
        public Boolean active;
    }

    public static class ChangeRoomRequest {
        @SerializedName("new_room_id")
        public Integer newRoomId;
        @SerializedName("old_room_id")
        public Integer oldRoomId;
        public String reason;
    }

    public static class ChangePasswordRequest {
        @SerializedName("current_password")
        public String currentPassword;
        @SerializedName("new_password")
        public String newPassword;
    }

    // --- FORGOT PASSWORD MODELS ---
    public static class ForgotPasswordRequest {
        public String identity;
    }

    public static class VerifyOtpRequest {
        public String identity;
        public String otp;
    }

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
