package com.project_mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project_mobile.Quan_ly_phong.RoomManagementFragment;
import com.project_mobile.check_in.StayFragment;
import com.project_mobile.datphong_mobile.BookingManagementFragment;
import com.project_mobile.service.RoomMapFragment;
import com.project_mobile.service.ServiceFragment;
import com.project_mobile.user.UserManagementFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        ImageView menuButton = findViewById(R.id.ivMenu);
        menuButton.setOnClickListener(v -> showAppMenu(v));

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_stay) {
                selectedFragment = new StayFragment();
            } else if (itemId == R.id.nav_booking) {
                selectedFragment = new BookingManagementFragment();
            } else if (itemId == R.id.nav_room_manage) {
                selectedFragment = new RoomManagementFragment();
            } else if (itemId == R.id.nav_service) {
                selectedFragment = new RoomMapFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    public void openStay(boolean showCheckIn) {
        loadFragment(StayFragment.newInstance(showCheckIn));
        bottomNav.getMenu().findItem(R.id.nav_stay).setChecked(true);
    }

    public void openUserManagement() {
        loadFragment(new UserManagementFragment());
        clearBottomSelection();
    }

    public void openServiceManagement() {
        loadFragment(new ServiceFragment());
        clearBottomSelection();
    }

    private void showAppMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.inflate(R.menu.app_shell_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_users) {
                openUserManagement();
                return true;
            } else if (itemId == R.id.menu_service_management) {
                openServiceManagement();
                return true;
            } else if (itemId == R.id.menu_account_info) {
                loadFragment(new ProfileFragment());
                clearBottomSelection();
                return true;
            } else if (itemId == R.id.menu_logout) {
                new com.project_mobile.common.SessionManager(this).logout();
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void clearBottomSelection() {
        bottomNav.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setChecked(false);
        }
        bottomNav.getMenu().setGroupCheckable(0, true, true);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
