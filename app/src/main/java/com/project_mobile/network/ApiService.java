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

public interface ApiService {
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
    Call<ApiResponse<BookingDto>> checkInBooking(@Path("id") String id);

    @PUT("/api/bookings/{id}/check-out")
    Call<ApiResponse<BookingDto>> checkOutBooking(@Path("id") String id);

    @PUT("/api/bookings/{id}/payment")
    Call<ApiResponse<BookingDto>> paymentBooking(@Path("id") String id);

    // --- Services & Assets ---
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
    @POST("/api/auth/login")
    Call<ApiResponse<ApiModels.UserDto>> login(@Body ApiModels.LoginRequest req);

    @GET("/api/users")
    Call<ApiResponse<List<ApiModels.UserDto>>> getUsers();

    @POST("/api/users")
    Call<ApiResponse<ApiModels.UserDto>> createUser(@Body ApiModels.UserDto req);

    @PUT("/api/users/{id}")
    Call<ApiResponse<ApiModels.UserDto>> updateUser(@Path("id") String id, @Body ApiModels.UserDto req);

    @DELETE("/api/users/{id}")
    Call<ApiResponse<ApiModels.UserDto>> deleteUser(@Path("id") String id);

    @GET("/api/rooms")
    Call<ApiResponse<List<ApiModels.RoomDto>>> getRooms();

    @PUT("/api/rooms/{id}/status")
    Call<ApiResponse<ApiModels.RoomDto>> updateRoomStatus(@Path("id") int id, @Body ApiModels.StatusRequest req);

    // Stats
    @GET("/api/stats")
    Call<ApiResponse<ApiModels.DashboardStatsDto>> getStats();

    @GET("/api/dashboard/activities")
    Call<ApiResponse<List<BookingDto>>> getDashboardActivities();

    @GET("/api/check-in/bookings")
    Call<ApiResponse<List<ApiModels.BookingDto>>> getCheckInBookings(
        @Query("from") String from,
        @Query("to") String to,
        @Query("q") String query
    );

    @GET("/api/v2/checkouts")
    Call<ApiResponse<List<ApiModels.CheckoutDto>>> getCheckouts(
        @Query("date") String date,
        @Query("q") String query
    );

    @GET("/api/check-in/bookings/{maDatPhong}/available-rooms")
    Call<ApiResponse<List<ApiModels.RoomDto>>> getAvailableRooms(@Path("maDatPhong") String maDatPhong);

    @POST("/api/check-in/bookings/{maDatPhong}/change-room")
    Call<ApiResponse<Void>> changeRoom(@Path("maDatPhong") String maDatPhong, @Body ApiModels.ChangeRoomRequest req);

    @PUT("/api/users/{id}/change-password")
    Call<ApiResponse<Void>> changePassword(@Path("id") String id, @Body ApiModels.ChangePasswordRequest req);
}
