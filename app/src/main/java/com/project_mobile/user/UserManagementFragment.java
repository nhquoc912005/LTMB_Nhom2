package com.project_mobile.user;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.project_mobile.R;
import com.project_mobile.common.AppDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserManagementFragment extends Fragment implements UserAdapter.UserActionListener {

    private final List<UserModel> allUsers = new ArrayList<>();
    private final List<UserModel> filteredUsers = new ArrayList<>();
    private UserAdapter adapter;
    private EditText edtSearch;
    private TextView tvTotalUsers;
    private TextView tvActiveUsers;
    private TextView tvLockedUsers;

    private static final String[] ROLES = { "Quản trị viên", "Quản lý", "Lễ tân" };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvActiveUsers = view.findViewById(R.id.tvActiveUsers);
        tvLockedUsers = view.findViewById(R.id.tvLockedUsers);
        edtSearch = view.findViewById(R.id.edtUserSearch);

        RecyclerView recyclerView = view.findViewById(R.id.rvUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(filteredUsers, this);
        recyclerView.setAdapter(adapter);

        fetchUsers();

        view.findViewById(R.id.btnAddUser).setOnClickListener(v -> showUserForm(null));
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        updateStats();
        return view;
    }

    @Override
    public void onToggleLock(UserModel user) {
        String action = user.isLocked() ? "mở khóa" : "khóa";
        AppDialog.showConfirm(
                requireContext(),
                user.isLocked() ? "Mở khóa tài khoản" : "Khóa tài khoản",
                "Bạn có chắc chắn muốn " + action + " tài khoản " + user.getFullName() + "?",
                user.isLocked() ? "Mở khóa" : "Khóa",
                true,
                () -> {
                    user.setLocked(!user.isLocked());
                    reloadCurrentList();
                    AppDialog.showSuccess(requireContext(),
                            user.isLocked() ? "Khóa tài khoản thành công" : "Mở khóa thành công");
                });
    }

    @Override
    public void onEdit(UserModel user) {
        showUserForm(user);
    }

    @Override
    public void onDelete(UserModel user) {
        AppDialog.showConfirm(
                requireContext(),
                "Xóa người dùng",
                "Bạn có chắc chắn muốn xóa " + user.getFullName() + "?",
                "Xóa",
                true,
                () -> {
                    com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
                    api.deleteUser(user.getUserCode()).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                fetchUsers();
                                AppDialog.showSuccess(requireContext(), "Xóa người dùng thành công");
                            } else {
                                AppDialog.showError(requireContext(), "Lỗi khi xóa người dùng");
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, Throwable t) {
                            AppDialog.showError(requireContext(), "Lỗi kết nối");
                        }
                    });
                });
    }

    private void fetchUsers() {
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getUsers().enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.UserDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.UserDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.UserDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    allUsers.clear();
                    for (com.project_mobile.network.ApiModels.UserDto dto : response.body().data) {
                        allUsers.add(new UserModel(
                            String.valueOf(dto.id),
                            dto.fullName,
                            dto.email,
                            dto.phone,
                            dto.role,
                            false // Add lock status to DTO if needed
                        ));
                    }
                    reloadCurrentList();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.UserDto>>> call, Throwable t) {
                if (isAdded()) AppDialog.showError(requireContext(), "Lỗi tải dữ liệu nhân viên");
            }
        });
    }

    private void seedUsers() {
        // Obsolete
    }

    private void applySearch(String rawQuery) {
        String query = normalize(rawQuery);
        filteredUsers.clear();
        for (UserModel user : allUsers) {
            if (query.isEmpty()
                    || normalize(user.getFullName()).contains(query)
                    || normalize(user.getEmail()).contains(query)
                    || normalize(user.getPhone()).contains(query)) {
                filteredUsers.add(user);
            }
        }
        adapter.submitList(filteredUsers);
        updateStats();
    }

    private void reloadCurrentList() {
        applySearch(edtSearch == null ? "" : edtSearch.getText().toString());
    }

    private void updateStats() {
        int locked = 0;
        for (UserModel user : allUsers) {
            if (user.isLocked()) {
                locked++;
            }
        }
        tvTotalUsers.setText(String.valueOf(allUsers.size()));
        tvActiveUsers.setText(String.valueOf(allUsers.size() - locked));
        tvLockedUsers.setText(String.valueOf(locked));
    }

    private void showUserForm(@Nullable UserModel editingUser) {
        boolean isEdit = editingUser != null;
        View formView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog_user_form, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(formView).create();

        TextView title = formView.findViewById(R.id.tvUserFormTitle);
        EditText edtFullName = formView.findViewById(R.id.edtFullName);
        EditText edtEmail = formView.findViewById(R.id.edtEmail);
        EditText edtPhone = formView.findViewById(R.id.edtPhone);
        EditText edtPassword = formView.findViewById(R.id.edtPassword);
        Spinner spRole = formView.findViewById(R.id.spRole);
        LinearLayout passwordGroup = formView.findViewById(R.id.llPasswordGroup);
        MaterialButton btnSubmit = formView.findViewById(R.id.btnSubmitUserForm);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                ROLES);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(roleAdapter);

        title.setText(isEdit ? "Cập nhật người dùng" : "Thêm người dùng mới");
        btnSubmit.setText(isEdit ? "Cập nhật" : "Tạo tài khoản");
        passwordGroup.setVisibility(isEdit ? View.GONE : View.VISIBLE);

        if (isEdit) {
            edtFullName.setText(editingUser.getFullName());
            edtEmail.setText(editingUser.getEmail());
            edtPhone.setText(editingUser.getPhone());
            spRole.setSelection(Math.max(0, roleIndexOf(editingUser.getRole())));
        }

        formView.findViewById(R.id.btnCloseUserForm).setOnClickListener(v -> dialog.dismiss());
        formView.findViewById(R.id.btnCancelUserForm).setOnClickListener(v -> dialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String role = spRole.getSelectedItem() == null ? "" : spRole.getSelectedItem().toString();
            String password = edtPassword.getText().toString();

            String validationError = validateUserForm(fullName, email, phone, role, password, isEdit);
            if (validationError != null) {
                AppDialog.showError(requireContext(), validationError);
                return;
            }

            com.project_mobile.network.ApiModels.UserDto dto = new com.project_mobile.network.ApiModels.UserDto();
            dto.fullName = fullName;
            dto.email = email;
            dto.phone = phone;
            dto.role = role;
            dto.username = fullName.toLowerCase().replaceAll("\\s+", "_"); 
            dto.password = password; 

            com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
            if (isEdit) {
                api.updateUser(editingUser.getUserCode(), dto).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            fetchUsers();
                            AppDialog.showSuccess(requireContext(), "Cập nhật thành công");
                            dialog.dismiss();
                        } else {
                            AppDialog.showError(requireContext(), "Lỗi cập nhật");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, Throwable t) {
                        AppDialog.showError(requireContext(), "Lỗi kết nối");
                    }
                });
            } else {
                api.createUser(dto).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            fetchUsers();
                            AppDialog.showSuccess(requireContext(), "Tạo tài khoản thành công");
                            dialog.dismiss();
                        } else {
                            AppDialog.showError(requireContext(), "Lỗi tạo tài khoản");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, Throwable t) {
                        AppDialog.showError(requireContext(), "Lỗi kết nối");
                    }
                });
            }
        });

        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.94f);
                window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
    }

    @Nullable
    private String validateUserForm(String fullName, String email, String phone, String role, String password,
            boolean isEdit) {
        if (fullName.isEmpty()) {
            return "Không được để trống họ tên.";
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Email không đúng định dạng.";
        }
        if (phone.isEmpty() || !phone.matches("^[0-9]{9,11}$")) {
            return "Số điện thoại không đúng định dạng.";
        }
        if (role.isEmpty()) {
            return "Vui lòng chọn vai trò.";
        }
        if (!isEdit && password.trim().isEmpty()) {
            return "Mật khẩu không được để trống.";
        }
        return null;
    }

    private int roleIndexOf(String role) {
        for (int i = 0; i < ROLES.length; i++) {
            if (ROLES[i].equals(role)) {
                return i;
            }
        }
        return 0;
    }

    private String nextUserCode() {
        int max = 0;
        for (UserModel user : allUsers) {
            String code = user.getUserCode();
            if (code != null && code.matches("U\\d{3}")) {
                max = Math.max(max, Integer.parseInt(code.substring(1)));
            }
        }
        return String.format(Locale.US, "U%03d", max + 1);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
