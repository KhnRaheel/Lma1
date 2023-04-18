package com.lma.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.lma.R;
import com.lma.info.Info;
import com.lma.model.Device;
import com.lma.singletons.DeviceSingleton;
import com.lma.utils.DialogUtils;
import com.lma.utils.Utils;

public class TrackingDevice extends AppCompatActivity implements Info {
    Context context;
    Dialog dgLoading;
    EditText etName;
    EditText etModelNumber;
    EditText etIMEI;
    EditText etPhone;

    String strEtPhone;
    String strEtName;
    String strEtModelNumber;
    String strEtIMEI;
    AppCompatButton btnRing;
    AppCompatButton btnMap;
    AppCompatButton btnReqCall;
    AppCompatButton btnRemove;
   DeviceSingleton  deviceSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_device);
        if (DeviceSingleton.getInstance() == null)
            finish();
        initViews();
        dgLoading=new Dialog(this);
        DialogUtils.initLoadingDialog(dgLoading);
        Device device=new Device(DeviceSingleton.getInstance().getId(),
                strEtName, strEtModelNumber, strEtIMEI, strEtPhone);


        btnRing=findViewById(R.id.btn_ring);
        btnMap=findViewById(R.id.btn_map);
        btnReqCall=findViewById(R.id.btn_request_call);
        btnRemove=findViewById(R.id.btn_remove);
        btnMap.setOnClickListener(view -> btnMap(device));
        btnRing.setOnClickListener(view -> btnRing(device));
        btnReqCall.setOnClickListener(view -> btnReqCall(device));
        btnRemove.setOnClickListener(view -> btnRemove(device));


    }

    private void btnRemove(Device device) {
        FirebaseDatabase.getInstance().getReference()
                .child(NODE_DEVICES)
                .child(Utils.getCurrentUserId())
                .child(device.getIMEI())
                .removeValue();
    }

    private void btnReqCall(Device device) {
        sendMessage(device.getPhone(), device.getIMEI(), COMMAND_CALL);

    }
    private void sendMessage(String phoneNo, String imei, String command) {
        if (!checkSMSPermission())
            return;

        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(phoneNo, null, imei + " " + command, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "SMS sent.",
                Toast.LENGTH_LONG).show();
    }
    private boolean checkSMSPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("asd", "Check self permission");
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    Manifest.permission.SEND_SMS)) {
                Log.i("asd", "should show permission dialog");
            } else {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.SEND_SMS},
                        11);
            }
            return false;
        } else {
            Log.i("long", "///////Permission granted");
            return true;
        }
    }

    private void btnRing(Device device) {
        sendMessage(device.getPhone(), device.getIMEI(), COMMAND_RING);
    }

    private void btnMap(Device device) {
        sendMessage(device.getPhone(), device.getIMEI(), COMMAND_MAP);

    }

    private void initViews() {
        etName = findViewById(R.id.et_user_name);
        etModelNumber = findViewById(R.id.et_email);
        etIMEI = findViewById(R.id.et_pass);
        etPhone = findViewById(R.id.et_phone);

        etName.setText(DeviceSingleton.getInstance().getName());
        etModelNumber.setText(DeviceSingleton.getInstance().getModelNumber());
        etIMEI.setText(DeviceSingleton.getInstance().getIMEI());
        etPhone.setText(DeviceSingleton.getInstance().getPhone());

    }

    public void trackDevice(View view) {

    }
}