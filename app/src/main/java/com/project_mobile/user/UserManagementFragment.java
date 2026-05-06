// Module quản lý tài khoản Android.
// File này hiển thị danh sách nhân viên, tìm kiếm, thêm/sửa/xóa và khóa/mở khóa tài khoản.
// Dữ liệu chính lấy từ /api/users và /api/users/roles.
/*
 * File: UserManagementFragment.java
 * Module: Quản lý tài khoản.
 *
 * Luồng chính:
 * 1. fetchRoles gọi GET /api/users/roles để lấy vai trò từ bảng vai_tro.
 * 2. fetchUsers gọi GET /api/users để lấy danh sách tài khoản.
 * 3. UserDto được map sang UserModel để hiển thị RecyclerView.
 * 4. showUserForm tạo/sửa tài khoản bằng POST/PUT /api/users.
 * 5. updateUserLockOnServer khóa/mở khóa tài khoản qua PUT /api/users/{id}/lock.
 *
 * Dữ liệu quan trọng:
 * - roleOptions luôn lấy từ backend, không hard-code danh sách vai trò trong Android.
 * - locked/active quyết định tài khoản có được sử dụng hay không.
 */
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

/**
 * UserManagementFragment phụ trách màn Quản lý tài khoản.
 * Class này tải người dùng/vai trò, map UserDto sang UserModel và gọi API CRUD tài khoản.
 */
public class UserManagementFragment extends Fragment implements UserAdapter.UserActionListener {

    private final List<UserModel> allUsers = new ArrayList<>();
    private final List<UserModel> filteredUsers = new ArrayList<>();
    private UserAdapter adapter;
    private EditText edtSearch;
    private TextView tvTotalUsers;
    private TextView tvActiveUsers;
    private TextView tvLockedUsers;
    private final List<RoleOption> roleOptions = new ArrayList<>();

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

        fetchRoles(null);
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
    /** Mở xác nhận khóa/mở khóa và gọi backend cập nhật trạng thái tài khoản. */
    public void onToggleLock(UserModel user) {
        String action = user.isLocked() ? "mở khóa" : "khóa";
        AppDialog.showConfirm(
                requireContext(),
                user.isLocked() ? "Mở khóa tài khoản" : "Khóa tài khoản",
                "Bạn có chắc chắn muốn " + action + " tài khoản " + user.getFullName() + "?",
                user.isLocked() ? "Mở khóa" : "Khóa",
                true,
                () -> {
                    if (user != null) {
                        updateUserLockOnServer(user);
                        return;
                    }
                    user.setLocked(!user.isLocked());
                    reloadCurrentList();
                    AppDialog.showSuccess(requireContext(),
                            user.isLocked() ? "Khóa tài khoản thành công" : "Mở khóa thành công");
                });
    }

    /** Gửi trạng thái locked/active mới lên API /api/users/{id}/lock. */
    private void updateUserLockOnServer(UserModel user) {
        com.project_mobile.network.ApiModels.UserLockRequest req = new com.project_mobile.network.ApiModels.UserLockRequest();
        req.locked = !user.isLocked();
        req.active = !req.locked;

        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.updateUserLock(user.getUserCode(), req).enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    fetchUsers();
                    AppDialog.showSuccess(requireContext(), req.locked ? "Khóa tài khoản thành công" : "Mở khóa tài khoản thành công");
                } else {
                    AppDialog.showError(requireContext(), "Lỗi cập nhật trạng thái tài khoản");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<com.project_mobile.network.ApiModels.UserDto>> call, Throwable t) {
                AppDialog.showError(requireContext(), "Lỗi kết nối, vui lòng thử lại");
            }
        });
    }

    @Override
    public void onEdit(UserModel user) {
        showUserForm(user);
    }

    @Override
    /** Xác nhận xóa nhân viên rồi gọi API DELETE nếu người dùng đồng ý. */
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

    /** Tải danh sách tài khoản, map DTO sang UserModel và cập nhật thống kê. */
    /*
     * Tải danh sách tài khoản từ backend.
     *
     * Input: không có tham số.
     * Output: allUsers được làm mới bằng UserModel.
     *
     * Map dữ liệu:
     * - dto.id -> userCode.
     * - dto.role hoặc dto.position -> tên vai trò hiển thị.
     * - dto.locked ưu tiên dùng trực tiếp; nếu thiếu thì suy ra từ active=false.
     */
    private void fetchUsers() {
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getUsers().enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.UserDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.UserDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.UserDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    allUsers.clear();
                    for (com.project_mobile.network.ApiModels.UserDto dto : response.body().data) {
                        // locked ưu tiên field backend, nếu thiếu thì suy ra từ active=false.
                        allUsers.add(new UserModel(
                            String.valueOf(dto.id),
                            dto.fullName,
                            dto.email,
                            dto.phone,
                            dto.role != null ? dto.role : dto.position,
                            dto.gender,
                            dto.idVaitro,
                            dto.locked != null ? dto.locked : Boolean.FALSE.equals(dto.active)
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

    /** Lọc người dùng theo họ tên, email hoặc số điện thoại. */
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

    /** Đếm tổng, đang hoạt động và tạm khóa để hiển thị trên dashboard của màn tài khoản. */
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

    /** Mở form thêm/sửa tài khoản; khi sửa thì ẩn nhóm nhập mật khẩu. */
    /*
     * Hiển thị form thêm/sửa tài khoản.
     *
     * Khi thêm mới:
     * - Hiện ô mật khẩu.
     * - Gọi POST /api/users.
     *
     * Khi sửa:
     * - Ẩn ô mật khẩu để không đổi mật khẩu ngoài ý muốn.
     * - Gọi PUT /api/users/{id}.
     */
    private void showUserForm(@Nullable UserModel editingUser) {
        boolean isEdit = editingUser != null;
        View formView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog_user_form, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(formView).create();

        TextView title = formView.findViewById(R.id.tvUserFormTitle);
        EditText edtFullName = formView.findViewById(R.id.edtFullName);
        EditText edtEmail = formView.findViewById(R.id.edtEmail);
        EditText edtPhone = formView.findViewById(R.id.edtPhone);
        EditText edtPassword = formView.findViewById(R.id.edtPassword);
        Spinner spGender = formView.findViewById(R.id.spGender);
        Spinner spRole = formView.findViewById(R.id.spRole);
        LinearLayout passwordGroup = formView.findViewById(R.id.llPasswordGroup);
        MaterialButton btnSubmit = formView.findViewById(R.id.btnSubmitUserForm);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"Nam", "Nữ"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        configureRoleSpinner(spRole, editingUser);
        btnSubmit.setEnabled(!roleOptions.isEmpty());
        if (roleOptions.isEmpty()) {
            fetchRoles(() -> {
                if (!isAdded()) return;
                configureRoleSpinner(spRole, editingUser);
                btnSubmit.setEnabled(!roleOptions.isEmpty());
            });
        }

        title.setText(isEdit ? "Cập nhật người dùng" : "Thêm người dùng mới");
        btnSubmit.setText(isEdit ? "Cập nhật" : "Tạo tài khoản");
        passwordGroup.setVisibility(isEdit ? View.GONE : View.VISIBLE);

        if (isEdit) {
            edtFullName.setText(editingUser.getFullName());
            edtEmail.setText(editingUser.getEmail());
            edtPhone.setText(editingUser.getPhone());
            spGender.setSelection("Nữ".equals(editingUser.getGender()) ? 1 : 0);
        }

        formView.findViewById(R.id.btnCloseUserForm).setOnClickListener(v -> dialog.dismiss());
        formView.findViewById(R.id.btnCancelUserForm).setOnClickListener(v -> dialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String gender = spGender.getSelectedItem() == null ? "Nam" : spGender.getSelectedItem().toString();
            // Lấy vai trò từ spinner đã load từ bảng vai_tro để gửi cả tên và id_vaitro.
            // Danh sách vai trò được lấy trực tiếp từ bảng vai_tro.
            // Không hard-code trong Android để khi database thêm vai trò mới,
            // ứng dụng vẫn tự động hiển thị đầy đủ.
            RoleOption selectedRole = spRole.getSelectedItem() instanceof RoleOption
                    ? (RoleOption) spRole.getSelectedItem()
                    : null;
            String role = selectedRole == null || selectedRole.id == null ? "" : selectedRole.name;
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
            dto.position = role;
            dto.idVaitro = selectedRole == null ? null : selectedRole.id;
            dto.gender = gender;
            dto.username = fullName.toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
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
    /** Kiểm tra input form tài khoản trước khi gọi API tạo/cập nhật. */
    /*
     * Validate form trước khi gửi API.
     * Hàm trả về null nếu dữ liệu hợp lệ, hoặc trả về message lỗi để hiển thị.
     */
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

    /** Lấy danh sách vai trò từ backend để nạp Spinner chọn quyền. */
    /*
     * Lấy danh sách vai trò từ backend.
     *
     * Endpoint: GET /api/users/roles.
     * Mục đích: nạp Spinner chọn quyền, tránh hard-code role trong app.
     * afterLoaded được dùng để cấu hình lại Spinner sau khi API trả dữ liệu.
     */
    private void fetchRoles(@Nullable Runnable afterLoaded) {
        com.project_mobile.network.ApiService api = com.project_mobile.network.ApiClient.getClient().create(com.project_mobile.network.ApiService.class);
        api.getUserRoles().enqueue(new retrofit2.Callback<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoleDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoleDto>>> call, retrofit2.Response<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoleDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                    roleOptions.clear();
                    for (com.project_mobile.network.ApiModels.RoleDto dto : response.body().data) {
                        if (dto != null && dto.idVaitro != null && dto.name != null && !dto.name.trim().isEmpty()) {
                            roleOptions.add(new RoleOption(dto.idVaitro, dto.name));
                        }
                    }
                    if (afterLoaded != null) afterLoaded.run();
                } else if (afterLoaded != null) {
                    afterLoaded.run();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.project_mobile.network.ApiModels.ApiResponse<List<com.project_mobile.network.ApiModels.RoleDto>>> call, Throwable t) {
                if (afterLoaded != null) afterLoaded.run();
            }
        });
    }

    /** Cấu hình Spinner vai trò, chọn đúng role hiện tại khi đang sửa tài khoản. */
    private void configureRoleSpinner(Spinner spRole, @Nullable UserModel editingUser) {
        List<RoleOption> options = roleOptions.isEmpty() ? new ArrayList<>() : new ArrayList<>(roleOptions);
        if (options.isEmpty()) {
            options.add(new RoleOption(null, "Đang tải vai trò..."));
        }
        ArrayAdapter<RoleOption> roleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(roleAdapter);
        if (editingUser != null && !roleOptions.isEmpty()) {
            spRole.setSelection(Math.max(0, roleIndexOf(editingUser)));
        }
    }

    /** Tìm vị trí role của user trong danh sách roleOptions theo id hoặc tên vai trò. */
    private int roleIndexOf(UserModel user) {
        for (int i = 0; i < roleOptions.size(); i++) {
            RoleOption option = roleOptions.get(i);
            if ((user.getRoleId() != null && user.getRoleId().equals(option.id))
                    || (user.getRole() != null && user.getRole().equals(option.name))) {
                return i;
            }
        }
        return 0;
    }

    private static class RoleOption {
        final Integer id;
        final String name;

        RoleOption(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
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
