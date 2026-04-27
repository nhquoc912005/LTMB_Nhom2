package com.project_mobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import com.project_mobile.network.ApiClient;
import com.project_mobile.network.ApiModels;
import com.project_mobile.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private EditText etIdentity, etOtp, etNewPassword, etConfirmPassword;
    private TextView tvOtpDescription;
    private ApiService apiService;
    private String currentIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews();
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        etIdentity = findViewById(R.id.etIdentity);
        etOtp = findViewById(R.id.etOtp);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvOtpDescription = findViewById(R.id.tvOtpDescription);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() > 0 && viewFlipper.getDisplayedChild() < 3) {
                viewFlipper.showPrevious();
            } else {
                finish();
            }
        });

        findViewById(R.id.btnSendOtp).setOnClickListener(v -> handleSendOtp());
        findViewById(R.id.btnVerifyOtp).setOnClickListener(v -> handleVerifyOtp());
        findViewById(R.id.btnResetPassword).setOnClickListener(v -> handleResetPassword());
        findViewById(R.id.btnBackToLogin).setOnClickListener(v -> finish());
    }

    private void handleSendOtp() {
        String identity = etIdentity.getText().toString().trim();
        if (TextUtils.isEmpty(identity)) {
            etIdentity.setError("Vui lòng nhập email hoặc số điện thoại");
            return;
        }

        ApiModels.ForgotPasswordRequest req = new ApiModels.ForgotPasswordRequest();
        req.identity = identity;

        apiService.forgotPassword(req).enqueue(new Callback<ApiModels.ApiResponse<ApiModels.IdentityResponse>>() {
            @Override
            public void onResponse(Call<ApiModels.ApiResponse<ApiModels.IdentityResponse>> call, Response<ApiModels.ApiResponse<ApiModels.IdentityResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    currentIdentity = identity;
                    tvOtpDescription.setText("Nhập mã 4 chữ số chúng tôi vừa gửi đến " + identity);
                    viewFlipper.setDisplayedChild(1);
                } else {
                    String msg = (response.body() != null) ? response.body().message : "Không tìm thấy tài khoản";
                    Toast.makeText(ForgotPasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.ApiResponse<ApiModels.IdentityResponse>> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleVerifyOtp() {
        String otp = etOtp.getText().toString().trim();
        if (otp.length() < 4) {
            etOtp.setError("Nhập đủ 4 số");
            return;
        }

        ApiModels.VerifyOtpRequest req = new ApiModels.VerifyOtpRequest();
        req.identity = currentIdentity;
        req.otp = otp;

        apiService.verifyOtp(req).enqueue(new Callback<ApiModels.ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiModels.ApiResponse<Void>> call, Response<ApiModels.ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    viewFlipper.setDisplayedChild(2);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Mã xác thực không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleResetPassword() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (newPass.length() < 6) {
            etNewPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        ApiModels.ResetPasswordRequest req = new ApiModels.ResetPasswordRequest();
        req.identity = currentIdentity;
        req.otp = etOtp.getText().toString().trim();
        req.newPassword = newPass;

        apiService.resetPassword(req).enqueue(new Callback<ApiModels.ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiModels.ApiResponse<Void>> call, Response<ApiModels.ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    viewFlipper.setDisplayedChild(3);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Đã có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
