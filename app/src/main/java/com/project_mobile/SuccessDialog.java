package com.project_mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

public class SuccessDialog {

    public static void show(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_success, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMessage = view.findViewById(R.id.tvSuccessMessage);
        tvMessage.setText(message);

        MaterialButton btnDone = view.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
