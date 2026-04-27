package com.project_mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.project_mobile.common.SessionManager;
import com.project_mobile.network.ApiModels.UserDto;

public class ProfileFragment extends Fragment {

    private TextView tvProfileFullName, tvProfileUsername, tvProfilePhone, tvProfileEmail, tvProfileRole;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileFullName = view.findViewById(R.id.tvProfileFullName);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);

        sessionManager = new SessionManager(requireContext());
        loadUserData();

        view.findViewById(R.id.btnProfileChangePassword).setOnClickListener(v -> showChangePasswordDialog());

        return view;
    }

    private void showChangePasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.EditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
        android.widget.EditText etNew = dialogView.findViewById(R.id.etNewPassword);
        android.widget.EditText etConfirm = dialogView.findViewById(R.id.etConfirmPassword);
        
        dialogView.findViewById(R.id.btnCancelChange).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btnConfirmChange).setOnClickListener(v -> {
            String current = etCurrent.getText().toString();
            String newPass = etNew.getText().toString();
            String confirm = etConfirm.getText().toString();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirm)) {
                Toast.makeText(getContext(), "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            performChangePassword(current, newPass, dialog);
        });

        dialog.show();
    }

    private void performChangePassword(String current, String newPass, android.app.AlertDialog dialog) {
        UserDto user = sessionManager.getUser();
        if (user == null || user.id == null) return;

        com.project_mobile.network.ApiModels.ChangePasswordRequest req = new com.project_mobile.network.ApiModels.ChangePasswordRequest();
        req.currentPassword = current;
        req.newPassword = newPass;

        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.changePassword(user.id, req).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<Void>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<Void>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(getContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    String msg = "Đổi mật khẩu thất bại";
                    if (response.body() != null && response.body().message != null) msg = response.body().message;
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        UserDto user = sessionManager.getUser();
        if (user != null) {
            tvProfileFullName.setText(user.fullName != null ? user.fullName : "N/A");
            tvProfileUsername.setText(user.username != null ? user.username : "N/A");
            tvProfilePhone.setText(user.phone != null ? user.phone : "N/A");
            tvProfileEmail.setText(user.email != null ? user.email : "N/A");
            tvProfileRole.setText(user.role != null ? user.role : (user.position != null ? user.position : "Nhân viên"));
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
        }
    }
}
