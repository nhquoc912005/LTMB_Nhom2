package com.project_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project_mobile.checkout.StayFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Mặc định chọn Quản lý phòng
        bottomNav.setSelectedItemId(R.id.nav_room_manage);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RoomManagementFragment())
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_room_manage) {
                return true;
            } 
            // Xử lý khi nhấn vào Dịch vụ -> Hiện sơ đồ phòng
            else if (itemId == R.id.nav_service) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RoomMapFragment())
                        .commit();
                return true;
            }
            // Bắt sự kiện nút Lưu trú
            else if (item.getItemId() == R.id.nav_stay) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new StayFragment())
                    .commit();
            return true;
        }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
