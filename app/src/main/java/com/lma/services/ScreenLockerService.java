package com.lma.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.lma.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScreenLockerService extends Service implements PatternLockViewListener {

    public static int accessState = 0;

    float translationY = 0;

    View mView;
    PatternLockView patternLockView;
    TextView tvBottom;
    WindowManager wm;
    ImageView imageView;
    ImageView imageView1;
    Button btnCancel;
    TextView textView2;
    TextView tvDate;
    TextView tvUnlock;

    DigitalClock digitalClock;
    WindowManager.LayoutParams params;
    int animationDelay = 700;
    private int i = 1;
    boolean clickRepeat = false;

    public ScreenLockerService() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onCreate();
        if (LockScreenManager.pattern == null){
            stopSelf();
        }

        if (mView == null) {
            wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            mView = LayoutInflater.from(this).inflate(R.layout.service_layout, null, false);
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 0;
            params.y = 0;
            params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN;


            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            mView.setSystemUiVisibility(uiOptions);

            castElements();
            if (LockScreenManager.imagBack != null){
                imageView.setImageDrawable(LockScreenManager.imagBack);
            } else {
                imageView.setImageDrawable(getDrawable(R.drawable.lockscreen_background));
            }
            params.format = PixelFormat.TRANSLUCENT;
            initiateAnimations();
            patternLockView.addPatternLockListener(this);

            setDate(tvDate);
            patternLockView.setEnabled(false);
            patternLockView.setAlpha(0);
            tvUnlock.setAlpha(0);

            final int[] initX = new int[1];
            final int[] initY = new int[1];
            imageView.setOnTouchListener((v, event) -> {
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (i == 1) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.i("TAG", "touched down");
                            initX[0] = x;
                            initY[0] = y;
                            tvBottom.animate().alpha(0).setDuration(200);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            actionMove(x, y, initX, initY);
                            Log.i("TAG", "moving: (" + (x - initX[0]) + ", " + (y - initY[0]) + ")");
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.i("TAG", "touched up");
                            if ((x - initX[0]) > 500 || y - initY[0] > 500 || (x - initX[0]) < -500 || y - initY[0] < -500) {
                                //SHOW PATTERN
                                swipe();
                                digitalClock.animate().alpha(0).setDuration(200);
                                textView2.animate().alpha(0).setDuration(200);
                                tvDate.animate().alpha(0).setDuration(200);

                                i++;
                            } else {
                                doneAnimation();
                            }
                            break;
                    }
                }
                return true;
            });
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelPress();
                }
            });
            wm.addView(mView, params);
        } else {
            Log.i("sada", "////////// mView is not null");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void cancelPress() {
        if (!clickRepeat){
            clickRepeat = true;
            i = 1;
            patternLockView.setScaleX(1.5f);
            patternLockView.setScaleY(1.5f);
            patternLockView.setAlpha(0);
            tvUnlock.setScaleX(1.5f);
            tvUnlock.setScaleY(1.5f);
            tvUnlock.setAlpha(0);
            btnCancel.setAlpha(0);
            btnCancel.setScaleX(1.5f);
            btnCancel.setScaleY(1.5f);
            patternLockView.setEnabled(false);
            btnCancel.setEnabled(false);
            textView2.setScaleX(1.5f);
            textView2.setScaleY(1.5f);
            textView2.setAlpha(0);
            tvDate.setScaleX(1.5f);
            tvDate.setScaleY(1.5f);
            tvDate.setAlpha(0);

            doneAnimation();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    clickRepeat = false;
                }
            }, 900);
        }
    }

    private void castElements() {
        imageView = mView.findViewById(R.id.img_back);
        imageView1 = mView.findViewById(R.id.img_back2);
        patternLockView = mView.findViewById(R.id.pattern_lock_view);
        tvUnlock = mView.findViewById(R.id.tv_unlock);
        textView2 = mView.findViewById(R.id.tv_note1);
        tvDate = mView.findViewById(R.id.tv_date);
        digitalClock = mView.findViewById(R.id.clock);
        btnCancel = mView.findViewById(R.id.btn_Cancel);
        tvBottom = mView.findViewById(R.id.tv_bottom);
    }

    private void swipe() {
        Log.i("asdasd", "swipe");
        imageView1.animate().alpha(1).setDuration(500);
        patternLockView.animate().scaleX(1).scaleY(1).alpha(1).setDuration(500);
        tvUnlock.animate().scaleX(1).scaleY(1).alpha(1).setDuration(500);
        btnCancel.animate().scaleX(1).scaleY(1).alpha(1).setDuration(500);
        patternLockView.setEnabled(true);
        btnCancel.setEnabled(true);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onProgress(List<PatternLockView.Dot> progressPattern) {

    }

    @Override
    public void onComplete(List<PatternLockView.Dot> pattern) {
        String ptrn = PatternLockUtils.patternToString(patternLockView, pattern);
        if (ptrn.equalsIgnoreCase(LockScreenManager.pattern)) {
            patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
            ScreenLockerService.accessState = 1;
            final Intent intent1 = new Intent("finish_activity");
            sendBroadcast(intent1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            }, 700);

        } else {
            patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
            ScreenLockerService.accessState = 2;

        }
    }

    private void disappearAnimation() {
        disappearView(digitalClock);
        disappearView(textView2);
        disappearView(tvDate);
        disappearView(patternLockView);
        disappearView(tvUnlock);
        disappearView(tvBottom);
        disappearView(imageView1);
        disappearView(imageView);
        disappearView(btnCancel);
    }
    private void disappearView(View view){
        view.animate().alpha(0).setDuration(animationDelay);
    }

    private void initiateAnimations() {
        digitalClock.animate().alpha(1).setDuration(500);
        imageView.animate().alpha(1).setDuration(500);

    }

    @Override
    public void onCleared() {

    }

    @Override
    public void onDestroy() {
        Log.i("asdkjlk", "LockScreen is destroyed");
        disappearAnimation();
        if (LockScreenManager.timerRunning) {
            LockScreenManager.timer.cancel();
            LockScreenManager.timerRunning = false;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wm.removeView(mView);
            }
        }, animationDelay);
        super.onDestroy();
    }

    private void doneAnimation() {
        digitalClock.animate().alpha(1).setDuration(500);
        digitalClock.animate().scaleX(1).setDuration(500);
        digitalClock.animate().scaleY(1).setDuration(500);
        tvBottom.animate().alpha(1).setDuration(200);
        digitalClock.animate().translationYBy(translationY).setDuration(500);
        textView2.animate().alpha(1).setDuration(500);
        textView2.animate().scaleX(1).setDuration(500);
        textView2.animate().scaleY(1).setDuration(500);
        textView2.animate().translationYBy(translationY).setDuration(500);
        tvDate.animate().alpha(1).setDuration(500);
        tvDate.animate().scaleX(1).setDuration(500);
        tvDate.animate().scaleY(1).setDuration(500);
        tvDate.animate().translationYBy(translationY).setDuration(500);
        imageView1.animate().alpha(0).setDuration(500);
        translationY = 0;
    }

    private void actionMove(int x, int y, int[] initX, int[] initY) {
        int xNew = x - initX[0];
        int yNew = y - initY[0];
        if (xNew < 0) {
            xNew = -1 * xNew;
        }
        if (yNew < 0) {
            yNew = -1 * yNew;
        }
        float appearCal = (float) ((xNew + yNew));
        float disAppear = (float) (appearCal * 0.00125);
        float appear = 1 - disAppear;
        Log.i("TAG", "Appear : " + appear + " \nDisappear : " + disAppear);
        digitalClock.setAlpha(appear);
        textView2.setAlpha(appear);
        tvDate.setAlpha(appear);

        float viewScale = (float) (disAppear * 0.2 + 1);
        float viewTranslation = (float) (disAppear * 1.5 * - 150);

        translationY = - viewTranslation;
        digitalClock.setTranslationY(viewTranslation);
        textView2.setTranslationY(viewTranslation);
        tvDate.setTranslationY(viewTranslation);
        digitalClock.setScaleX(viewScale);
        digitalClock.setScaleY(viewScale);
        textView2.setScaleX(viewScale);
        textView2.setScaleY(viewScale);
        tvDate.setScaleX(viewScale);
        tvDate.setScaleY(viewScale);
    }
    public void setDate (TextView view){
        Date today = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");//formating according to my need
        String date = formatter.format(today);
        view.setText(date);
    }

}

