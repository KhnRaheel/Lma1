package com.lma.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.lma.R;

import java.util.Objects;

public class DialogUtils {
    public static void setDefaultDialogProperties(Dialog dg) {
        dg.setCanceledOnTouchOutside(true);
        dg.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dg.getWindow();
        lp.copyFrom(Objects.requireNonNull(window).getAttributes());

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.setAttributes(lp);
        Objects.requireNonNull(dg.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
    }

    public static void initLoadingDialog(Dialog loadingDialog) {
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCanceledOnTouchOutside(false);
        setDefaultDialogProperties(loadingDialog);
    }

    public static void settingsDialog(Activity context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Permission Settings");
        alertDialog.setMessage("Application might not work, since some permissions are denied. Please allow all permissions from settings.");
        alertDialog.setPositiveButton("Go to settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

}
