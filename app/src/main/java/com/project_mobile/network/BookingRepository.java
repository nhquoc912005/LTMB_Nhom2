package com.project_mobile.network;

import android.util.Log;

import com.project_mobile.datphong_mobile.Booking;
import com.project_mobile.network.ApiModels.ApiResponse;
import com.project_mobile.network.ApiModels.BookingDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepository {
    private final ApiService api;

    public BookingRepository() {
        api = ApiClient.getClient().create(ApiService.class);
    }

    public interface CallbackList {
        void onSuccess(List<Booking> bookings);
        void onError(String error);
    }

    public void fetchBookings(CallbackList cb) {
        Call<ApiResponse<List<BookingDto>>> call = api.getBookings();
        call.enqueue(new Callback<ApiResponse<List<BookingDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDto>>> call, Response<ApiResponse<List<BookingDto>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    String err = "Network error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String eb = response.errorBody().string();
                            if (eb != null && !eb.isEmpty()) err += " -> " + eb;
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                    cb.onError(err);
                    return;
                }

                ApiResponse<List<BookingDto>> apiResp = response.body();
                if (!apiResp.success) {
                    cb.onError(apiResp.error != null ? apiResp.error : "Unknown API error");
                    return;
                }

                List<Booking> mapped = new ArrayList<>();
                if (apiResp.data != null) {
                    for (BookingDto d : apiResp.data) {
                        // map DTO to UI model (simple mapping)
                        String roomName = d.roomNumber != null ? d.roomNumber : "";
                        String status = d.status != null ? d.status : "";
                        String customerName = d.customerName != null ? d.customerName : "";
                        String email = d.email != null ? d.email : "";
                        String phone = d.phone != null ? d.phone : "";
                        String checkIn = d.checkIn != null ? d.checkIn : "";
                        String checkOut = d.checkOut != null ? d.checkOut : "";
                        String totalPrice = d.totalAmount != null ? d.totalAmount : "";

                        Booking b = new Booking(roomName, status, customerName, email, phone, checkIn, checkOut, totalPrice);
                        mapped.add(b);
                    }
                }

                cb.onSuccess(mapped);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingDto>>> call, Throwable t) {
                Log.e("BookingRepo", "fetchBookings failed", t);
                cb.onError(t.getMessage());
            }
        });
    }

    // Check availability callback
    public interface CallbackBoolean {
        void onResult(boolean available);
        void onError(String error);
    }

    public void checkAvailability(String roomNumber, String checkIn, String checkOut, CallbackBoolean cb) {
        Call<ApiResponse<Boolean>> call = api.checkAvailability(roomNumber, checkIn, checkOut);
        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onError("Network error: " + response.code());
                    return;
                }
                ApiResponse<Boolean> apiResp = response.body();
                if (!apiResp.success) {
                    cb.onError(apiResp.error != null ? apiResp.error : "Unknown API error");
                    return;
                }
                cb.onResult(apiResp.data != null && apiResp.data);
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e("BookingRepo", "checkAvailability failed", t);
                cb.onError(t.getMessage());
            }
        });
    }

    // Generic single-booking callback
    public interface CallbackBooking {
        void onSuccess(Booking booking);
        void onError(String error);
    }

    private Booking mapDtoToBooking(BookingDto d) {
        String roomName = d.roomNumber != null ? d.roomNumber : "";
        String status = d.status != null ? d.status : "";
        String customerName = d.customerName != null ? d.customerName : "";
        String email = d.email != null ? d.email : "";
        String phone = d.phone != null ? d.phone : "";
        String checkIn = d.checkIn != null ? d.checkIn : "";
        String checkOut = d.checkOut != null ? d.checkOut : "";
        String totalPrice = d.totalAmount != null ? d.totalAmount : "";
        return new Booking(roomName, status, customerName, email, phone, checkIn, checkOut, totalPrice);
    }

    public void createBooking(ApiModels.CreateBookingRequest req, CallbackBooking cb) {
        Call<ApiResponse<BookingDto>> call = api.createBooking(req);
        call.enqueue(new Callback<ApiResponse<BookingDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDto>> call, Response<ApiResponse<BookingDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onError("Network error: " + response.code());
                    return;
                }
                ApiResponse<BookingDto> apiResp = response.body();
                if (!apiResp.success) {
                    cb.onError(apiResp.error != null ? apiResp.error : "Unknown API error");
                    return;
                }
                cb.onSuccess(mapDtoToBooking(apiResp.data));
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDto>> call, Throwable t) {
                Log.e("BookingRepo", "createBooking failed", t);
                cb.onError(t.getMessage());
            }
        });
    }

    private void performSimplePut(String id, Call<ApiResponse<BookingDto>> call, CallbackBooking cb) {
        call.enqueue(new Callback<ApiResponse<BookingDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDto>> call, Response<ApiResponse<BookingDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onError("Network error: " + response.code());
                    return;
                }
                ApiResponse<BookingDto> apiResp = response.body();
                if (!apiResp.success) {
                    cb.onError(apiResp.error != null ? apiResp.error : "Unknown API error");
                    return;
                }
                cb.onSuccess(mapDtoToBooking(apiResp.data));
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDto>> call, Throwable t) {
                Log.e("BookingRepo", "PUT operation failed", t);
                cb.onError(t.getMessage());
            }
        });
    }

    public void cancelBooking(String id, CallbackBooking cb) {
        performSimplePut(id, api.cancelBooking(id), cb);
    }

    public void confirmBooking(String id, CallbackBooking cb) {
        performSimplePut(id, api.confirmBooking(id), cb);
    }

    public void checkInBooking(String id, CallbackBooking cb) {
        performSimplePut(id, api.checkInBooking(id), cb);
    }

    public void checkOutBooking(String id, CallbackBooking cb) {
        performSimplePut(id, api.checkOutBooking(id), cb);
    }

    public void paymentBooking(String id, CallbackBooking cb) {
        performSimplePut(id, api.paymentBooking(id), cb);
    }
}

