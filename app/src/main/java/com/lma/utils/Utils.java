package com.lma.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lma.info.Info;
import com.lma.model.Device;
import com.lma.model.UserPojo;

import java.util.Objects;


public class Utils implements Info {
    public static UserPojo currentUser;
    public static Device currentDevice;

    public static DatabaseReference getReference() {
        return FirebaseDatabase.getInstance("https://final-year-534ca-default-rtdb.firebaseio.com/").getReference();
    }

    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static void printDeviceInfo(Context context) {
        Log.i(TAG, "SERIAL: " + Build.SERIAL);
        Log.i(TAG, "MODEL: " + Build.MODEL);
        Log.i(TAG, "ID: " + Build.ID);
        Log.i(TAG, "Manufacture: " + Build.MANUFACTURER);
        Log.i(TAG, "brand: " + Build.BRAND);
        Log.i(TAG, "type: " + Build.TYPE);
        Log.i(TAG, "user: " + Build.USER);
        Log.i(TAG, "BASE: " + Build.VERSION_CODES.BASE);
        Log.i(TAG, "INCREMENTAL " + Build.VERSION.INCREMENTAL);
        Log.i(TAG, "SDK  " + Build.VERSION.SDK);
        Log.i(TAG, "BOARD: " + Build.BOARD);
        Log.i(TAG, "BRAND " + Build.BRAND);
        Log.i(TAG, "HOST " + Build.HOST);
        Log.i(TAG, "FINGERPRINT: " + Build.FINGERPRINT);
        Log.i(TAG, "Version Code: " + Build.VERSION.RELEASE);

    }

    public static String getCurrentUserId() {
        String uid = null;
        try {
            uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uid;
    }

    public static boolean validEt(EditText etUserName, String strEtUserName) {
        if (strEtUserName.isEmpty()) {
            etUserName.setError("Field Empty");
            return false;
        }
        return true;
    }

    public static boolean isPermissions(Activity context) {
        if (smsPermission(context))
            if (locationPermission(context))
                if (callPermission(context))
                    return true;
                else
                    DialogUtils.settingsDialog(context);
        return false;

    }

    private static boolean callPermission(Activity context) {
        return !initPermission(context, Manifest.permission.CALL_PHONE, 4);
    }

    private static boolean locationPermission(Activity context) {
        if (initPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION, 2))
            return false;
        return !initPermission(context, Manifest.permission.ACCESS_FINE_LOCATION, 3);
    }

    private static boolean smsPermission(Activity context) {
        return !initPermission(context, Manifest.permission.SEND_SMS, 1);

    }

    private static boolean initPermission(Activity context, String perm, int code) {
        if (ContextCompat.checkSelfPermission(context, perm)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                    new String[]{perm}, code);
            return true;
        } else
            return false;

    }

}
