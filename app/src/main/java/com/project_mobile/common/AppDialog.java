package com.project_mobile.common;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.project_mobile.R;

public final class AppDialog {

    private AppDialog() {
    }

    public static void showSuccess(Context context, String message) {
        AlertDialog dialog = createDialog(context, R.layout.layout_dialog_success);
        dialog.setOnShowListener(d -> styleWindow(dialog, 0.86f));
        dialog.show();

        TextView tvMessage = dialog.findViewById(R.id.tvSuccessMessage);
        MaterialButton btnDone = dialog.findViewById(R.id.btnDone);
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> dialog.dismiss());
        }
    }

    public static void showError(Context context, String message) {
        showConfirm(context, "Có lỗi xảy ra", message, "Đã hiểu", false, null);
    }

    public static void showConfirm(
            Context context,
            String title,
            String message,
            String confirmText,
            boolean danger,
            @Nullable Runnable onConfirm
    ) {
        AlertDialog dialog = createDialog(context, R.layout.layout_dialog_confirm);
        dialog.setOnShowListener(d -> styleWindow(dialog, 0.86f));
        dialog.show();

        TextView tvTitle = dialog.findViewById(R.id.tvConfirmTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvConfirmMessage);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = dialog.findViewById(R.id.btnConfirm);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
        if (btnConfirm != null) {
            btnConfirm.setText(confirmText);
            btnConfirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    Color.parseColor(danger ? "#DD5A5D" : "#A87548")
            ));
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            });
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
            if (onConfirm == null && btnConfirm != null) {
                btnCancel.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnConfirm.getLayoutParams();
                params.setMarginStart(0);
                btnConfirm.setLayoutParams(params);
            }
        }
    }

    private static AlertDialog createDialog(Context context, int layoutRes) {
        View view = LayoutInflater.from(context).inflate(layoutRes, null);
        AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private static void styleWindow(AlertDialog dialog, float widthRatio) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int) (dialog.getContext().getResources().getDisplayMetrics().widthPixels * widthRatio);
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
    }
}
