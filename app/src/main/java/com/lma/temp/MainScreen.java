package com.lma.temp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.lma.R;
import com.lma.activities.LockSettingsActivity;
import com.lma.activities.SetPatternActivity;
import com.lma.services.LockScreenManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainScreen extends AppCompatActivity {

    public final static int REQUEST_CODE = 10101;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    public static String imageURI;
    public static Drawable drawable;
    @SuppressLint("StaticFieldLeak")
    public static Switch powerOffPrevention;
    private static int RESULT_LOAD_IMG = 1;
    String pattern;
    Intent i;
    Context context = MainScreen.this;
    Button setPattern;
    String imgpath, storedpath;
    SharedPreferences sp;
//    ImageView imageView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        loadData();
//        imageView = findViewById(R.id.image);
        if (LockScreenManager.pattern != null) {
            pattern = LockScreenManager.pattern;
        } else {
            LockScreenManager.pattern = pattern;
        }

        sp = getSharedPreferences("setback", MODE_PRIVATE);
        if (sp.contains("imagepath")) {
            storedpath = sp.getString("imagepath", "");
            Uri uri = Uri.fromFile(new File(storedpath));
            Drawable bg;
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                bg = Drawable.createFromStream(inputStream, uri.toString());
            } catch (FileNotFoundException e) {
                bg = ContextCompat.getDrawable(this, R.drawable.backgrnd);
            }


            if (isMyServiceRunning()) {
                LockScreenManager.imagBack = bg;
                LockScreenManager.imageURI = uri.toString();
            }
            LockSettingsActivity.drawable = bg;
            LockSettingsActivity.imageURI = uri.toString();
//            imageView.setImageDrawable(bg);
        }  //            imageView.setImageResource(R.drawable.lockscreen_background);


        setPattern = findViewById(R.id.set_pattern);
        i = new Intent(this, LockScreenManager.class);
        powerOffPrevention = findViewById(R.id.preventions_state);

        if (isMyServiceRunning()) {
            powerOffPrevention.setChecked(true);
        }

        if (pattern != null) {
            setPattern.setText("CHANGE PATTERN");
        } else {
            setPattern.setText("Set Pattern");
        }
    }


    public void disableLockScreen(View view) {
        powerOffPrevention.setChecked(false);
        stopService(i);
    }

    public void SetPattern(View view) {
        startActivity(new Intent(this, SetPatternActivity.class));
        finish();
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LockScreenManager.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }

    public void checkPermission(View view) {
        if (checkDrawOverlayPermission()) {
            if (LockScreenManager.pattern != null && !isMyServiceRunning()) {
                powerOffPrevention.setChecked(true);
                startService(i);
            } else if (isMyServiceRunning()) {
                Toast.makeText(context, "Already Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Set A Pattern First", Toast.LENGTH_SHORT).show();
            }

        }

    }

    public boolean checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT, LockScreenManager.pattern);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        pattern = sharedPreferences.getString(TEXT, null);
        Log.i("adasd", "PATTERN =  " + pattern);
    }

    public void changeBackground(View view) {
        loadImagefromGallery();

    }

    public void loadImagefromGallery() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.MediaColumns.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgpath = cursor.getString(columnIndex);
                Log.i("path", "////////////////////////" + imgpath);
                cursor.close();

                SharedPreferences.Editor edit = sp.edit();
                edit.putString("imagepath", imgpath);
                edit.commit();

                Uri uri = Uri.fromFile(new File(imgpath));
                Drawable bg;
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    bg = Drawable.createFromStream(inputStream, uri.toString());
                } catch (FileNotFoundException e) {
                    bg = ContextCompat.getDrawable(this, R.drawable.backgrnd);
                }

//                imageView.setImageDrawable(bg);
                if (isMyServiceRunning()) {
                    LockScreenManager.imagBack = bg;
                    LockScreenManager.imageURI = uri.toString();
                }
                LockSettingsActivity.drawable = bg;
                LockSettingsActivity.imageURI = uri.toString();
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void instructions(View view) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Lock screen Instructions");
        alertDialog.setMessage("To avoid any issue set device lock to none from settings.");

        alertDialog.setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();

    }

    public void prepareAD() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }
}