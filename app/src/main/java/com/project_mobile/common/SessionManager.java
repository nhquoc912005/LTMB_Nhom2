package com.project_mobile.common;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.project_mobile.network.ApiModels.UserDto;

public class SessionManager {
    private static final String PREF_NAME = "HotelSession";
    private static final String KEY_USER = "current_user";
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveUser(UserDto user) {
        String json = new Gson().toJson(user);
        editor.putString(KEY_USER, json);
        editor.apply();
    }

    public UserDto getUser() {
        String json = pref.getString(KEY_USER, null);
        if (json == null) return null;
        return new Gson().fromJson(json, UserDto.class);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
