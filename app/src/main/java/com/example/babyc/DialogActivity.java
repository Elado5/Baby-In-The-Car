package com.example.babyc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

public class DialogActivity extends AppCompatActivity {

    MediaPlayer mp;
    MyBroadcastReceiver receiver;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogactivity);
        mp = MediaPlayer.create(this, R.raw.ringtone);
        mp.setLooping(true); //make sure it loops

        receiver = new MyBroadcastReceiver();
    }

    //dialog
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    mp.pause();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    mp.pause();
                    //MainActivity.removeActivityUpdates();
                    registerReceiver(receiver, new IntentFilter());
                   // mIsReceiverRegistered = true;
                    Log.d("Receiver Status", "Registered");
                    break;
            }
        }
    };

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Baby Check");
        builder.setMessage("Is there a baby in the car?");
        builder.setPositiveButton("Keep running the service",listener);
        builder.setNegativeButton("I'm done, thanks",listener);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}