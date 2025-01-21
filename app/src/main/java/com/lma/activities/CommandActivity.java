package com.lma.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lma.R;

import static com.lma.info.Info.COMMAND_CALL;
import static com.lma.info.Info.COMMAND_MAP;
import static com.lma.info.Info.COMMAND_RING;

public class CommandActivity extends AppCompatActivity {
    String phone = "";
    String IMEI = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);
        phone = this.getIntent().getStringExtra("getPhone");
        IMEI = this.getIntent().getStringExtra("getIMEI");
    }

    public void ringDevice(View view) {
        btnRing(phone , IMEI);
    }
    public void tracking(View view) {
        btnMap(phone, IMEI);
    }

    public void requestCall(View view) {
        btnReqCall(phone, IMEI);

    }

    private void btnRing(String phone, String imei) {
        sendMessage(phone, imei, COMMAND_RING);
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

        Toast.makeText(this, "SMS sent.",
                Toast.LENGTH_LONG).show();
    }

    private boolean checkSMSPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("asd", "Check self permission");
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) this,
                    Manifest.permission.SEND_SMS)) {
                Log.i("asd", "should show permission dialog");
            } else {
                ActivityCompat.requestPermissions((Activity) this,
                        new String[]{Manifest.permission.SEND_SMS},
                        11);
            }
            return false;
        } else {
            Log.i("long", "///////Permission granted");
            return true;
        }
    }



    public void removeDevice(View view) {
    }


    private void btnReqCall(String phone, String imei) {
//         REQUEST A CALL FROM END DEVICE
        sendMessage(phone , imei, COMMAND_CALL);

    }
    private void btnMap(String phone , String imei) {
        sendMessage(phone , imei, COMMAND_MAP);

    }
    public void DeviceDetails(View view) {
        startActivity(new Intent(this,DeviceDetailsActivity.class));
    }
}