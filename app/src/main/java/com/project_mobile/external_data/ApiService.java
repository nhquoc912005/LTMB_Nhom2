package com.project_mobile.external_data;

import com.project_mobile.check_in.CheckInModel;
import com.project_mobile.external_data.ExternalData;
import com.project_mobile.external_data.PageResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    
    @GET("api/data")
    Call<PageResponse<ExternalData>> getAllData();

    @GET("api/data/search")
    Call<PageResponse<ExternalData>> searchByName(@Query("keyword") String keyword);

    @POST("api/auth/login")
    Call<LoginResponse> login(@retrofit2.http.Body LoginRequest request);

    @GET("api/checkin/pending")
    Call<java.util.List<CheckInModel>> getPendingCheckIns();

    @POST("api/checkin/confirm/{id}")
    Call<okhttp3.ResponseBody> confirmCheckIn(@retrofit2.http.Path("id") int id);
}
