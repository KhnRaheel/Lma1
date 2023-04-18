package com.lma.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefUtils {

    public static void putStringSharedPrefs(Activity context, String value, String key) {
        SharedPreferences mPrefs = context.getSharedPreferences("SharedPrefs", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void putBooleanSharedPrefs(Activity context, Boolean value, String key) {
        SharedPreferences mPrefs = context.getSharedPreferences("SharedPrefs", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void putIntSharedPrefs(Activity context, int value, String key) {
        SharedPreferences mPrefs = context.getSharedPreferences("SharedPrefs", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getIntSharedPrefs(Activity context, String key) {
        SharedPreferences mPrefs = context.getSharedPreferences("SharedPrefs", 0);
        return mPrefs.getInt(key, 0);
    }

    public static String getStringSharedPrefs(Context context, String key) {
        SharedPreferences mPrefs = context.getSharedPreferences("SharedPrefs", 0);
        return mPrefs.getString(key, "");
    }

    public static boolean getBooleanSharedPrefs(Context context, String key) {
        SharedPreferences mPrefs = context.getSharedPreferences("SharedPrefs", 0);
        return mPrefs.getBoolean(key, false);
    }


    public static void putFirstRun(boolean isFirst, Activity context) {
        SharedPreferences mPrefs = context.getSharedPreferences("IDvalue", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean("IS_FIRST", isFirst);
        editor.apply();
    }

    public static boolean isFirstRun(Activity context) {
        SharedPreferences mPrefs = context.getSharedPreferences("IDvalue", 0);
        return mPrefs.getBoolean("IS_FIRST", true);
    }

    public static void putDesc(String desc, Activity context) {
        SharedPreferences mPrefs = context.getSharedPreferences("IDvalue", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("Desc", desc);
        editor.apply();
    }

    public static String getDesc(Activity context) {
        SharedPreferences mPrefs = context.getSharedPreferences("IDvalue", 0);
        return mPrefs.getString("Desc", "");
    }

}
