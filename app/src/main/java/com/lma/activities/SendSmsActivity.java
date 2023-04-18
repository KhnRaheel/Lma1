package com.lma.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.lma.R;
import com.lma.services.GPSTracker;

public class SendSmsActivity extends AppCompatActivity {
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            String phoneNo = getIntent().getStringExtra("phoneNo");
            SmsManager smsManager = SmsManager.getDefault();
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            smsManager.sendTextMessage(phoneNo, null, "device_location," + lat + "," + lng, null, null);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    int LOCATION_REFRESH_TIME = 500; // 15 seconds to update
    int LOCATION_REFRESH_DISTANCE = 5; // 500 meters to update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        Log.i("TAG", "onReceive: INSIDE SEND SMS ACTIVITY");
        Toast.makeText(this, "SENDING ACTIVITY", Toast.LENGTH_SHORT).show();
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        Intent i = getPackageManager().getLaunchIntentForPackage("com.lma");
        startActivity(i);
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);

        GPSTracker gpsTracker = new GPSTracker(this);
        String phoneNo = getIntent().getStringExtra("phoneNo");
        SmsManager smsManager = SmsManager.getDefault();
        double lat = gpsTracker.getLatitude();
        double lng = gpsTracker.getLongitude();
        smsManager.sendTextMessage(phoneNo, null, "device_location," + lat + "," + lng, null, null);

    }

}
