package com.example.babyc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;

public class DialogActivity extends AppCompatActivity {

    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogactivity);
        mp = MediaPlayer.create(this, R.raw.ringtone);
        mp.setLooping(true); //make sure it loops
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
                    break;
            }
        }
    };

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Baby Check");
        builder.setMessage("Is there a baby in the car?");
        builder.setPositiveButton("Keep running the service",listener);
        builder.setNegativeButton("I'm done, thanks",listener);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);

        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}