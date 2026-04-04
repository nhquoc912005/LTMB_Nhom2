package com.project_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
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
        // Here you would typically perform network authentication
        // For now, we will just simulate a successful login
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic mock logic: any non-empty username/password is "valid"
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

        // Redirect to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close LoginActivity
    }
}
