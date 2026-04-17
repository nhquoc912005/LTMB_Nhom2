package com.project_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Set click listener for login button
        btnLogin.setOnClickListener(v -> {
            if (validateInput()) {
                performLogin();
            }
        });

        // Set click listener for forgot password (example)
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInput() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Vui lòng nhập tài khoản hoặc email");
            etUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Show a "Logging in..." message
        Toast.makeText(this, "Đang xác thực...", Toast.LENGTH_SHORT).show();

        com.project_mobile.external_data.LoginRequest loginRequest = new com.project_mobile.external_data.LoginRequest(username, password);
        
        com.project_mobile.external_data.RetrofitClient.getApiService().login(loginRequest)
            .enqueue(new retrofit2.Callback<com.project_mobile.external_data.LoginResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.project_mobile.external_data.LoginResponse> call, retrofit2.Response<com.project_mobile.external_data.LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(LoginActivity.this, "Chào mừng " + response.body().getUsername() + "!", Toast.LENGTH_SHORT).show();
                        // Redirect to MainActivity (Overview)
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.project_mobile.external_data.LoginResponse> call, Throwable t) {
                    Log.e("LoginActivity", "Login error: " + t.getMessage());
                    Toast.makeText(LoginActivity.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}
