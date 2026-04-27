package com.project_mobile.service;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project_mobile.R;
import com.project_mobile.network.ApiModels.ActiveRoomDto;
import com.project_mobile.network.ApiModels.CatalogItemDto;
import com.project_mobile.network.ApiModels.RoomLineDto;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class RoomMapFragment extends Fragment {
    private static final String TAG = "RoomMapFragment";

    private ServiceRepository repository;
    private EditText edtSearch;
    private LinearLayout layoutRoomMap;
    private RecyclerView rcvRoomMap;
    private RelativeLayout layoutRoomDetail;
    private View fabAddRoomLine;

    private TextView tvTabService;
    private TextView tvTabAsset;
    private TextView tvDetailTabService;
    private TextView tvDetailTabAsset;
    private TextView tvDetailRoomTitle;
    private TextView tvDetailStatus;
    private TextView tvDetailCheckIn;
    private TextView tvDetailCheckOut;
    private TextView tvDetailRoomFee;

    private LinearLayout layoutAddedServices;
    private LinearLayout layoutAddedAssets;
    private LinearLayout sectionServices;
    private LinearLayout sectionAssets;
    private LinearLayout layoutActionButtons;

    private boolean isServiceTab = true;
    private StayRoomModel selectedRoom;
    private final List<RoomLineDto> currentServiceLines = new ArrayList<>();
    private final List<RoomLineDto> currentAssetLines = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_map, container, false);
        repository = new ServiceRepository();

        edtSearch = view.findViewById(R.id.edtSearch);
        Button btnSearch = view.findViewById(R.id.btnSearch);
        tvTabService = view.findViewById(R.id.tvTabService);
        tvTabAsset = view.findViewById(R.id.tvTabAsset);
        tvDetailTabService = view.findViewById(R.id.tvDetailTabService);
        tvDetailTabAsset = view.findViewById(R.id.tvDetailTabAsset);
        layoutRoomMap = view.findViewById(R.id.layoutRoomMap);
        rcvRoomMap = view.findViewById(R.id.rcvRoomMap);
        layoutRoomDetail = view.findViewById(R.id.layoutRoomDetail);
        fabAddRoomLine = view.findViewById(R.id.fabAddRoomLine);
        tvDetailRoomTitle = view.findViewById(R.id.tvDetailRoomTitle);
        tvDetailStatus = view.findViewById(R.id.tvDetailStatus);
        tvDetailCheckIn = view.findViewById(R.id.tvDetailCheckIn);
        tvDetailCheckOut = view.findViewById(R.id.tvDetailCheckOut);
        tvDetailRoomFee = view.findViewById(R.id.tvDetailRoomFee);
        sectionServices = view.findViewById(R.id.sectionServices);
        sectionAssets = view.findViewById(R.id.sectionAssets);
        layoutAddedServices = view.findViewById(R.id.layoutAddedServices);
        layoutAddedAssets = view.findViewById(R.id.layoutAddedAssets);
        layoutActionButtons = view.findViewById(R.id.layoutActionButtons);

        rcvRoomMap.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutActionButtons.setVisibility(View.GONE);
        fabAddRoomLine.setVisibility(View.GONE);

        setupTabs();
        setupActions(view);
        btnSearch.setOnClickListener(v -> loadActiveRooms());
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadActiveRooms();
                return true;
            }
            return false;
        });

        loadActiveRooms();
        return view;
    }

    private void setupTabs() {
        View.OnClickListener serviceClick = v -> setCurrentTab(true);
        View.OnClickListener assetClick = v -> setCurrentTab(false);
        tvTabService.setOnClickListener(serviceClick);
        tvDetailTabService.setOnClickListener(serviceClick);
        tvTabAsset.setOnClickListener(assetClick);
        tvDetailTabAsset.setOnClickListener(assetClick);
        setCurrentTab(true);
    }

    private void setupActions(View view) {
        view.findViewById(R.id.btnDetailClose).setOnClickListener(v -> closeDetail());
        fabAddRoomLine.setOnClickListener(v -> handleAddRoomLineClick());

        View btnAddServiceInline = view.findViewById(R.id.btnAddServiceInline);
        View btnAddAssetInline = view.findViewById(R.id.btnAddAssetInline);
        btnAddServiceInline.setOnClickListener(v -> {
            setCurrentTab(true);
            showServicePickerBottomSheet();
        });
        btnAddAssetInline.setOnClickListener(v -> {
            setCurrentTab(false);
            showAssetPickerBottomSheet();
        });
    }

    private void setCurrentTab(boolean serviceTab) {
        isServiceTab = serviceTab;
        updateTabUi(tvTabService, tvTabAsset);
        updateTabUi(tvDetailTabService, tvDetailTabAsset);
        updateDetailSectionVisibility();
    }

    private void updateTabUi(TextView serviceTab, TextView assetTab) {
        if (serviceTab == null || assetTab == null)
            return;
        if (isServiceTab) {
            serviceTab.setBackgroundResource(R.drawable.bg_tab_left_active);
            serviceTab.setTextColor(Color.parseColor("#C0410D"));
            assetTab.setBackgroundResource(0);
            assetTab.setTextColor(Color.BLACK);
        } else {
            assetTab.setBackgroundResource(R.drawable.bg_tab_right_active);
            assetTab.setTextColor(Color.parseColor("#C0410D"));
            serviceTab.setBackgroundResource(0);
            serviceTab.setTextColor(Color.BLACK);
        }
    }

    private void updateDetailSectionVisibility() {
        if (layoutRoomDetail == null || layoutRoomDetail.getVisibility() != View.VISIBLE)
            return;
        sectionServices.setVisibility(isServiceTab ? View.VISIBLE : View.GONE);
        sectionAssets.setVisibility(isServiceTab ? View.GONE : View.VISIBLE);
        fabAddRoomLine.setVisibility(View.VISIBLE);
    }

    private void handleAddRoomLineClick() {
        if (selectedRoom == null) {
            Toast.makeText(getContext(), "Vui lòng chọn phòng trước", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isServiceTab) {
            showServicePickerBottomSheet();
        } else {
            showAssetPickerBottomSheet();
        }
    }

    private void loadActiveRooms() {
        String query = edtSearch.getText() == null ? "" : edtSearch.getText().toString().trim();
        repository.fetchActiveRooms(query, new ServiceRepository.DataCallback<List<ActiveRoomDto>>() {
            @Override
            public void onSuccess(List<ActiveRoomDto> data) {
                if (!isAdded())
                    return;
                List<StayRoomModel> rooms = new ArrayList<>();
                if (data != null) {
                    for (ActiveRoomDto dto : data) {
                        // Chỉ hiển thị những phòng đang có khách lưu trú (stayId != null)
                        if (dto.stayId != null) {
                            rooms.add(StayRoomModel.fromDto(dto));
                        }
                    }
                }
                rcvRoomMap.setAdapter(new FloorAdapter(groupByFloor(rooms), room -> showRoomDetail(room)));
            }

            @Override
            public void onError(String error) {
                if (!isAdded())
                    return;
                Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<FloorModel> groupByFloor(List<StayRoomModel> rooms) {
        TreeMap<Integer, List<StayRoomModel>> grouped = new TreeMap<>();
        for (StayRoomModel room : rooms) {
            int floor = floorOf(room.getRoomNumber());
            if (!grouped.containsKey(floor)) {
                grouped.put(floor, new ArrayList<>());
            }
            grouped.get(floor).add(room);
        }

        List<FloorModel> floors = new ArrayList<>();
        for (Integer floor : grouped.keySet()) {
            floors.add(new FloorModel("Tầng " + floor, grouped.get(floor)));
        }
        return floors;
    }

    private int floorOf(String roomNumber) {
        String digits = roomNumber == null ? "" : roomNumber.replaceAll("\\D+", "");
        if (digits.length() >= 3) {
            return Math.max(1, Integer.parseInt(digits.substring(0, digits.length() - 2)));
        }
        if (!digits.isEmpty()) {
            return Math.max(1, Character.getNumericValue(digits.charAt(0)));
        }
        return 1;
    }

    private void showRoomDetail(StayRoomModel room) {
        selectedRoom = room;
        isServiceTab = true;
        tvDetailRoomTitle.setText("Phòng " + room.getRoomNumber());
        tvDetailStatus.setText(readableStatus(room.getStatus()));
        tvDetailCheckIn.setText(formatDate(room.getExpectedCheckIn()));
        tvDetailCheckOut.setText(formatDate(room.getExpectedCheckOut()));
        tvDetailRoomFee.setText(formatMoney(room.getRoomFee()));

        currentServiceLines.clear();
        currentAssetLines.clear();
        layoutAddedServices.removeAllViews();
        layoutAddedAssets.removeAllViews();
        layoutRoomMap.setVisibility(View.GONE);
        layoutRoomDetail.setVisibility(View.VISIBLE);
        setCurrentTab(true);
        loadRoomLines(true);
        loadRoomLines(false);
    }

    private void closeDetail() {
        selectedRoom = null;
        layoutRoomDetail.setVisibility(View.GONE);
        layoutRoomMap.setVisibility(View.VISIBLE);
        fabAddRoomLine.setVisibility(View.GONE);
    }

    private void loadRoomLines(boolean serviceTab) {
        if (selectedRoom == null)
            return;
        repository.fetchRoomLines(serviceTab, selectedRoom.getRoomId(),
                new ServiceRepository.DataCallback<List<RoomLineDto>>() {
                    @Override
                    public void onSuccess(List<RoomLineDto> data) {
                        if (!isAdded() || selectedRoom == null)
                            return;
                        List<RoomLineDto> safeData = data == null ? new ArrayList<>() : data;
                        cacheRoomLines(serviceTab, safeData);
                        renderLines(serviceTab, safeData);
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded())
                            return;
                        Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderLines(boolean serviceTab, List<RoomLineDto> lines) {
        LinearLayout container = serviceTab ? layoutAddedServices : layoutAddedAssets;
        container.removeAllViews();

        if (lines.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText(serviceTab ? "Chưa có dịch vụ đã chọn" : "Chưa có tài sản đã chọn");
            emptyView.setTextColor(Color.parseColor("#888888"));
            emptyView.setTextSize(15);
            emptyView.setPadding(0, 12, 0, 12);
            container.addView(emptyView);
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            RoomLineDto line = lines.get(i);
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_added_service, container,
                    false);
            TextView tvName = itemView.findViewById(R.id.tvItemName);
            TextView tvPrice = itemView.findViewById(R.id.tvItemPrice);
            EditText edtQty = itemView.findViewById(R.id.edtQty);

            tvName.setText((i + 1) + ". " + safeText(line.name));
            tvPrice.setText(formatMoney(line.total != null ? line.total : 0));
            edtQty.setText(String.valueOf(line.quantity != null ? line.quantity : 1));
            edtQty.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updateLineQuantity(serviceTab, line, edtQty);
                }
            });
            edtQty.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateLineQuantity(serviceTab, line, edtQty);
                    edtQty.clearFocus();
                    return true;
                }
                return false;
            });

            itemView.findViewById(R.id.btnDeleteItem).setOnClickListener(v -> confirmDeleteLine(serviceTab, line));
            container.addView(itemView);
        }
    }

    private void updateLineQuantity(boolean serviceTab, RoomLineDto line, EditText edtQty) {
        Integer quantity = parseQuantity(edtQty.getText() == null ? "" : edtQty.getText().toString());
        if (quantity == null) {
            edtQty.setError("Số lượng phải là số nguyên dương");
            return;
        }
        if (line.quantity != null && line.quantity.equals(quantity))
            return;

        repository.updateRoomLine(serviceTab, line.id, quantity, new ServiceRepository.DataCallback<RoomLineDto>() {
            @Override
            public void onSuccess(RoomLineDto data) {
                if (!isAdded())
                    return;
                loadRoomLines(serviceTab);
            }

            @Override
            public void onError(String error) {
                if (!isAdded())
                    return;
                Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
                loadRoomLines(serviceTab);
            }
        });
    }

    private void confirmDeleteLine(boolean serviceTab, RoomLineDto line) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa khỏi phòng")
                .setMessage("Bạn có chắc muốn xoá \"" + safeText(line.name) + "\"?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (dialog, which) -> repository.deleteRoomLine(serviceTab, line.id,
                        new ServiceRepository.DataCallback<RoomLineDto>() {
                            @Override
                            public void onSuccess(RoomLineDto data) {
                                if (!isAdded())
                                    return;
                                loadRoomLines(serviceTab);
                            }

                            @Override
                            public void onError(String error) {
                                if (!isAdded())
                                    return;
                                Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
                            }
                        }))
                .show();
    }

    private void showServicePickerBottomSheet() {
        showCatalogPickerBottomSheet(true);
    }

    private void showAssetPickerBottomSheet() {
        showCatalogPickerBottomSheet(false);
    }

    private void showCatalogPickerBottomSheet(boolean serviceTab) {
        if (selectedRoom == null) {
            Toast.makeText(getContext(), "Vui lòng chọn phòng trước", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_service_picker, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvPickerTitle);
        EditText edtPickerSearch = view.findViewById(R.id.edtServiceSearch);
        ProgressBar progress = view.findViewById(R.id.progressPicker);
        TextView tvEmpty = view.findViewById(R.id.tvPickerEmpty);
        RecyclerView rvPicker = view.findViewById(R.id.rvServicePicker);

        String itemType = serviceTab ? "dịch vụ" : "tài sản";
        tvTitle.setText(serviceTab ? "Chọn dịch vụ" : "Chọn tài sản");
        edtPickerSearch.setHint(serviceTab ? "Tìm kiếm dịch vụ" : "Tìm kiếm tài sản");
        rvPicker.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<CatalogItemDto> allItems = new ArrayList<>();
        ServicePickerAdapter adapter = new ServicePickerAdapter(item -> addCatalogItemToCurrentRoom(serviceTab, item, dialog));
        rvPicker.setAdapter(adapter);

        view.findViewById(R.id.btnClosePicker).setOnClickListener(v -> dialog.dismiss());
        edtPickerSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCatalogItems(s == null ? "" : s.toString(), allItems, adapter, tvEmpty, rvPicker, serviceTab);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvPicker.setVisibility(View.GONE);
        repository.fetchCatalog(serviceTab, "", new ServiceRepository.DataCallback<List<CatalogItemDto>>() {
            @Override
            public void onSuccess(List<CatalogItemDto> data) {
                if (!isAdded())
                    return;
                progress.setVisibility(View.GONE);
                allItems.clear();
                if (data != null) {
                    allItems.addAll(data);
                }
                filterCatalogItems(
                        edtPickerSearch.getText() == null ? "" : edtPickerSearch.getText().toString(),
                        allItems,
                        adapter,
                        tvEmpty,
                        rvPicker,
                        serviceTab);
            }

            @Override
            public void onError(String error) {
                if (!isAdded())
                    return;
                progress.setVisibility(View.GONE);
                rvPicker.setVisibility(View.GONE);
                tvEmpty.setText(serviceTab ? "Không thể tải danh sách dịch vụ" : "Không thể tải danh sách tài sản");
                tvEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "Cannot load " + itemType + ": " + readableError(error));
            }
        });

        dialog.show();
    }

    private void filterCatalogItems(String keyword, List<CatalogItemDto> allItems, ServicePickerAdapter adapter,
            TextView tvEmpty, RecyclerView rvPicker, boolean serviceTab) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<CatalogItemDto> filtered = new ArrayList<>();
        for (CatalogItemDto item : allItems) {
            String name = item.name == null ? "" : item.name.toLowerCase(Locale.ROOT);
            if (normalized.isEmpty() || name.contains(normalized)) {
                filtered.add(item);
            }
        }

        adapter.submitList(filtered);
        boolean hasItems = !filtered.isEmpty();
        rvPicker.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        tvEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
        if (!hasItems) {
            if (allItems.isEmpty()) {
                tvEmpty.setText(serviceTab ? "Chưa có dịch vụ khả dụng" : "Chưa có tài sản khả dụng");
            } else {
                tvEmpty.setText(serviceTab ? "Không tìm thấy dịch vụ phù hợp" : "Không tìm thấy tài sản phù hợp");
            }
        }
    }

    private void addCatalogItemToCurrentRoom(boolean serviceTab, CatalogItemDto item, BottomSheetDialog dialog) {
        if (selectedRoom == null) {
            Toast.makeText(getContext(), "Không tìm thấy phòng hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedRoom.getRoomId() <= 0) {
            Toast.makeText(getContext(), "Không tìm thấy mã phòng hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        String catalogId = item.id;
        if (catalogId == null || catalogId.trim().isEmpty()) {
            Toast.makeText(getContext(), serviceTab ? "Không tìm thấy mã dịch vụ" : "Không tìm thấy mã tài sản", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRoom != null) {
            refreshRoomLinesBeforeAdd(serviceTab, catalogId, dialog);
            return;
        }

        RoomLineDto existingLine = findExistingLine(serviceTab, catalogId);
        if (existingLine != null) {
            if (serviceTab) {
                increaseServiceQuantity(existingLine, dialog);
            } else {
                Toast.makeText(getContext(), "Tài sản này đã được chọn", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        repository.addRoomLine(serviceTab, selectedRoom.getRoomId(), catalogId, 1,
                new ServiceRepository.DataCallback<RoomLineDto>() {
                    @Override
                    public void onSuccess(RoomLineDto data) {
                        if (!isAdded())
                            return;
                        Toast.makeText(getContext(), serviceTab ? "Đã thêm dịch vụ" : "Đã thêm tài sản", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        setCurrentTab(serviceTab);
                        loadRoomLines(serviceTab);
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded())
                            return;
                        Log.e(TAG, "Cannot add room line: " + readableError(error));
                        Toast.makeText(getContext(),
                                serviceTab ? "Không thể thêm dịch vụ, vui lòng thử lại" : "Không thể thêm tài sản, vui lòng thử lại",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshRoomLinesBeforeAdd(boolean serviceTab, String catalogId, BottomSheetDialog dialog) {
        int roomId = selectedRoom.getRoomId();
        repository.fetchRoomLines(serviceTab, roomId, new ServiceRepository.DataCallback<List<RoomLineDto>>() {
            @Override
            public void onSuccess(List<RoomLineDto> data) {
                if (!isAdded() || selectedRoom == null)
                    return;
                List<RoomLineDto> safeData = data == null ? new ArrayList<>() : data;
                cacheRoomLines(serviceTab, safeData);
                addCatalogItemAfterDuplicateCheck(serviceTab, catalogId, dialog);
            }

            @Override
            public void onError(String error) {
                if (!isAdded())
                    return;
                Log.e(TAG, "Cannot refresh room lines before add: " + readableError(error));
                Toast.makeText(getContext(),
                        serviceTab ? "Không thể thêm dịch vụ, vui lòng thử lại" : "Không thể thêm tài sản, vui lòng thử lại",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cacheRoomLines(boolean serviceTab, List<RoomLineDto> lines) {
        if (serviceTab) {
            currentServiceLines.clear();
            currentServiceLines.addAll(lines);
        } else {
            currentAssetLines.clear();
            currentAssetLines.addAll(lines);
        }
    }

    private void addCatalogItemAfterDuplicateCheck(boolean serviceTab, String catalogId, BottomSheetDialog dialog) {
        RoomLineDto existingLine = findExistingLine(serviceTab, catalogId);
        if (existingLine != null) {
            if (serviceTab) {
                increaseServiceQuantity(existingLine, dialog);
            } else {
                Toast.makeText(getContext(), "Tài sản này đã được chọn", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        repository.addRoomLine(serviceTab, selectedRoom.getRoomId(), catalogId, 1,
                new ServiceRepository.DataCallback<RoomLineDto>() {
                    @Override
                    public void onSuccess(RoomLineDto data) {
                        if (!isAdded())
                            return;
                        Toast.makeText(getContext(), serviceTab ? "Đã thêm dịch vụ" : "Đã thêm tài sản", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        setCurrentTab(serviceTab);
                        loadRoomLines(serviceTab);
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded())
                            return;
                        Log.e(TAG, "Cannot add room line: " + readableError(error));
                        Toast.makeText(getContext(),
                                serviceTab ? "Không thể thêm dịch vụ, vui lòng thử lại" : "Không thể thêm tài sản, vui lòng thử lại",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void increaseServiceQuantity(RoomLineDto line, BottomSheetDialog dialog) {
        if (line.id == null || line.id.trim().isEmpty()) {
            Toast.makeText(getContext(), "Dịch vụ này đã được chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = line.quantity != null ? line.quantity : 1;
        repository.updateRoomLine(true, line.id, quantity + 1, new ServiceRepository.DataCallback<RoomLineDto>() {
            @Override
            public void onSuccess(RoomLineDto data) {
                if (!isAdded())
                    return;
                Toast.makeText(getContext(), "Đã thêm dịch vụ", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                setCurrentTab(true);
                loadRoomLines(true);
            }

            @Override
            public void onError(String error) {
                if (!isAdded())
                    return;
                Log.e(TAG, "Cannot increase service quantity: " + readableError(error));
                Toast.makeText(getContext(), "Không thể thêm dịch vụ, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    private RoomLineDto findExistingLine(boolean serviceTab, String catalogId) {
        List<RoomLineDto> lines = serviceTab ? currentServiceLines : currentAssetLines;
        for (RoomLineDto line : lines) {
            String lineCatalogId = getLineCatalogId(serviceTab, line);
            if (catalogId.equals(lineCatalogId)) {
                return line;
            }
        }
        return null;
    }

    private String getLineCatalogId(boolean serviceTab, RoomLineDto line) {
        if (line == null) {
            return null;
        }
        if (line.catalogId != null) {
            return line.catalogId;
        }
        return serviceTab ? line.serviceId : line.assetId;
    }

    private void showAddLineDialog() {
        if (selectedRoom == null)
            return;
        boolean targetServiceTab = isServiceTab;
        repository.fetchCatalog(targetServiceTab, "", new ServiceRepository.DataCallback<List<CatalogItemDto>>() {
            @Override
            public void onSuccess(List<CatalogItemDto> data) {
                if (!isAdded())
                    return;
                if (data == null || data.isEmpty()) {
                    Toast.makeText(getContext(), targetServiceTab ? "Chưa có dịch vụ" : "Chưa có bồi thường",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                showCatalogPickerDialog(targetServiceTab, data);
            }

            @Override
            public void onError(String error) {
                if (!isAdded())
                    return;
                Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCatalogPickerDialog(boolean serviceTab, List<CatalogItemDto> catalog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.popup_room_item, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = view.findViewById(R.id.tvRoomItemDialogTitle);
        Spinner spCatalog = view.findViewById(R.id.spCatalog);
        EditText edtQuantity = view.findViewById(R.id.edtQuantity);
        TextView tvPreviewTotal = view.findViewById(R.id.tvPreviewTotal);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        tvTitle.setText(serviceTab ? "Thêm dịch vụ vào phòng" : "Thêm tài sản vào phòng");
        List<String> labels = new ArrayList<>();
        for (CatalogItemDto item : catalog) {
            labels.add(safeText(item.name) + " - " + formatMoney(item.price != null ? item.price : 0));
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCatalog.setAdapter(spinnerAdapter);

        Runnable updateTotal = () -> {
            int position = spCatalog.getSelectedItemPosition();
            int quantity = parseQuantity(edtQuantity.getText() == null ? "" : edtQuantity.getText().toString()) == null
                    ? 0
                    : parseQuantity(edtQuantity.getText().toString());
            double price = position >= 0 ? (catalog.get(position).price != null ? catalog.get(position).price : 0) : 0;
            tvPreviewTotal.setText("Thành tiền: " + formatMoney(price * quantity));
        };

        spCatalog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                updateTotal.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateTotal.run();
            }
        });
        edtQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotal.run();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            int position = spCatalog.getSelectedItemPosition();
            if (position < 0)
                return;
            Integer quantity = parseQuantity(edtQuantity.getText() == null ? "" : edtQuantity.getText().toString());
            if (quantity == null) {
                edtQuantity.setError("Số lượng phải là số nguyên dương");
                return;
            }
            String catalogId = catalog.get(position).id;
            if (catalogId == null || selectedRoom == null)
                return;

            repository.addRoomLine(serviceTab, selectedRoom.getRoomId(), catalogId, quantity,
                    new ServiceRepository.DataCallback<RoomLineDto>() {
                        @Override
                        public void onSuccess(RoomLineDto data) {
                            if (!isAdded())
                                return;
                            dialog.dismiss();
                            setCurrentTab(serviceTab);
                            loadRoomLines(serviceTab);
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded())
                                return;
                            Toast.makeText(getContext(), readableError(error), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        updateTotal.run();
        dialog.show();
    }

    @Nullable
    private Integer parseQuantity(String value) {
        try {
            int quantity = Integer.parseInt(value.trim());
            return quantity > 0 ? quantity : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String readableStatus(String status) {
        if (status == null || status.trim().isEmpty())
            return "Đang sử dụng";
        if ("OCCUPIED".equalsIgnoreCase(status) || "CHECKED_IN".equalsIgnoreCase(status)
                || "Bận".equalsIgnoreCase(status)) {
            return "Đang sử dụng";
        }
        return status;
    }

    private String formatDate(String raw) {
        if (raw == null || raw.trim().isEmpty())
            return "-";
        String value = raw.trim();
        try {
            return OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value.replace(" ", "T")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (DateTimeParseException ignored) {
        }
        return value;
    }

    private String formatMoney(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(Math.round(value)) + "đ";
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String readableError(String error) {
        return error == null || error.trim().isEmpty() ? "Có lỗi xảy ra" : error;
    }
}
