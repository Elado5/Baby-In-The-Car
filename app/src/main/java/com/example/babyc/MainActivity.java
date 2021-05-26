package com.example.babyc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    //Getting api from goggle
    public GoogleApiClient mApiClient;
    //our activation button
    public ImageButton btn;
    int connected = 0;
    TextView CurrentState; //Text on screen that tells the user what he app recognizes they're doing.
    Long curTime; //Save and update current time with each UI update.
    Long curTime2; //saving time for alert without updating it with each UI update.
    Boolean curTime2Lock = false;

    Boolean firstUpdate = true; //first time we update the UI?
    Boolean dialogOnScreen = false; //Is there a dialog on the screen currently?
    MediaPlayer mp;

    AlarmManager alarmManager;
    //alarm
    Boolean alarmSet = false;

    //sharedpreferences
    SharedPreferences prefs;
    //SharedPreferences pref = context.getSharedPreferences(MY_PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);

    //dialog listener
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            alarmSet = false; //reset it
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    dialogOnScreen = false;
                    mp.pause();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    dialogOnScreen = false;
                    mp.pause();
                    mApiClient.disconnect();
                    removeActivityUpdates();
                    break;
            }
        }
    };

    //Inner broadcast receiver
    boolean mIsReceiverRegistered = false;
    MyBroadcastReceiver mReceiver = null;

    String TAG = "BabyC";
    // Review check for devices with Android 10 (29+).
    private boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    private boolean activityTrackingEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Shared prefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        //if we don't have language settings saved, which is the first instance
        if(!(prefs.contains("English")) && !(prefs.contains("Hebrew")))
        {
            editor.putBoolean("English", true);
            editor.putBoolean("Hebrew", false);
            editor.apply();
        }

        if(prefs.getBoolean("English", true))
        {
            mp = MediaPlayer.create(this, R.raw.ringtone);
            mp.setLooping(true); //make sure it loops
            curTime = System.currentTimeMillis();
            btn = (ImageButton) findViewById(R.id.btn);
            btn.setImageResource(R.drawable.startbtn);
            CurrentState = (TextView) findViewById(R.id.CurrentState);
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }

            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this)
                    .build();

            btn.setOnClickListener(this);
        }

        //change stuff here to hebrew
        else if (prefs.getBoolean("Hebrew", true))
        {
            mp = MediaPlayer.create(this, R.raw.ringtone);
            mp.setLooping(true); //make sure it loops
            curTime = System.currentTimeMillis();
            btn = (ImageButton) findViewById(R.id.btn);
            btn.setImageResource(R.drawable.startbtn);
            CurrentState = (TextView) findViewById(R.id.CurrentState);
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }

            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this)
                    .build();

            btn.setOnClickListener(this);
        }
    }

   // @RequiresApi(api = Build.VERSION_CODES.O) //for the dialog overlay type to work, it needs 26+ android instead of 23+
    private void showDialog(){

        //pop it back on screen
        Intent openMainActivity = new Intent(MainActivity.this, MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //openMainActivity.setFlags(Intent.FLAG_ACTIVITY_C)
        startActivityIfNeeded(openMainActivity, 0);
        /*Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(startIntent);
        finish();
        */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Baby Check");
        builder.setMessage("Is there a baby in the car?");
        builder.setPositiveButton("Keep running the service",listener);
        builder.setNegativeButton("I'm done, thanks",listener);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);

        /*notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        */
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (activityRecognitionPermissionApproved()){

            //Connect Broadcast
            if (!mIsReceiverRegistered) {
                if (mReceiver == null)
                    mReceiver = new MyBroadcastReceiver();
                registerReceiver(mReceiver, new IntentFilter("updateIntent"));
                mIsReceiverRegistered = true;
                Log.d("Receiver Status", "Registered");

            }
            //change button image
            btn.setImageResource(R.drawable.stopbtn);

            Log.d("Connection", "connection good.");
            Intent intent = new Intent (MainActivity.this, ActivityRecognizedService.class);
            PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, 5000, pendingIntent);
            ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
            Log.d("Activity Recognition - ", "Started.");
            Task task = activityRecognitionClient.requestActivityUpdates(1000, pendingIntent);
            //Log.d("A",task.getResult().toString());
        }
        else{
            Log.d("Permission Status - ", "Not approved");
        }
    }


         public void removeActivityUpdates() {

             if (mIsReceiverRegistered) {
                 unregisterReceiver(mReceiver);
                 mReceiver = null;
                 mIsReceiverRegistered = false;
             }
             //reset button
             btn.setImageResource(R.drawable.startbtn);

             Intent intent = new Intent (MainActivity.this, ActivityRecognizedService.class);
             PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
             ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
             activityRecognitionClient.removeActivityUpdates(pendingIntent);
             Log.d("Activity Recognition", "Stopped.");
             CurrentState.setText("-");

         }


    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void updateUI(Intent intent) {
        Log.d("updateUI", "got broadcast");
        Log.d("curTime comparison", curTime + "");
        //if 20 seconds passed from last UI update
        if(System.currentTimeMillis() > (curTime + 20000) || firstUpdate) {

            if(firstUpdate){
                firstUpdate = false;
            }

            CurrentState.setText(intent.getStringExtra("activityUpdate"));
            curTime = System.currentTimeMillis();

            //check if the state is driving and set alarm for 2 minutes after it
            if(CurrentState.getText().toString().equals("Current Activity:\n\nStill") && !alarmSet && !dialogOnScreen){
                    curTime2 = System.currentTimeMillis();
                /*
                Intent myIntent = new Intent(this, MainActivity.class);
                myIntent.putExtra("show_dialog","showDialog");

                PendingIntent mPi = PendingIntent.getActivity(this, 0,myIntent, this
                        .getIntent().getFlags());
                */
                /*alarmManager.set(AlarmManager.RTC_WAKEUP,
                        curTime + twoMinutesInMillis,
                        mPi);
                */
                Log.d("Alarm", "Set");
                alarmSet = true;
            }

            //activate alert dialog if x time passed
            if(alarmSet && System.currentTimeMillis() > curTime2 + 1000 * 40){
                PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        ,  "BabyC: Wake Up Screen");

                wakeLock.acquire(10*60*100L /*30 seconds*/);
                mp.start();
                dialogOnScreen = true; //the dialog will start in the next line so we don't want multiple dialogs to stack.
                /*if (super.isDestroyed()){
                }*/
                showDialog();
                alarmSet = false;
            }

            //car version
            /*if(CurrentState.getText().toString().equals("Current Activity:\n\nIn Vehicle") && !alarmSet && !dialogOnScreen){
                curTime2 = System.currentTimeMillis();
                Log.d("Alarm", "Set");
                alarmSet = true;
            }


            //car version - real version - 3 minutes (180 seconds) - activate only if state changed from ''in vehicle''
            if(alarmSet && System.currentTimeMillis() > curTime2 + 1000 * 180 && !(CurrentState.getText().toString().equals("Current Activity:\n\nIn Vehicle")) ){
                PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        ,  "BabyC: Wake Up Screen");

                wakeLock.acquire(10*60*100L ); //30 seconds screen wake force
                mp.start(); //start playing
                dialogOnScreen = true; //the dialog will start in the next line so we don't want multiple dialogs to stack.
                showDialog();
                alarmSet = false;
            }

             */

        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Suspension", "connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Failure", "connection failed.");
    }

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    private boolean activityRecognitionPermissionApproved() {

        // TODO: Review permission check for 29+.
        if (runningQOrLater) {

            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            );
        } else {
            return true;
        }
    }


    public boolean onClickEnableOrDisableActivityRecognition(View view) {

        // TODO: Enable/Disable activity tracking and ask for permissions if needed.
        if (activityRecognitionPermissionApproved()) {

            Log.d("Permissions status", "GIVEN!");
            return true;

        } else {
            // Request permission and start activity for result. If the permission is approved, we
            // want to make sure we start activity recognition tracking.
            Intent startIntent = new Intent(this, PermissionRationalActivity.class);
            startActivityForResult(startIntent, 0);
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        if (onClickEnableOrDisableActivityRecognition(v)) {
            if (mApiClient.isConnected()) {
                Toast.makeText(this, "Connected Already - Disconnecting!",
                        Toast.LENGTH_SHORT).show();
                removeActivityUpdates();
                mApiClient.disconnect();


            } else {
                Toast.makeText(this, "Connecting!",
                        Toast.LENGTH_SHORT).show();
                if (connected == 0) {
                    mApiClient.connect();
                    connected = 1;
                } else {
                    mApiClient.reconnect();
                }
            }
        }

        else{
            Toast.makeText(this, "You need to give permissiosns to activate the service", Toast.LENGTH_SHORT).show();
        }
    }
}