package com.lma.recievers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.lma.R;
import com.lma.info.Info;
import com.lma.utils.SharedPrefUtils;

public class SmsReceiver extends BroadcastReceiver implements Info {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    String msg, phoneNo = "";
    MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TAG", "msg Received");
        Toast.makeText(context, "MESSAGE RECEIVED", Toast.LENGTH_SHORT).show();

        mediaPlayer = MediaPlayer.create(context, R.raw.all_might_i_am_here);
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle dataBundle = intent.getExtras();
            if (dataBundle != null) {
                Object[] mypdu = (Object[]) dataBundle.get("pdus");
                final SmsMessage[] message = new SmsMessage[mypdu.length];
                for (int i = 0; i < mypdu.length; i++) {
                    String format = dataBundle.getString("format");
                    message[i] = SmsMessage.createFromPdu((byte[]) mypdu[i], format);
                    msg = message[i].getMessageBody();
                    phoneNo = message[i].getOriginatingAddress();
                }

                String command = msg.toLowerCase();
                Toast.makeText(context, command, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onReceive: " + SharedPrefUtils.getStringSharedPrefs(context, KEY_CURRENT_DEVICE_IMEI));
                if (command.contains(COMMAND_RING) & command.contains(SharedPrefUtils.getStringSharedPrefs(context, KEY_CURRENT_DEVICE_IMEI))) {
                    Toast.makeText(context, "Starting", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(false);
                    mediaPlayer.start();
                    for (int i = 0; i < 20; i++) {
                        AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        if (mgr != null)
                            mgr.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                                    AudioManager.FLAG_SHOW_UI);

                    }
                    final int[] count = {0};
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        final int maxCount = 3;

                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if (count[0] < maxCount) {
                                count[0]++;
                                mediaPlayer.seekTo(0);
                                mediaPlayer.start();
                            }
                        }
                    });
                }
                if (command.contains(LOCATION_KEYWORD)) {
                    String[] str = command.split(",");
                    String lat = str[1];
                    String lng = str[2];
                    Log.i(TAG, "onReceive: LAT = " + lat);
                    Log.i(TAG, "onReceive: LNG = " + lng);
                    String strUri = "http://maps.google.com/maps?q=loc:" + lat + "," + lng + " (" + "Label which you want" + ")";
                    Intent intent2 = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

                    intent2.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(intent2);
                }

                if (!SharedPrefUtils.getBooleanSharedPrefs(context, KEY_TRACKING))
                    return;

                if (command.contains(COMMAND_MAP)
                        & command.contains(SharedPrefUtils.getStringSharedPrefs(context, KEY_CURRENT_DEVICE_IMEI))) {
                    Toast.makeText(context, "SENDING LOCATION", Toast.LENGTH_SHORT).show();

                    Log.i(TAG, "onReceive: OUTSIDE HANDLER init activity call");

                    Log.i(TAG, "onReceive: INSIDE HANDLE + init activity call");
                    Log.i(TAG, "onReceive: PACKAGE " + context.getApplicationContext().getPackageName());
                    Intent launchIntent = new Intent();
                    launchIntent.setComponent(new ComponentName
                            (context.getApplicationContext().getPackageName(),
                                    context.getApplicationContext().getPackageName() + ".activities.SendSmsActivity"));
                    launchIntent.putExtra("phoneNo", phoneNo);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);

                }

                if (command.contains(COMMAND_CALL)
                        & command.contains(SharedPrefUtils.getStringSharedPrefs(context, KEY_CURRENT_DEVICE_IMEI))) {
                    Toast.makeText(context, "Making Call", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(Intent.ACTION_CALL);
                    intent1.setData(Uri.parse("tel:" + phoneNo));
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent1);
                }


            }
        }
    }
}
