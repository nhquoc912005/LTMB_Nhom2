package com.project_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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

        com.project_mobile.network.ApiModels.LoginRequest req = new com.project_mobile.network.ApiModels.LoginRequest();
        req.username = username;
        req.password = password;

        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.login(req).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    com.project_mobile.network.ApiModels.UserDto user = response.body().data;
                    
                    // Save session
                    com.project_mobile.common.SessionManager sessionManager = new com.project_mobile.common.SessionManager(LoginActivity.this);
                    sessionManager.saveUser(user);
                    
                    Toast.makeText(LoginActivity.this, "Chào mừng " + user.fullName, Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Đăng nhập thất bại";
                    if (response.body() != null && response.body().message != null) {
                        msg = response.body().message;
                    }
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
