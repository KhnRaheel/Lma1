package com.lma.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.lma.R;
import com.lma.activities.LockSettingsActivity;
import com.lma.activities.LoginActivity;
import com.lma.info.Info;
import com.lma.recievers.ScreenStateReceiver;
import com.lma.utils.SharedPrefUtils;

import java.util.Timer;

import static com.lma.App.CHANNEL_ID;

public class LockScreenManager extends Service implements Info {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT2 = "text2";
    public static final String IMAGE_URI = "imagePath";

    public static String pattern;
    public static Timer timer;
    public static boolean timerRunning = false;
    public static boolean screenOn = true;
    public static String activationKey;
    public static boolean screamOn = false;
    public static boolean flashOn = false;
    public static boolean locationOn = false;
    public static Drawable imagBack;
    public static String imageURI;
    String previousKey;
    String prevImageURI;
    BroadcastReceiver mScreenStateReceiver;
    Context context;
    int iteration = 0;
    PowerManager.WakeLock wakeLock;

    public LockScreenManager() {

    }

    public void initLocationSequence() {

    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "tag");
        wakeLock.acquire();

        loadData();

        if (LockSettingsActivity.imageURI != null) {
            LockScreenManager.imageURI = LockSettingsActivity.imageURI;
            LockScreenManager.imagBack = LockSettingsActivity.drawable;
        }

        if (previousKey != null) {
            LockScreenManager.activationKey = previousKey;
        }

        Log.i("Asd", "PASSWORD = " + activationKey);
        Log.i("Asd", "PATTERN = " + pattern);

        registerReceivers();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel();


        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        tm.listen(new PhoneStateListener() {
            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                Log.i(TAG, "onServiceStateChanged: ");
                if (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {
                    iteration = 0;
                    checkIfSimAvailable();
                }
            }

            @Override
            public void onDataConnectionStateChanged(int state) {

            }

        }, PhoneStateListener.LISTEN_SERVICE_STATE);


        LockScreenManager.screenOn = true;
        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ReachIt Security")
                .setContentText("ReachIt Services Running")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        return START_NOT_STICKY;

    }

    private void checkIfSimAvailable() {
        Log.i(TAG, "onServiceStateChanged: STATE OUT OF SERVICE");
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        int simState = tm.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_READY:
                Log.i(TAG, "onServiceStateChanged: Sim available, sending sms to emergency ");
                sendSmsToEmergency();
                break;
            case TelephonyManager.SIM_STATE_ABSENT:
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
            case TelephonyManager.SIM_STATE_UNKNOWN:
            default:
                iteration++;
                if (iteration < 100)
                    new Handler().postDelayed(this::checkIfSimAvailable, 3000);
        }
    }

    private void sendSmsToEmergency() {
        String contact = SharedPrefUtils.getStringSharedPrefs(context, KEY_EMERGENCY_CONTACT);
        sendSmsInSequence(0);
        Log.i(TAG, "onServiceStateChanged: SMS SENT TO EMERGENCY");
        Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName
                (context.getApplicationContext().getPackageName(),
                        context.getApplicationContext().getPackageName() + ".activities.SendSmsActivity"));
        launchIntent.putExtra("phoneNo", contact);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);

    }

    private void sendSmsInSequence(int iteration) {
        SmsManager smsManager = SmsManager.getDefault();
        String contact = SharedPrefUtils.getStringSharedPrefs(context, KEY_EMERGENCY_CONTACT);
        smsManager.sendTextMessage(contact, null, "Warning : Sim state of your phone has been changed", null, null);
        final int in = iteration;
        new Handler().postDelayed(() -> {
            if (in < 30)
                sendSmsInSequence(in + 1);
        }, 3000);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void registerReceivers() {
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenStateReceiver = new ScreenStateReceiver();
        registerReceiver(mScreenStateReceiver, screenStateFilter);
    }

    @Override
    public void onDestroy() {
        Log.i("laksjdl", "disabled On Destroy Called");
        unregisterReceiver(mScreenStateReceiver);
        saveData();
        stopSelf();
        if (wakeLock != null)
            wakeLock.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT2, LockScreenManager.activationKey);
        editor.putString(IMAGE_URI, LockScreenManager.imageURI);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        previousKey = sharedPreferences.getString(TEXT2, "reachit");
        prevImageURI = sharedPreferences.getString(IMAGE_URI, null);
    }
}
