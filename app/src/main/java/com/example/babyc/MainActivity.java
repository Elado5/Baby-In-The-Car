package com.example.babyc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    //Getting api from goggle
    public GoogleApiClient mApiClient;
    //our activation button
    public ImageButton btn;
    int connected = 0;
    TextView StatusText; //Text above state box
    TextView CurrentState; //Text on screen that tells the user what he app recognizes they're doing.
    Button langBtn; //pop up language menu
    Button helpBtn; //pop up help menu
    Long curTime; //Save and update current time with each UI update.
    Long curTime2; //saving time for alert without updating it with each UI update.
    Long curTimeForDialog; //saving time to check if x time passed since alert dialog is on screen.

    Boolean firstUpdate = true; //first time we update the UI?
    Boolean dialogOnScreen = false; //Is there an alert dialog on the screen currently?
    Boolean firstDialogCheck = false; //bool for first time the dialog pops up.
    Boolean firstLocationCheck = false; //
    Boolean EnglishMode = false; //is English mode active?

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    //private static final int MY_PERMISSIONS_REQUEST_Location = 0;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    Location mylocation;

    String PhoneNum = "";

    MediaPlayer mp;
    Vibrator v;

    AlarmManager alarmManager;

    //alarm
    Boolean alarmSet = false;
    int alarmDelaySeconds = 0;

    //shared preferences context - static so we can use it in the ActivityRecognizedService class
    static private Context contextOfApplication;

    //alarm dialog listener
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            alarmSet = false; //reset it
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    dialogOnScreen = false;
                    mp.pause();
                    v.cancel();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    dialogOnScreen = false;
                    mp.pause();
                    v.cancel();
                    mApiClient.disconnect();
                    removeActivityUpdates();
                    break;
            }
        }
    };

    DialogInterface.OnClickListener listenerForOverlay = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            alarmSet = false; //reset it
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    //move to permission page
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 0);
                    break;
                case Dialog.BUTTON_NEGATIVE:
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Shared prefs
        contextOfApplication = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        //grab phone number from SP if it exists
            PhoneNum = prefs.getString("SMS_Number", "");

        //150 is value if it doesn't exist
        alarmDelaySeconds = prefs.getInt("alarmDelay", 150);


        helpBtn = (Button) findViewById(R.id.helper);

        //if we don't have language settings saved - default is English
        if (!(prefs.contains("English")) && !(prefs.contains("Hebrew"))) {
            Log.d("Shared pref", "defining");
            editor.putBoolean("English", true);
            editor.putBoolean("Hebrew", false);
            editor.apply();
            EnglishMode = true;
        }

        //English Version
        if (prefs.getBoolean("English", true)) {
            //functions can use this info later, like changing stop button to the english one
            EnglishMode = true;

            //media player for alarm
            mp = MediaPlayer.create(this, R.raw.ringtone);
            mp.setLooping(true); //make sure it loops
            curTime = System.currentTimeMillis();
            btn = (ImageButton) findViewById(R.id.btn);
            btn.setImageResource(R.drawable.startbtn);
            langBtn = (Button) findViewById(R.id.langButton);
            StatusText = (TextView) findViewById(R.id.StatusTop);
            CurrentState = (TextView) findViewById(R.id.CurrentState);
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            //can't draw app over other app? ask for permission via dialog
            if (!Settings.canDrawOverlays(this)) {
                overlayPermissionDialog();
            }

            //Goggle API client builder
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this)
                    .build();

            //setting click listener for start/stop button
            btn.setOnClickListener(this);

            //language button functionality
            langBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(MainActivity.this, langBtn);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            if (item.getItemId() == R.id.hebrew) {
                                Toast.makeText(MainActivity.this, "פותח מחדש ב : " + item.getTitle(), Toast.LENGTH_SHORT).show();

                                editor.putBoolean("Hebrew", true);
                                editor.putBoolean("English", false);
                                editor.apply();
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        triggerRebirth(contextOfApplication);
                                    }
                                }, 2000);
                            } else if (item.getItemId() == R.id.english) {
                                Toast.makeText(MainActivity.this, "Already in English mode", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                }
            });

        }

        //Hebrew Version
        else if (prefs.getBoolean("Hebrew", true)) {
            mp = MediaPlayer.create(this, R.raw.ringtone);
            mp.setLooping(true); //make sure it loops
            curTime = System.currentTimeMillis();
            btn = (ImageButton) findViewById(R.id.btn);
            btn.setImageResource(R.drawable.btnheb);
            langBtn = (Button) findViewById(R.id.langButton);
            StatusText = (TextView) findViewById(R.id.StatusTop);
            StatusText.setText("סטטוס נוכחי:");
            CurrentState = (TextView) findViewById(R.id.CurrentState);
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            //check if we can pop the app back to the screen from another app when the alarm goes off
            if (!Settings.canDrawOverlays(this)) {
                overlayPermissionDialog();
            }

            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this)
                    .build();

            btn.setOnClickListener(this);

            //'Language' menu button handling
            langBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(MainActivity.this, langBtn);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            if (item.getItemId() == R.id.hebrew) {
                                Toast.makeText(MainActivity.this, "מצב עברית כבר בשימוש", Toast.LENGTH_SHORT).show();
                            } else if (item.getItemId() == R.id.english) {
                                Toast.makeText(MainActivity.this, "Reopening in : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                                editor.putBoolean("Hebrew", false);
                                editor.putBoolean("English", true);
                                editor.apply();
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        triggerRebirth(contextOfApplication);
                                    }
                                }, 2000);
                            }
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                }
            });
        }

        //'Help' menu button handling
        helpBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, langBtn);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.helpmenu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.explanation) {
                            ViewDialog explanationAlert = new ViewDialog();
                            explanationAlert.showExDialog(MainActivity.this, "This app will" +
                                    " help never forget your baby in the car.\n\n" +
                                    "It uses Google's smart activity recognition service to" +
                                    " identify what you're doing every set amount of time.\nIf" +
                                    " you start driving and then stop for a few minutes (changeable)," +
                                    " it will trigger an alarm that will definitely get your" +
                                    " attention.\n" +
                                    "\nHave a nice drive!");
                        } else if (item.getItemId() == R.id.alarmdelay) {
                            Intent intent = new Intent(MainActivity.this, AlarmDelay.class);
                            startActivity(intent);
                        } else if (item.getItemId() == R.id.SMS_S) {
                            if (contextOfApplication.checkSelfPermission(Manifest.permission.SEND_SMS)
                                    !=
                                    PackageManager.PERMISSION_GRANTED) {
                                Log.d("SMS", "permission not granted yet");
                                // Permission not yet granted. Use requestPermissions().
                                // MY_PERMISSIONS_REQUEST_SEND_SMS is an
                                // app-defined int constant. The callback method gets the
                                // result of the request.
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.SEND_SMS},
                                        MY_PERMISSIONS_REQUEST_SEND_SMS);
                                Toast.makeText(contextOfApplication, "Please allow 'SEND SMS' permission in phone settings.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!checkLocationPermission()) {
                                    Log.d("SMS", "location permission not granted yet");
                                } else {
                                    // Permission already granted. Enable the SMS button.
                                    Intent intent = new Intent(MainActivity.this, SMS_Settings.class);
                                    startActivity(intent);
                                }
                            }
                        }
                        else if (item.getItemId() == R.id.RestartApp) {
                            Toast.makeText(contextOfApplication, "Restarting App in 2 seconds...", Toast.LENGTH_SHORT).show();

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    triggerRebirth(contextOfApplication);
                                }
                            }, 2000);
                        }
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        });

    }

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

    public void triggerRebirth(Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    // @RequiresApi(api = Build.VERSION_CODES.O) //for the dialog overlay type to work, it needs 26+ android instead of 23+
    private void showDialog() {
        //pop it back on screen
        Intent openMainActivity = new Intent(MainActivity.this, MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(openMainActivity, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Baby Check");
        if (EnglishMode) {
            builder.setMessage("Is there a baby in the car?");
            builder.setPositiveButton("Continue", listener);
            builder.setNegativeButton("Stop", listener);
        } else {
            builder.setMessage("האם יש תינוק ברכב?");
            builder.setPositiveButton("המשך לפעול", listener);
            builder.setNegativeButton("סיימתי, תודה", listener);
        }
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void overlayPermissionDialog() {
        //pop it back on screen

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Needed");
        builder.setMessage("Please give the app the 'overlay over other apps' permission for it to work properly.");
        builder.setPositiveButton("OK", listenerForOverlay);
        builder.setNegativeButton("Ask me next time", listenerForOverlay);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (activityRecognitionPermissionApproved()) {

            //Connect Broadcast
            if (!mIsReceiverRegistered) {
                if (mReceiver == null)
                    mReceiver = new MyBroadcastReceiver();
                registerReceiver(mReceiver, new IntentFilter("updateIntent"));
                mIsReceiverRegistered = true;
                Log.d("Receiver Status", "Registered");

            }
            //change button image
            if (EnglishMode) {
                btn.setImageResource(R.drawable.stopbtn);
            } else {
                btn.setImageResource(R.drawable.btn2heb);

            }
            Log.d("Connection", "connection good.");
            Intent intent = new Intent(MainActivity.this, ActivityRecognizedService.class);
            PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, 5000, pendingIntent);
            ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
            Log.d("Activity Recognition - ", "Started.");
            Task task = activityRecognitionClient.requestActivityUpdates(1000, pendingIntent);
            //Log.d("A",task.getResult().toString());
        } else {
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
        if (EnglishMode) {
            btn.setImageResource(R.drawable.startbtn);
        } else {
            btn.setImageResource(R.drawable.btnheb);
        }
        Intent intent = new Intent(MainActivity.this, ActivityRecognizedService.class);
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

        if (dialogOnScreen && !firstDialogCheck && !PhoneNum.equals("")) {
            firstDialogCheck = true;
            curTimeForDialog = System.currentTimeMillis();
        }

        //If the user doesn't respond to the dialog on the screen for x seconds and there's a user location saved.
        if (dialogOnScreen && !PhoneNum.equals("") && firstDialogCheck) {

            if(System.currentTimeMillis() > curTimeForDialog + 1000 * 120){
                Toast.makeText(contextOfApplication, "Sending SMS to contact", Toast.LENGTH_SHORT).show();

                new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendLocationSMS(mylocation);
                }
            }, 4500);
            //sendLocationSMS(locationManager.requestLocationUpdates(LocationManager.))
        }
        }

        //if 10 seconds passed from last UI update or we don't have a previous update
        if(System.currentTimeMillis() > (curTime + 10000) || firstUpdate) {

            if(firstUpdate){
                firstUpdate = false;
            }

            //change 'current status' string via extra string from broadcast update
            CurrentState.setText(intent.getStringExtra("activityUpdate"));

            //update current time variable
            curTime = System.currentTimeMillis();

            //debug version of alarm set: check if the state is driving (in this case - still, because it's for debug) and set alarm for 2 minutes after it
            /*if( (CurrentState.getText().toString().equals("Still") || (CurrentState.getText().toString().equals("דומם")))  && !alarmSet && !dialogOnScreen){
                curTime2 = System.currentTimeMillis();
                Log.d("Alarm", "Set");
                Toast.makeText(contextOfApplication, "Alarm is Set", Toast.LENGTH_SHORT).show();
                alarmSet = true;
            }*/

            //car version alarm set
            if((CurrentState.getText().toString().equals("In Vehicle")||CurrentState.getText().toString().equals("בנסיעה")) && !alarmSet && !dialogOnScreen){
                curTime2 = System.currentTimeMillis();
                Log.d("Alarm", "Set");
                alarmSet = true;
            }


            //car version - real version - using alarmDelaySeconds - activate only if state changed from ''in vehicle'' and 'x' time passed
            if(alarmSet && System.currentTimeMillis() > curTime2 + 1000 * alarmDelaySeconds && (!(CurrentState.getText().toString().equals("Current Activity:\n\nIn Vehicle") && !(CurrentState.getText().toString().equals("סטטוס נוכחי:\n\nבנסיעה")))) ){
                PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        ,  "BabyC: Wake Up Screen");

                final LocationListener locationListener = location -> {
                    mylocation = location;
                    Log.d("Location Changes", location.toString());
                };

                final Looper looper = null;
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


                //another permission check to make sure nothing will make the app collapse
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(contextOfApplication, "Location permission missing", Toast.LENGTH_SHORT).show();
                }
                //if it's good - get a single location update
                else {
                    locationManager.requestSingleUpdate(criteria, locationListener, looper);
                    firstLocationCheck = true;
                }

                mp.start();
                v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = { 0, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500};
                v.vibrate(pattern, 0);

                dialogOnScreen = true; //boolean to make sure we don't have stacking dialogs
                showDialog();
                alarmSet = false;
            }


            //activate alert dialog and save location if x seconds passed
            /*if(alarmSet && System.currentTimeMillis() > curTime2 + 1000 * 40){

                //lambda version of 'on location changed'
                final LocationListener locationListener = location -> {
                    mylocation = location;
                    Log.d("Location Changes", location.toString());
                };

                final Looper looper = null;
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


                //another permission check to make sure nothing will make the app collapse
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(contextOfApplication, "Location permission missing", Toast.LENGTH_SHORT).show();
                }
                //if it's good - get a single location update
                else {
                    locationManager.requestSingleUpdate(criteria, locationListener, looper);
                    firstLocationCheck = true;
                }

                mp.start();
                v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 500};
                v.vibrate(pattern, 5);
                dialogOnScreen = true; //boolean to make sure we don't have stacking dialogs
                showDialog();
                alarmSet = false;
            }
            */

        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("This function requires your location")
                        .setMessage("Please allow the location permission to continue")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
                Toast.makeText(contextOfApplication, "Restart the app for changes to take effect.", Toast.LENGTH_LONG).show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void sendLocationSMS(Location currentLocation) {
        if(!PhoneNum.equals("")) {
            String message = getResources().getString(R.string.location_message);
            SmsManager sms = SmsManager.getDefault();
            StringBuffer smsBody = new StringBuffer();
            smsBody.append(message);
            smsBody.append("\n");
            smsBody.append("http://maps.google.com?q=");
            smsBody.append(currentLocation.getLatitude());
            smsBody.append(",");
            smsBody.append(currentLocation.getLongitude());
            ArrayList<String> parts = sms.divideMessage(smsBody.toString());
            sms.sendMultipartTextMessage(PhoneNum, null, parts, null, null);
            //sms.sendMultipartTextMessage("15555215556", null, parts, null, null);
            Log.d(TAG, parts.toString());
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
                Toast.makeText(this, "Disconnecting!",
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
            Toast.makeText(this, "You need to give permissions to activate the service", Toast.LENGTH_SHORT).show();
        }
    }
}