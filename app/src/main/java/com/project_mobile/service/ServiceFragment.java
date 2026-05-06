// Module dịch vụ/tài sản Android.
// File này quản lý danh mục dịch vụ và danh mục tài sản/bồi thường.
// Dữ liệu chính lấy từ /api/services và /api/assets, có thêm/sửa/xóa danh mục.
/*
 * File: ServiceFragment.java
 * Module: Quản lý danh mục dịch vụ và tài sản.
 *
 * Luồng chính:
 * - Tab Dịch vụ gọi các endpoint /api/services.
 * - Tab Tài sản gọi các endpoint /api/assets.
 * - loadCatalog tải danh mục hiện tại.
 * - showCatalogDialog tạo hoặc cập nhật một item.
 * - confirmDelete xóa item khỏi danh mục.
 *
 * File này quản lý danh mục chung, khác với RoomMapFragment là nơi gắn dịch vụ/tài sản vào từng phòng.
 */
package com.project_mobile.service;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project_mobile.R;
import com.project_mobile.network.ApiModels.CatalogItemDto;

import java.util.ArrayList;
import java.util.List;

/**
 * ServiceFragment là màn CRUD danh mục dịch vụ/tài sản.
 * Class này chuyển tab giữa service/asset, gọi ServiceRepository và mở dialog nhập thông tin danh mục.
 */
public class ServiceFragment extends Fragment {
    private RecyclerView rcvServices;
    private ServiceAdapter adapter;
    private ServiceRepository repository;
    private EditText edtSearch;
    private TextView tvTabService;
    private TextView tvTabAsset;
    private boolean isServiceTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);

        repository = new ServiceRepository();
        edtSearch = view.findViewById(R.id.edtSearch);
        Button btnSearch = view.findViewById(R.id.btnSearch);
        rcvServices = view.findViewById(R.id.rcvServices);
        tvTabService = view.findViewById(R.id.tvTabService);
        tvTabAsset = view.findViewById(R.id.tvTabAsset);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddService);

        adapter = new ServiceAdapter(new ArrayList<>(), new ServiceAdapter.OnServiceClickListener() {
            @Override
            public void onEditClick(ServiceModel item) {
                showCatalogDialog(item);
            }

            @Override
            public void onDeleteClick(ServiceModel item) {
                confirmDelete(item);
            }
        });
        adapter.setServiceType(isServiceTab);

        rcvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvServices.setAdapter(adapter);

        tvTabService.setOnClickListener(v -> switchTab(true));
        tvTabAsset.setOnClickListener(v -> switchTab(false));
        btnSearch.setOnClickListener(v -> loadCatalog());
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadCatalog();
                return true;
            }
            return false;
        });
        fabAdd.setOnClickListener(v -> showCatalogDialog(null));

        updateTabUI();
        loadCatalog();
        return view;
    }

    /** Chuyển tab danh mục và reload dữ liệu tương ứng. */
    /** Chuyển giữa danh mục dịch vụ và danh mục tài sản, sau đó tải lại dữ liệu tương ứng. */
    private void switchTab(boolean toServiceTab) {
        if (isServiceTab == toServiceTab) return;
        isServiceTab = toServiceTab;
        adapter.setServiceType(isServiceTab);
        updateTabUI();
        loadCatalog();
    }

    /** Cập nhật màu tab service/asset đang chọn. */
    private void updateTabUI() {
        if (isServiceTab) {
            tvTabService.setBackgroundResource(R.drawable.bg_tab_left_active);
            tvTabService.setTextColor(Color.parseColor("#C0410D"));
            tvTabAsset.setBackgroundResource(0);
            tvTabAsset.setTextColor(Color.BLACK);
        } else {
            tvTabAsset.setBackgroundResource(R.drawable.bg_tab_right_active);
            tvTabAsset.setTextColor(Color.parseColor("#C0410D"));
            tvTabService.setBackgroundResource(0);
            tvTabService.setTextColor(Color.BLACK);
        }
    }

    /** Lấy danh mục từ backend theo tab hiện tại và từ khóa tìm kiếm. */
    /*
     * Tải danh mục hiện tại từ API.
     * isServiceTab=true gọi /api/services, ngược lại gọi /api/assets.
     */
    private void loadCatalog() {
        String query = edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim();
        repository.fetchCatalog(isServiceTab, query, new ServiceRepository.DataCallback<List<CatalogItemDto>>() {
            @Override
            public void onSuccess(List<CatalogItemDto> data) {
                if (!isAdded()) return;
                List<ServiceModel> mapped = new ArrayList<>();
                if (data != null) {
                    for (CatalogItemDto item : data) {
                        mapped.add(ServiceModel.fromDto(item));
                    }
                }
                adapter.submitList(mapped);
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Mở dialog thêm/sửa danh mục; item null là thêm mới, khác null là chỉnh sửa. */
    /*
     * Hiển thị form thêm/sửa dịch vụ hoặc tài sản.
     * Input item=null nghĩa là tạo mới; item khác null nghĩa là cập nhật item hiện có.
     */
    private void showCatalogDialog(@Nullable ServiceModel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.popup_service, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        EditText edtName = view.findViewById(R.id.edtServiceName);
        EditText edtPrice = view.findViewById(R.id.edtServicePrice);
        EditText edtUnit = view.findViewById(R.id.edtServiceUnit);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        btnCancel.setStateListAnimator(null);
        btnConfirm.setStateListAnimator(null);

        boolean editing = item != null;
        String typeName = isServiceTab ? "dịch vụ" : "bồi thường";
        tvTitle.setText(editing ? "Sửa " + typeName : "Thêm " + typeName + " mới");
        btnConfirm.setText("Xác nhận");

        if (editing) {
            edtName.setText(item.getName());
            edtPrice.setText(String.valueOf(Math.round(item.getPrice())));
            edtUnit.setText(item.getUnit() == null ? "" : item.getUnit());
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            // Validate tên và giá tiền trước khi gửi request lưu danh mục.
            String name = edtName.getText() == null ? "" : edtName.getText().toString().trim();
            String unit = edtUnit.getText() == null ? "" : edtUnit.getText().toString().trim();
            if (name.isEmpty()) {
                edtName.setError("Không được bỏ trống tên");
                return;
            }

            Double price = parseMoneyInput(edtPrice.getText() == null ? "" : edtPrice.getText().toString());
            if (price == null || price < 0) {
                edtPrice.setError("Giá tiền phải là số lớn hơn hoặc bằng 0");
                return;
            }

            repository.saveCatalog(isServiceTab, editing ? item.getId() : null, name, price, unit, new ServiceRepository.DataCallback<CatalogItemDto>() {
                @Override
                public void onSuccess(CatalogItemDto data) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Đã lưu thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadCatalog();
                }

                @Override
                public void onError(String error) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    /** Xác nhận xóa danh mục; backend có thể chặn nếu item đã phát sinh sử dụng. */
    /*
     * Xác nhận xóa danh mục.
     * Việc xóa chỉ gửi lên backend sau khi người dùng xác nhận trong dialog.
     */
    private void confirmDelete(ServiceModel item) {
        String typeName = isServiceTab ? "dịch vụ" : "bồi thường";
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa " + typeName)
                .setMessage("Bạn có chắc muốn xoá \"" + item.getName() + "\"?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (dialog, which) -> repository.deleteCatalog(isServiceTab, item.getId(), new ServiceRepository.DataCallback<CatalogItemDto>() {
                    @Override
                    public void onSuccess(CatalogItemDto data) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Đã xoá thành công", Toast.LENGTH_SHORT).show();
                        loadCatalog();
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
                    }
                }))
                .show();
    }

    @Nullable
    /** Chuẩn hóa tiền nhập từ UI, bỏ ký hiệu đ/dấu phân cách trước khi parse số. */
    private Double parseMoneyInput(String value) {
        String normalized = value.replace("đ", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", "")
                .trim();
        if (normalized.isEmpty()) return null;
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String readableError(String error) {
        return error == null || error.trim().isEmpty() ? "Có lỗi xảy ra" : error;
    }
}
