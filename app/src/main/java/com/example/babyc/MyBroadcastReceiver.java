package com.example.babyc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyBroadcastReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){

        if (intent.getAction().matches("android.intent.action.BOOT_COMPLETED"))
            Log.d(getClass().getSimpleName(), "ActionBoot");
        else if
        (intent.getAction().matches("android.intent.action.TIME_TICK"))
            Log.d(getClass().getSimpleName(), "TimeTick");
        else if
        (intent.getAction().matches("android.intent.action.TIME_SET"))
            Log.d(getClass().getSimpleName(), "TimeSet");
        else if
        (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
            Log.d(getClass().getSimpleName(), "Action sms");
        else
            Log.d(getClass().getSimpleName(), "else");

        String msg = intent.getStringExtra("brd");
        if (msg != null) {
            Log.d(getClass().getSimpleName(), "My broadcast receiver message receive:" + msg);
        }
        else {
            Log.d(getClass().getSimpleName(), "My broadcast receiver message receive:null");

        }
    }

}
