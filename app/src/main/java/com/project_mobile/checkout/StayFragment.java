// Module trả phòng Android.
// File này là container tab lưu trú phiên bản cũ, mặc định mở CheckoutFragment.
// Dữ liệu chính chỉ là trạng thái tab UI, không gọi API trực tiếp.
package com.project_mobile.checkout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project_mobile.R;

/**
 * StayFragment trong package checkout là container cũ cho màn trả phòng.
 * Luồng điều hướng hiện tại chủ yếu dùng com.project_mobile.check_in.StayFragment.
 */
public class StayFragment extends Fragment {

    private TextView tabCheckIn, tabCheckOut;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stay, container, false);

        tabCheckIn = view.findViewById(R.id.tabCheckIn);
        tabCheckOut = view.findViewById(R.id.tabCheckOut);

        // Mặc định chọn Trả phòng để hiển thị giống hình của bạn
        loadFragment(new CheckoutFragment());
        updateTabUI(false);

        tabCheckIn.setOnClickListener(v -> {
            updateTabUI(true);
            // Sau này bạn thêm CheckInFragment vào đây: loadFragment(new CheckInFragment());
        });

        tabCheckOut.setOnClickListener(v -> {
            updateTabUI(false);
            loadFragment(new CheckoutFragment());
        });

        return view;
    }

    private void updateTabUI(boolean isCheckInSelected) {
        if (isCheckInSelected) {
            tabCheckIn.setBackgroundResource(R.drawable.bg_tab_active);
            tabCheckIn.setTextColor(Color.parseColor("#C0410D"));
            tabCheckOut.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabCheckOut.setTextColor(Color.parseColor("#888888"));
        } else {
            tabCheckOut.setBackgroundResource(R.drawable.bg_tab_active);
            tabCheckOut.setTextColor(Color.parseColor("#C0410D"));
            tabCheckIn.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabCheckIn.setTextColor(Color.parseColor("#888888"));
        }
    }

    private void loadFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.stay_content_frame, fragment)
                .commit();
    }
}
