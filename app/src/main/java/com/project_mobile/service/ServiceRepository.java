package com.project_mobile.service;

import android.util.Log;

import com.project_mobile.network.ApiClient;
import com.project_mobile.network.ApiModels;
import com.project_mobile.network.ApiModels.ActiveRoomDto;
import com.project_mobile.network.ApiModels.ApiResponse;
import com.project_mobile.network.ApiModels.CatalogItemDto;
import com.project_mobile.network.ApiModels.CatalogItemRequest;
import com.project_mobile.network.ApiModels.QuantityRequest;
import com.project_mobile.network.ApiModels.RoomLineDto;
import com.project_mobile.network.ApiModels.RoomLineRequest;
import com.project_mobile.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRepository {
    private final ApiService api;

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public ServiceRepository() {
        api = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchCatalog(boolean serviceTab, String query, DataCallback<List<CatalogItemDto>> callback) {
        Call<ApiResponse<List<CatalogItemDto>>> call = serviceTab
                ? api.getServices(query)
                : api.getAssets(query);
        enqueue(call, callback);
    }

    public void saveCatalog(boolean serviceTab, Integer id, String name, double price, String unit, DataCallback<CatalogItemDto> callback) {
        CatalogItemRequest req = new CatalogItemRequest();
        req.name = name;
        req.price = price;
        req.unit = unit;
        req.icon = null;

        Call<ApiResponse<CatalogItemDto>> call;
        if (id == null) {
            call = serviceTab ? api.createService(req) : api.createAsset(req);
        } else {
            call = serviceTab ? api.updateService(id, req) : api.updateAsset(id, req);
        }
        enqueue(call, callback);
    }

    public void deleteCatalog(boolean serviceTab, int id, DataCallback<CatalogItemDto> callback) {
        Call<ApiResponse<CatalogItemDto>> call = serviceTab ? api.deleteService(id) : api.deleteAsset(id);
        enqueue(call, callback);
    }

    public void fetchActiveRooms(String query, DataCallback<List<ActiveRoomDto>> callback) {
        enqueue(api.getActiveRooms(query), callback);
    }

    public void fetchRoomLines(boolean serviceTab, int roomId, DataCallback<List<RoomLineDto>> callback) {
        Call<ApiResponse<List<RoomLineDto>>> call = serviceTab
                ? api.getRoomServices(roomId)
                : api.getRoomAssets(roomId);
        enqueue(call, callback);
    }

    public void addRoomLine(boolean serviceTab, int roomId, int catalogId, int quantity, DataCallback<RoomLineDto> callback) {
        RoomLineRequest req = new RoomLineRequest();
        req.catalogId = catalogId;
        req.quantity = quantity;
        if (serviceTab) {
            req.serviceId = catalogId;
        } else {
            req.assetId = catalogId;
        }

        Call<ApiResponse<RoomLineDto>> call = serviceTab
                ? api.addRoomService(roomId, req)
                : api.addRoomAsset(roomId, req);
        enqueue(call, callback);
    }

    public void updateRoomLine(boolean serviceTab, int lineId, int quantity, DataCallback<RoomLineDto> callback) {
        QuantityRequest req = new QuantityRequest();
        req.quantity = quantity;
        Call<ApiResponse<RoomLineDto>> call = serviceTab
                ? api.updateRoomService(lineId, req)
                : api.updateRoomAsset(lineId, req);
        enqueue(call, callback);
    }

    public void deleteRoomLine(boolean serviceTab, int lineId, DataCallback<RoomLineDto> callback) {
        Call<ApiResponse<RoomLineDto>> call = serviceTab
                ? api.deleteRoomService(lineId)
                : api.deleteRoomAsset(lineId);
        enqueue(call, callback);
    }

    private <T> void enqueue(Call<ApiResponse<T>> call, DataCallback<T> callback) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(readHttpError(response));
                    return;
                }

                ApiModels.ApiResponse<T> body = response.body();
                if (!body.success) {
                    callback.onError(body.message != null ? body.message : body.error);
                    return;
                }
                callback.onSuccess(body.data);
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                Log.e("ServiceRepository", "API call failed", t);
                callback.onError(t.getMessage());
            }
        });
    }

    private String readHttpError(Response<?> response) {
        String fallback = "Network error: " + response.code();
        try {
            if (response.errorBody() != null) {
                String error = response.errorBody().string();
                if (error != null && !error.isEmpty()) {
                    return error;
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }
}
