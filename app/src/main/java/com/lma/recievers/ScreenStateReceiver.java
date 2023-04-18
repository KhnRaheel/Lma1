package com.lma.recievers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.lma.services.LockScreenManager;
import com.lma.services.ScreenLockerService;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenStateReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            LockScreenManager.screenOn = true;
            if (!LockScreenManager.timerRunning) {
                updateTimeOnEachSecond(context);
            }
        } else {
            LockScreenManager.screenOn = false;
            if (LockScreenManager.pattern != null) {
                if (!isMyServiceRunning(ScreenLockerService.class, context)) {
                    Log.w("BAM", "////////////////////Starting Screen locker Service");
                    Intent intent1 = new Intent(context, ScreenLockerService.class);
                    context.startService(intent1);
                } else {
                    Log.i("BAM", "////////////////////ScreenLockerService Already Running");
                }
            } else {
                Toast.makeText(context, "Pattern null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void updateTimeOnEachSecond(final Context context) {
        this.context = context;
        LockScreenManager.timer = new Timer();
        LockScreenManager.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LockScreenManager.timerRunning = true;
                Log.i("myapp", "Timer Running ------");
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                ScreenStateReceiver.this.context.sendBroadcast(closeDialog);
            }
        }, 0, 100);
    }
}
