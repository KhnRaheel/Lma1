package com.lma.recievers;

import android.telephony.PhoneStateListener;
import android.util.Log;

public class SimStateReceiver extends PhoneStateListener {


    public void onCallStateChanged(int state, String incomingNumber) {
        Log.i("TAG", "onCallStateChanged: " + state);
        Log.i("TAG", "onCallStateChanged: " + incomingNumber);
    }

}