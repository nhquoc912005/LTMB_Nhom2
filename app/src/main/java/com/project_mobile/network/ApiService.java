// Module network/API Android.
// File này khai báo toàn bộ endpoint Retrofit mà app mobile dùng để gọi Node.js API.
// Dữ liệu chính đi qua file gồm booking, check-in, checkout, phòng, dịch vụ/tài sản và người dùng.
/*
 * File: ApiService.java
 * Module: Network/API contract Android.
 *
 * Chức năng:
 * - Khai báo toàn bộ endpoint Retrofit mà app gọi sang Node.js API.
 * - Mỗi method mô tả HTTP method, path, query/path param và body tương ứng.
 * - Các Fragment/Repository không tự build URL thủ công mà gọi qua interface này.
 *
 * Cách đọc:
 * - @GET/@POST/@PUT/@DELETE là method HTTP.
 * - @Path lấy giá trị đưa vào URL.
 * - @Query đưa dữ liệu lên query string.
 * - @Body gửi JSON request body.
 */
package com.project_mobile.network;

import com.project_mobile.network.ApiModels.ApiResponse;
import com.project_mobile.network.ApiModels.ActiveRoomDto;
import com.project_mobile.network.ApiModels.BookingDto;
import com.project_mobile.network.ApiModels.CatalogItemDto;
import com.project_mobile.network.ApiModels.CatalogItemRequest;
import com.project_mobile.network.ApiModels.QuantityRequest;
import com.project_mobile.network.ApiModels.RoomLineDto;
import com.project_mobile.network.ApiModels.RoomLineRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * ApiService là hợp đồng giữa Android và backend Express.
 * Mỗi method tương ứng một endpoint, Retrofit sẽ map path/query/body sang HTTP request.
 */
public interface ApiService {
    // Đặt phòng: lấy danh sách, chi tiết, kiểm tra trùng lịch, tạo mới và đổi trạng thái đặt phòng.
    @GET("/api/bookings")
    Call<ApiResponse<List<BookingDto>>> getBookings();

    @GET("/api/bookings/{id}")
    Call<ApiResponse<BookingDto>> getBooking(@Path("id") String id);

    @GET("/api/bookings/check-availability")
    Call<ApiResponse<Boolean>> checkAvailability(@Query("room_number") String roomNumber,
                                                 @Query("check_in") String checkIn,
                                                 @Query("check_out") String checkOut);

    @POST("/api/bookings")
    Call<ApiResponse<BookingDto>> createBooking(@Body com.project_mobile.network.ApiModels.CreateBookingRequest req);

    @PUT("/api/bookings/{id}/cancel")
    Call<ApiResponse<BookingDto>> cancelBooking(@Path("id") String id);

    @PUT("/api/bookings/{id}/confirm")
    Call<ApiResponse<BookingDto>> confirmBooking(@Path("id") String id);

    @PUT("/api/bookings/{id}/check-in")
    Call<ApiResponse<BookingDto>> checkInBooking(@Path("id") String id, @Body ApiModels.CheckInRequest req);

    // --- Services & Assets ---
    // Danh mục dịch vụ và tài sản: dùng cho màn quản lý danh mục và chọn thêm vào phòng đang lưu trú.
    @GET("/api/services")
    Call<ApiResponse<List<CatalogItemDto>>> getServices(@Query("q") String query);

    @POST("/api/services")
    Call<ApiResponse<CatalogItemDto>> createService(@Body CatalogItemRequest req);

    @PUT("/api/services/{id}")
    Call<ApiResponse<CatalogItemDto>> updateService(@Path("id") String id, @Body CatalogItemRequest req);

    @DELETE("/api/services/{id}")
    Call<ApiResponse<CatalogItemDto>> deleteService(@Path("id") String id);

    @GET("/api/assets")
    Call<ApiResponse<List<CatalogItemDto>>> getAssets(@Query("q") String query);

    @POST("/api/assets")
    Call<ApiResponse<CatalogItemDto>> createAsset(@Body CatalogItemRequest req);

    @PUT("/api/assets/{id}")
    Call<ApiResponse<CatalogItemDto>> updateAsset(@Path("id") String id, @Body CatalogItemRequest req);

    @DELETE("/api/assets/{id}")
    Call<ApiResponse<CatalogItemDto>> deleteAsset(@Path("id") String id);

    @GET("/api/active-rooms")
    Call<ApiResponse<List<ActiveRoomDto>>> getActiveRooms(@Query("q") String query);

    // --- Room Specific Items ---
    // Dòng dịch vụ/tài sản của từng phòng: dùng trong RoomMapFragment khi xem phòng đang có khách.
    @GET("/api/rooms/{roomId}/room-services")
    Call<ApiResponse<List<RoomLineDto>>> getRoomServices(@Path("roomId") int roomId);

    @POST("/api/rooms/{roomId}/room-services")
    Call<ApiResponse<RoomLineDto>> addRoomService(@Path("roomId") int roomId, @Body RoomLineRequest req);

    @PUT("/api/room-services/{id}")
    Call<ApiResponse<RoomLineDto>> updateRoomService(@Path("id") String id, @Body QuantityRequest req);

    @DELETE("/api/room-services/{id}")
    Call<ApiResponse<RoomLineDto>> deleteRoomService(@Path("id") String id);

    @GET("/api/rooms/{roomId}/room-assets")
    Call<ApiResponse<List<RoomLineDto>>> getRoomAssets(@Path("roomId") int roomId);

    @POST("/api/rooms/{roomId}/room-assets")
    Call<ApiResponse<RoomLineDto>> addRoomAsset(@Path("roomId") int roomId, @Body RoomLineRequest req);

    @PUT("/api/room-assets/{id}")
    Call<ApiResponse<RoomLineDto>> updateRoomAsset(@Path("id") String id, @Body QuantityRequest req);

    @DELETE("/api/room-assets/{id}")
    Call<ApiResponse<RoomLineDto>> deleteRoomAsset(@Path("id") String id);

    // --- Auth & Users ---
    // Xác thực, quên mật khẩu và CRUD nhân viên/tài khoản.
    @POST("/api/auth/login")
    Call<ApiResponse<ApiModels.UserDto>> login(@Body ApiModels.LoginRequest req);

    @POST("/api/auth/forgot-password")
    Call<ApiResponse<ApiModels.IdentityResponse>> forgotPassword(@Body ApiModels.ForgotPasswordRequest req);

    @POST("/api/auth/verify-otp")
    Call<ApiResponse<Void>> verifyOtp(@Body ApiModels.VerifyOtpRequest req);

    @POST("/api/auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body ApiModels.ResetPasswordRequest req);

    @GET("/api/users")
    Call<ApiResponse<List<ApiModels.UserDto>>> getUsers();

    @GET("/api/users/roles")
    Call<ApiResponse<List<ApiModels.RoleDto>>> getUserRoles();

    @POST("/api/users")
    Call<ApiResponse<ApiModels.UserDto>> createUser(@Body ApiModels.UserDto req);

    @PUT("/api/users/{id}")
    Call<ApiResponse<ApiModels.UserDto>> updateUser(@Path("id") String id, @Body ApiModels.UserDto req);

    @PUT("/api/users/{id}/lock")
    Call<ApiResponse<ApiModels.UserDto>> updateUserLock(@Path("id") String id, @Body ApiModels.UserLockRequest req);

    @DELETE("/api/users/{id}")
    Call<ApiResponse<ApiModels.UserDto>> deleteUser(@Path("id") String id);

    @GET("/api/rooms")
    Call<ApiResponse<List<ApiModels.RoomDto>>> getRooms();

    @PUT("/api/rooms/{id}/status")
    Call<ApiResponse<ApiModels.RoomDto>> updateRoomStatus(@Path("id") int id, @Body ApiModels.StatusRequest req);

    // Stats
    // Dashboard trang chủ: số phòng theo trạng thái và hoạt động gần đây.
    @GET("/api/stats")
    Call<ApiResponse<ApiModels.DashboardStatsDto>> getStats();

    @GET("/api/dashboard/activities")
    Call<ApiResponse<List<BookingDto>>> getDashboardActivities();

    // Nhận phòng: lấy danh sách booking đủ điều kiện check-in theo ngày và từ khóa.
    @GET("/api/check-in/bookings")
    Call<ApiResponse<List<ApiModels.BookingDto>>> getCheckInBookings(
        @Query("from") String from,
        @Query("to") String to,
        @Query("q") String query
    );

    // Trả phòng: lấy các lưu trú đang mở, backend đã tính sẵn phí và số tiền cần thanh toán.
    @GET("/api/v2/checkouts")
    Call<ApiResponse<List<ApiModels.CheckoutDto>>> getCheckouts(
        @Query("date") String date,
        @Query("q") String query
    );

    // Tạo/cập nhật hóa đơn nháp trước khi thanh toán để số tiền luôn lấy từ backend mới nhất.
    @POST("/api/v2/checkouts/{maDatPhong}/draft-bill")
    Call<ApiResponse<ApiModels.CheckoutDto>> createCheckoutDraft(@Path("maDatPhong") String maDatPhong);

    // Thanh toán hóa đơn; backend sẽ đóng lưu trú và cập nhật trạng thái phòng/booking.
    @POST("/api/v2/invoices/{idHoaDon}/pay")
    Call<ApiResponse<Object>> payInvoice(@Path("idHoaDon") int idHoaDon, @Body ApiModels.PaymentRequest req);

    // Lấy phòng trống có thể đổi sang trước khi nhận phòng.
    @GET("/api/check-in/bookings/{maDatPhong}/available-rooms")
    Call<ApiResponse<List<ApiModels.RoomDto>>> getAvailableRooms(@Path("maDatPhong") String maDatPhong);

    // Xác nhận nhận phòng; backend tạo bản ghi luu_tru và cập nhật trạng thái phòng.
    @POST("/api/check-in/bookings/{maDatPhong}/confirm")
    Call<ApiResponse<Object>> confirmCheckIn(@Path("maDatPhong") String maDatPhong, @Body ApiModels.CheckInRequest req);

    // Đổi phòng trong chi tiết đặt phòng trước khi check-in.
    @POST("/api/check-in/bookings/{maDatPhong}/change-room")
    Call<ApiResponse<Void>> changeRoom(@Path("maDatPhong") String maDatPhong, @Body ApiModels.ChangeRoomRequest req);

    @PUT("/api/users/{id}/change-password")
    Call<ApiResponse<Void>> changePassword(@Path("id") String id, @Body ApiModels.ChangePasswordRequest req);
}
