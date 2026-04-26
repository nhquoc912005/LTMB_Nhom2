package com.project_mobile.Quan_ly_phong;

import android.content.Context;

import com.project_mobile.common.AppDialog;

public class SuccessDialog {

    public static void show(Context context, String message) {
        AppDialog.showSuccess(context, message);
    }
}
