package com.project_mobile.service;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.List;

public class ServiceFragment extends Fragment {

    private RecyclerView rcvServices;
    private ServiceAdapter adapter;
    private List<ServiceModel> currentList;
    private List<ServiceModel> listServices;
    private List<ServiceModel> listAssets;

    private TextView tvTabService, tvTabAsset;
    private boolean isServiceTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);

        rcvServices = view.findViewById(R.id.rcvServices);
        tvTabService = view.findViewById(R.id.tvTabService);
        tvTabAsset = view.findViewById(R.id.tvTabAsset);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddService);

        initData();

        currentList = new ArrayList<>(listServices);
        adapter = new ServiceAdapter(currentList, new ServiceAdapter.OnServiceClickListener() {
            @Override
            public void onEditClick(ServiceModel item) {
                showServiceDialog(2, item); // MODE_EDIT
            }

            @Override
            public void onDeleteClick(ServiceModel item) {
                showServiceDialog(3, item); // MODE_DELETE
            }
        });

        rcvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvServices.setAdapter(adapter);

        tvTabService.setOnClickListener(v -> switchTab(true));
        tvTabAsset.setOnClickListener(v -> switchTab(false));
        fabAdd.setOnClickListener(v -> showServiceDialog(1, null)); // MODE_ADD

        return view;
    }

    private void initData() {
        listServices = new ArrayList<>();
        listServices.add(new ServiceModel("1", "Buffet sáng", "150.000đ/suất"));
        listServices.add(new ServiceModel("2", "Đưa đón sân bay", "500.000đ"));

        listAssets = new ArrayList<>();
        listAssets.add(new ServiceModel("3", "Bàn ghế hỏng", "400.000đ"));
        listAssets.add(new ServiceModel("4", "Mất chìa khóa", "100.000đ"));
    }

    private void switchTab(boolean toServiceTab) {
        isServiceTab = toServiceTab;
        if (isServiceTab) {
            tvTabService.setBackgroundResource(R.drawable.bg_tab_left_active);
            tvTabService.setTextColor(Color.parseColor("#C58959"));
            tvTabAsset.setBackgroundResource(0);
            tvTabAsset.setTextColor(Color.BLACK);
            currentList.clear();
            currentList.addAll(listServices);
        } else {
            tvTabAsset.setBackgroundResource(R.drawable.bg_tab_right_active);
            tvTabAsset.setTextColor(Color.parseColor("#C58959"));
            tvTabService.setBackgroundResource(0);
            tvTabService.setTextColor(Color.BLACK);
            currentList.clear();
            currentList.addAll(listAssets);
        }
        adapter.setServiceType(isServiceTab);
        adapter.notifyDataSetChanged();
    }

    private void showServiceDialog(int mode, ServiceModel item) {
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
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        btnCancel.setStateListAnimator(null);
        btnConfirm.setStateListAnimator(null);

        if (mode == 1) { // ADD
            tvTitle.setText("Thêm " + (isServiceTab ? "dịch vụ" : "bồi thường") + " mới");
            btnConfirm.setText("Thêm");
        } else if (mode == 2) { // EDIT
            tvTitle.setText("Chỉnh sửa thông tin");
            btnConfirm.setText("Cập nhật");
            edtName.setText(item.getName());
            edtPrice.setText(item.getPrice());
        } else if (mode == 3) { // DELETE
            tvTitle.setText("Xóa " + (isServiceTab ? "dịch vụ" : "bồi thường") + " này?");
            btnConfirm.setText("Xác nhận");
            edtName.setText(item.getName());
            edtPrice.setText(item.getPrice());
            edtName.setEnabled(false);
            edtPrice.setEnabled(false);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}