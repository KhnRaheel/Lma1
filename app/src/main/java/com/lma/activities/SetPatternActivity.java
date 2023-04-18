package com.lma.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.lma.R;
import com.lma.services.LockScreenManager;

import java.util.List;

public class SetPatternActivity extends AppCompatActivity implements PatternLockViewListener {
    PatternLockView patternLockView;
    TextView textView;
    String temp;
    String temp1;
    boolean check;
    Button button;
    Button button_Reset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pattern);

        Window window = this.getWindow();
        button = findViewById(R.id.btn);
        button_Reset = findViewById(R.id.btn_reset);
        button.setAlpha(0);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_Transparent));
        patternLockView = findViewById(R.id.pattern_set_lock_view);
        patternLockView.addPatternLockListener(this);
        textView = findViewById(R.id.text_instruct);
        textView.setText("Draw Previous Pattern");
        if (LockScreenManager.pattern == null) {
            textView.setText("Draw New Pattern");
        }
        button_Reset.setOnClickListener(v -> {
            temp = null;
            textView.setText("Draw New Pattern");
            patternLockView.clearPattern();
            patternLockView.setEnabled(true);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
        if (check) {
            setNewPattern(ptrn);
        }
        if (ptrn.equals(LockScreenManager.pattern)) {
            if (temp1 == null) {
                temp1 = ptrn;
                check = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Draw New Pattern");
                        patternLockView.clearPattern();
                    }
                }, 500);
            }
        } else if (LockScreenManager.pattern == null) {
            setNewPattern(ptrn);
        } else {
            if (temp1 == null) {
                check = false;
                patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                textView.setText("Pattern Wrong try again");
            }
        }

    }

    private void setNewPattern(final String ptrn) {
        if (temp == null) {
            button.setAlpha(1);
            button_Reset.setAlpha(1);
            button_Reset.setEnabled(true);
            button.setEnabled(true);
            button.setText("NEXT");
            patternLockView.setEnabled(false);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ptrn.length() < 4) {
                        textView.setText("You need to connect at least 4 dots. Try again");
                        textView.setTextSize(15);
                        patternLockView.clearPattern();
                        patternLockView.setEnabled(true);
                    } else {
                        temp = ptrn;
                        textView.setText("Confirm Pattern");
                        textView.setTextSize(25);
                        patternLockView.clearPattern();
                        patternLockView.setEnabled(true);
                    }
                }
            });

        } else if (temp.equals(ptrn)) {
            patternLockView.setEnabled(false);
            textView.setText("Pattern Matched");
            button.setText("Confirm");
            button.setOnClickListener(v -> {
                LockScreenManager.pattern = ptrn;
                patternLockView.setEnabled(true);
                Toast.makeText(SetPatternActivity.this, "Pattern Updated", Toast.LENGTH_SHORT).show();
                finish();
            });

        } else {
            textView.setText("Pattern Wrong try again");
            patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
        }
    }


    @Override
    public void onCleared() {

    }

}
