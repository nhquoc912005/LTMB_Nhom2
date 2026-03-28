package com.project_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project_mobile.check_in.StayFragment;
import com.project_mobile.datphong_mobile.BookingManagementFragment;
import com.project_mobile.service.RoomMapFragment;
import com.project_mobile.service.ServiceFragment;
import com.project_mobile.Quan_ly_phong.RoomManagementFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Thiết lập Listener trước để đảm bảo bắt được sự kiện click
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new RoomMapFragment();
            } else if (itemId == R.id.nav_stay) {
                selectedFragment = new StayFragment();
            } else if (itemId == R.id.nav_booking) {
                selectedFragment = new BookingManagementFragment();
            } else if (itemId == R.id.nav_room_manage) {
                selectedFragment = new RoomManagementFragment();
            } else if (itemId == R.id.nav_service) {
                selectedFragment = new ServiceFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Sau đó mới đặt item mặc định
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
