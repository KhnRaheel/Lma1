package com.lma.recievers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MessageSentListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int resultCode = this.getResultCode();
        boolean successfullySent = resultCode == Activity.RESULT_OK;
        if (successfullySent){
            Toast.makeText(context, "Sms Sent Successfully", Toast.LENGTH_SHORT).show();
        }
        context.unregisterReceiver(this);
    }
}
