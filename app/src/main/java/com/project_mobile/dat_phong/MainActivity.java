package com.project_mobile.dat_phong;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project_mobile.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
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
            }
            return true;
        });
    }
}
