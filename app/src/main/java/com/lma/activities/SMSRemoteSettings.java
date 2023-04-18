package com.lma.activities;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.lma.R;
import com.lma.services.LockScreenManager;

import java.util.Objects;

public class SMSRemoteSettings extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH2 = "switch2";
    public static final String SWITCH3 = "switch3";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    CheckBox locationSwitch;
    Button btnChangePassword;
    CheckBox checkScream, checkFlash;
    private boolean switchReceiver;
    private boolean switchReceiverFlash;
    private InterstitialAd mInterstitialAd;

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_m_s_remote_settings);
        loadData();
        prepareAD();
        locationSwitch = findViewById(R.id.checkbox_location);
        btnChangePassword = findViewById(R.id.change_password);
        checkScream = findViewById(R.id.check_scream);
        checkFlash = findViewById(R.id.flash_Check);
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkScream.setChecked(switchReceiver);
        checkFlash.setChecked(switchReceiverFlash);
        if (LockScreenManager.screamOn) {
            checkScream.setChecked(true);
        }
        if (LockScreenManager.flashOn) {
            checkFlash.setChecked(true);
        }

        boolean statusOfGPS = false;
        if (manager != null) {
            statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        if (statusOfGPS) {
            LockScreenManager.locationOn = true;
            locationSwitch.setChecked(true);
        } else {
            LockScreenManager.locationOn = false;
            locationSwitch.setChecked(false);
        }

    }

    public void requestLocationPermission(View view) {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        checkLocationPermission();
        LockScreenManager.locationOn = true;
        locationSwitch.setChecked(true);

        if (!isLocationEnabled(this)) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("GPS is settings");
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
            alertDialog.setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });


            alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            alertDialog.show();
        }


    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("location permission")
                        .setMessage("text location permission")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(SMSRemoteSettings.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                Log.i("All", "All location services are satisfid");
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(SMSRemoteSettings.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    public void enableScream(View view) {
        if (checkScream.isChecked()) {
            checkScream.setChecked(false);
            LockScreenManager.screamOn = false;
        } else {
            checkScream.setChecked(true);
            LockScreenManager.screamOn = true;
        }
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    finish();
                }
            });
        } else {
            super.onBackPressed();
        }
        // super.onBackPressed();
        //startActivity(new Intent(SMSRemoteSettings.this, MainActivity.class));
        //finish();
    }

    public void changeServicePassword(View view) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SMSRemoteSettings.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        final EditText editText = bottomSheetDialog.findViewById(R.id.new_password);
        final EditText editText1 = bottomSheetDialog.findViewById(R.id.confirm_password);
        final EditText editText2 = bottomSheetDialog.findViewById(R.id.prev_password);

        Button btn = bottomSheetDialog.findViewById(R.id.btn_confirm);
        bottomSheetDialog.show();

        Objects.requireNonNull(btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = editText.getText().toString().toLowerCase();
                String confirmPassword = editText1.getText().toString().toLowerCase();
                String previousPassword = editText2.getText().toString().toLowerCase();
                if (previousPassword.equals(LockScreenManager.activationKey)) {
                    if (!newPassword.equals("")) {
                        if (newPassword.equals(confirmPassword)) {
                            LockScreenManager.activationKey = confirmPassword.toLowerCase();
                            Toast.makeText(SMSRemoteSettings.this, "Password is updated", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.cancel();
                        } else {
                            editText1.setError("passwords not matched");
                        }
                    } else {
                        editText1.setError("Field Empty");
                    }
                } else {
                    editText2.setError("Previous Password incorrect");
                }

            }
        });
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH2, checkScream.isChecked());
        editor.putBoolean(SWITCH3, checkFlash.isChecked());

        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        switchReceiver = sharedPreferences.getBoolean(SWITCH2, false);
        switchReceiverFlash = sharedPreferences.getBoolean(SWITCH3, false);
    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }

    public void resetPassword(View view) {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        createLocationRequest();
                        /*locationManager.requestLocationUpdates(provider, 400, 1, this);*/
                    }

                } else {
                    Toast.makeText(this, "Allow Permissions for App to work properly", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }

        }
    }

    public void blinkFlash(View view) {
        if (checkFlash.isChecked()) {
            checkFlash.setChecked(false);
            LockScreenManager.flashOn = false;
        } else {
            checkFlash.setChecked(true);
            LockScreenManager.flashOn = true;
        }
    }

    public void instructionsSms(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("SMS Instructions");

        alertDialog.setMessage("Send a text SMS (in the following format) back to your phone when it is lost " +
                "to find it.");
        LayoutInflater factory = LayoutInflater.from(this);
        final View views = factory.inflate(R.layout.sample, null);
        alertDialog.setView(views);
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
