package com.project_mobile.external_data;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.project_mobile.R;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExternalDataActivity extends AppCompatActivity {

    private EditText etSearch;
    private Button btnSearch;
    private RecyclerView rvData;
    private ExternalDataAdapter adapter;
    private List<ExternalData> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_data);

        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        rvData = findViewById(R.id.rvExternalData);

        rvData.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExternalDataAdapter(dataList);
        rvData.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchData(keyword);
            } else {
                fetchData();
            }
        });

        // Initial fetch
        fetchData();
    }

    private void fetchData() {
        RetrofitClient.getApiService().getAllData().enqueue(new Callback<PageResponse<ExternalData>>() {
            @Override
            public void onResponse(Call<PageResponse<ExternalData>> call, Response<PageResponse<ExternalData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dataList = response.body().getContent();
                    adapter.setDataList(dataList);
                } else {
                    Toast.makeText(ExternalDataActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ExternalData>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(ExternalDataActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchData(String keyword) {
        RetrofitClient.getApiService().searchByName(keyword).enqueue(new Callback<PageResponse<ExternalData>>() {
            @Override
            public void onResponse(Call<PageResponse<ExternalData>> call, Response<PageResponse<ExternalData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dataList = response.body().getContent();
                    adapter.setDataList(dataList);
                } else {
                    Toast.makeText(ExternalDataActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ExternalData>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(ExternalDataActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
