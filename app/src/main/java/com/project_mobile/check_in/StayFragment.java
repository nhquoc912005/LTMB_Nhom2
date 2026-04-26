package com.project_mobile.check_in;

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
import com.project_mobile.checkout.CheckoutFragment;

public class StayFragment extends Fragment {

    private static final String ARG_SHOW_CHECK_IN = "show_check_in";

    private TextView tabCheckIn;
    private TextView tabCheckOut;

    public static StayFragment newInstance(boolean showCheckIn) {
        StayFragment fragment = new StayFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_CHECK_IN, showCheckIn);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stay, container, false);

        tabCheckIn = view.findViewById(R.id.tabCheckIn);
        tabCheckOut = view.findViewById(R.id.tabCheckOut);

        boolean showCheckIn = getArguments() == null || getArguments().getBoolean(ARG_SHOW_CHECK_IN, true);
        openTab(showCheckIn);

        tabCheckIn.setOnClickListener(v -> openTab(true));
        tabCheckOut.setOnClickListener(v -> openTab(false));

        return view;
    }

    private void openTab(boolean showCheckIn) {
        updateTabUI(showCheckIn);
        loadFragment(showCheckIn ? new CheckInFragment() : new CheckoutFragment());
    }

    private void updateTabUI(boolean isCheckInSelected) {
        if (isCheckInSelected) {
            tabCheckIn.setBackgroundResource(R.drawable.bg_tab_active);
            tabCheckIn.setTextColor(Color.parseColor("#C0410D"));
            tabCheckOut.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabCheckOut.setTextColor(Color.parseColor("#6D5D51"));
        } else {
            tabCheckOut.setBackgroundResource(R.drawable.bg_tab_active);
            tabCheckOut.setTextColor(Color.parseColor("#C0410D"));
            tabCheckIn.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabCheckIn.setTextColor(Color.parseColor("#6D5D51"));
        }
    }

    private void loadFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.stay_content_frame, fragment)
                .commit();
    }
}
