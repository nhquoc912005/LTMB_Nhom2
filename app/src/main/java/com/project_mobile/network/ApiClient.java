// Module network/API Android.
// File này tạo Retrofit singleton dùng chung cho toàn bộ app khi gọi backend.
// Dữ liệu chính là BASE_URL, OkHttp interceptor log body và Gson converter.
package com.project_mobile.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient cấu hình HTTP client dùng chung.
 * getClient() trả về một instance Retrofit duy nhất để tránh tạo client lặp lại ở mỗi màn hình.
 */
public class ApiClient {
    // NOTE: for emulator use 10.0.2.2 to reach host machine localhost:3000
    // BASE_URL moved to NetworkConfig for easier configuration
    public static final String BASE_URL = NetworkConfig.BASE_URL;

    private static Retrofit retrofit;

    /** Khởi tạo Retrofit lần đầu, các lần sau tái sử dụng cùng instance. */
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

