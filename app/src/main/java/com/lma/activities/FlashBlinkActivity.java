package com.lma.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lma.R;

public class FlashBlinkActivity extends AppCompatActivity {
    boolean hasCameraFlash = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_blink);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (hasCameraFlash) {
            blinkFlash();
        } else {
            Toast.makeText(this, "No Flash Available", Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(this::finish, 10 * 1000);
    }

    private void blinkFlash() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String myString = "0101010101010101010101010101010101010101010101";
        long blinkDelay = 100;
        for (int i = 0; i < myString.length(); i++) {
            if (myString.charAt(i) == '0') {
                try {
                    String cameraId = cameraManager.getCameraIdList()[0];
                    cameraManager.setTorchMode(cameraId, true);
                } catch (CameraAccessException e) {
                    Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    String cameraId = cameraManager.getCameraIdList()[0];
                    cameraManager.setTorchMode(cameraId, false);
                } catch (CameraAccessException e2) {
                    Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
            try {
                Thread.sleep(blinkDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        ExitActivity.exitApplicationAndRemoveFromRecent(FlashBlinkActivity.this);
        super.onDestroy();
    }
}
