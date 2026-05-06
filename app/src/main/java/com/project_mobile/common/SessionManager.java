// Module common/session Android.
// File này lưu và đọc phiên đăng nhập cục bộ bằng SharedPreferences.
// Dữ liệu chính là UserDto được serialize thành JSON với key current_user.
package com.project_mobile.common;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.project_mobile.network.ApiModels.UserDto;

/**
 * SessionManager là lớp tiện ích quản lý người dùng đang đăng nhập.
 * Các màn như ProfileFragment và MainActivity dùng class này để lấy user hoặc đăng xuất.
 */
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

    /** Lưu UserDto sau khi login thành công. */
    public void saveUser(UserDto user) {
        String json = new Gson().toJson(user);
        editor.putString(KEY_USER, json);
        editor.apply();
    }

    /** Đọc UserDto hiện tại; trả null nếu chưa đăng nhập hoặc đã logout. */
    public UserDto getUser() {
        String json = pref.getString(KEY_USER, null);
        if (json == null) return null;
        return new Gson().fromJson(json, UserDto.class);
    }

    /** Xóa toàn bộ dữ liệu phiên đăng nhập cục bộ. */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
