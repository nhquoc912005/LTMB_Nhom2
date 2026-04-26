package com.project_mobile.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiModels {
    public static class ApiResponse<T> {
        public boolean success;
        public T data;
        public String error;
        public String message;
    }

    public static class BookingDto {
        @SerializedName("booking_id")
        public String bookingId;

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

        public String status;

        @SerializedName("payment_status")
        public String paymentStatus;

        @SerializedName("total_amount")
        public String totalAmount; // string to be safe, backend may return numeric

        public String note;
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
        public Integer id;
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
        public Integer bookingId;

        @SerializedName("stay_id")
        public Integer stayId;

        @SerializedName("expected_check_in")
        public String expectedCheckIn;

        @SerializedName("expected_check_out")
        public String expectedCheckOut;

        @SerializedName("room_fee")
        public Double roomFee;
    }

    public static class RoomLineDto {
        public Integer id;

        @SerializedName("room_id")
        public Integer roomId;

        @SerializedName("catalog_id")
        public Integer catalogId;

        @SerializedName("service_id")
        public Integer serviceId;

        @SerializedName("asset_id")
        public Integer assetId;

        public String name;
        public Integer quantity;
        public Double price;

        @SerializedName("total_price")
        public Double totalPrice;

        public String unit;
        public String icon;
    }

    public static class RoomLineRequest {
        @SerializedName("catalog_id")
        public Integer catalogId;

        @SerializedName("service_id")
        public Integer serviceId;

        @SerializedName("asset_id")
        public Integer assetId;

        public Integer quantity;
    }

    public static class QuantityRequest {
        public Integer quantity;
    }
}

