package com.project_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        // Mặc định chọn Quản lý phòng
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_room_manage);
            loadFragment(new RoomManagementFragment());
        }

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
