package com.project_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project_mobile.datphong_mobile.BookingManagementFragment;

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
            if (item.getItemId() == R.id.nav_room_manage) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RoomManagementFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.nav_booking) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BookingManagementFragment())
                        .commit();
                return true;
            }
            return true;
        });
    }
}
