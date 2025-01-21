package com.lma.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lma.R;
import com.lma.info.Info;
import com.lma.model.Device;
import com.lma.model.UserPojo;
import com.lma.recievers.ServiceReceiver;
import com.lma.utils.DialogUtils;
import com.lma.utils.SharedPrefUtils;
import com.lma.utils.Utils;

import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class DashboardActivity extends AppCompatActivity implements Info ,EasyPermissions.PermissionCallbacks {

    Dialog loadingDialog;
    EditText etIMEI;
    String strEtIMEI;
    String currentIMEI;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        enableBroadcastReceiver();


        Utils.printDeviceInfo(this);

        initViews();

        loadingDialog = new Dialog(this);

        DialogUtils.initLoadingDialog(loadingDialog);
        currentIMEI = SharedPrefUtils.getStringSharedPrefs(this, KEY_CURRENT_DEVICE_IMEI);

        Log.i(TAG, "onCreate: CURRENT IMEI " + currentIMEI + " AFTER");
        etIMEI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSave.setVisibility(View.VISIBLE);
            }
        });
        if (!currentIMEI.isEmpty()) {
            etIMEI.setText(currentIMEI);
            initDevice();
        } else
            btnSave.setVisibility(View.VISIBLE);
        permissions();

//        initPermissions();
        initUserData();
        initTextWatchers();
    }


    private void enableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, ServiceReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }

    private void initPermissions() {
        if (locationPermission() & smsPermission() & callPermission())
            Toast.makeText(this, "All permissions allowed", Toast.LENGTH_SHORT).show();
    }

    private boolean callPermission() {
        return !initPermission(Manifest.permission.CALL_PHONE, 4);
    }

    private boolean locationPermission() {
        if (initPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, 2))
            return false;
        return !initPermission(Manifest.permission.ACCESS_FINE_LOCATION, 3);
    }

    private boolean smsPermission() {
        return !initPermission(Manifest.permission.SEND_SMS, 1);
    }

    private boolean initPermission(String perm, int code) {
        Log.i(TAG, "initPermission: " + code);
        if (ContextCompat.checkSelfPermission(this, perm)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{perm}, code);
            Log.i(TAG, "initPermission: RETURNING TRUE" + code);
            return true;
        } else {
            Log.i(TAG, "initPermission: RETURNING FALSE" + code);
            return false;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    locationPermission();
                break;
            case 2:
            case 3:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    callPermission();
        }
    }

    private void initTextWatchers() {
        etIMEI.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                castStrings();
                if (!strEtIMEI.equals(currentIMEI))
                    btnSave.setVisibility(View.VISIBLE);
                else
                    btnSave.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void castStrings() {
        strEtIMEI = etIMEI.getText().toString();
    }

    private void initViews() {
        etIMEI = findViewById(R.id.et_imei);
        btnSave = findViewById(R.id.btn_save);
    }

    private void initUserData() {
        loadingDialog.show();
        FirebaseDatabase.getInstance().getReference()
                .child(NODE_USERS)
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loadingDialog.dismiss();
                        Utils.currentUser = snapshot.getValue(UserPojo.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initDevice() {
        Log.i(TAG, "initDevice: ");
        loadingDialog.show();
        Utils.getReference()
                .child(NODE_DEVICES)
                .child(Utils.getCurrentUserId())
                .child(currentIMEI)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                loadingDialog.dismiss();
                                Device device = snapshot.getValue(Device.class);
                                Log.i(TAG, "onDataChange: " + device);
                                if (device != null) {
                                    Utils.currentDevice = device;
                                    if (Utils.currentDevice.getId().isEmpty()
                                            | device.getName().isEmpty()
                                            | device.getModelNumber().isEmpty()) {
                                        Utils.currentDevice.setIMEI(strEtIMEI);
                                        Utils.currentDevice.setName(Build.BRAND);
                                        Utils.currentDevice.setId(Build.ID);
                                        Utils.currentDevice.setModelNumber(Build.MODEL);
                                        save(null);
                                    }
                                } else {
                                    Toast.makeText(DashboardActivity.this, "This device contact number not found please add contact number in phone recovery", Toast.LENGTH_LONG).show();
                                    btnSave.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );
    }

    public void tracking(View view) {
        startActivity(new Intent(this, TrackingActivity.class));
    }

    public void recovery(View view) {
        permissions();
        if (Utils.currentDevice == null) {
            Toast.makeText(this, "Please set current device IMEI first", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, RecoveryActivity.class));
    }

    public void lockSettings(View view) {
        startActivity(new Intent(DashboardActivity.this, LockSettingsActivity.class));
    }

    public void addDevice(View view) {
        startActivity(new Intent(this, AddDeviceActivity.class));
    }

    public void save(View view) {
        castStrings();
        SharedPrefUtils.putStringSharedPrefs(this, strEtIMEI, KEY_CURRENT_DEVICE_IMEI);
        Log.i(TAG, "save: DEVICE IMEI SAVED TO SHARED PREFS " + strEtIMEI);
        try {
            Utils.currentDevice.setPhone(Utils.currentDevice.getPhone());
            Utils.currentDevice.setIMEI(strEtIMEI);
        } catch (Exception e) {
            Utils.currentDevice = new Device();
            Utils.currentDevice.setIMEI(strEtIMEI);
            Utils.currentDevice.setName(Build.BRAND);
            Utils.currentDevice.setId(Build.ID);
            Utils.currentDevice.setModelNumber(Build.MODEL);

        }
        loadingDialog.show();
        String id = Utils.currentDevice.getIMEI();
        Utils.getReference()
                .child(NODE_DEVICES)
                .child(Utils.getCurrentUserId())
                .child(id)
                .setValue(Utils.currentDevice)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        btnSave.setVisibility(View.GONE);
                        etIMEI.clearFocus();
                    } else
                        Toast.makeText(DashboardActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                });
    }

    private void permissions() {
        EasyPermissions.requestPermissions(this, "Location Permission", 1, Manifest.permission.ACCESS_FINE_LOCATION);
        EasyPermissions.requestPermissions(this, "SMS Permission", 2, Manifest.permission.SEND_SMS);
        EasyPermissions.requestPermissions(this, "CALL Permission", 3, Manifest.permission.CALL_PHONE);
        EasyPermissions.requestPermissions(this,"background",4,Manifest.permission.ACCESS_BACKGROUND_LOCATION);

    }

    @Override
    public void onPermissionsGranted(int i, @NonNull List<String> list) {

    }

    @Override
    public void onPermissionsDenied(int i, @NonNull List<String> list) {
        permissions();
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}