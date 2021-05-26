package com.example.babyc;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;


@SuppressWarnings("ALL")
public class ActivityRecognizedService extends IntentService {

    //class d.Log tag
    private static final String TAG = "ActivityRecService";

    // when driving detected -> true
    boolean inCar = false;
    //builder
    public  ActivityRecognizedService(){
        super("ActivityRecognizedService");
    }
    public  ActivityRecognizedService(String name){ //another constructor
        super(name);
    }

    //Shared prefs -> taking context from mainactivty static context so we can use it
    Context applicationContext =  MainActivity.getContextOfApplication();
    SharedPreferences prefss = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    SharedPreferences.Editor editor = prefss.edit();

    //when there's a result from the intent -> activate the 'handleDetectedActivity' function on it
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)){
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivity(result.getProbableActivities());
        }
    }


    private void handleDetectedActivity(List<DetectedActivity> probableActivities){
        //english version
        if(prefss.getBoolean("English", true))
        {
        for (DetectedActivity activity: probableActivities){
            switch(activity.getType()){
                case DetectedActivity.IN_VEHICLE:{
                    Log.d(TAG, "handDetectedActivity: IN_VEHICLE" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        //MainActivity.CurrentState.setText("Driving - In Car");
                        //return "Driving - In Car";
                        if (!inCar) { inCar = true; }
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Current Activity:\n\nIn Vehicle");
                        sendBroadcast(intent);
                    }


                    break;
                }
                case DetectedActivity.ON_BICYCLE:{
                    Log.d(TAG, "handDetectedActivity: ON_BICYCLE" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        if (inCar) { inCar = false; }
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Current Activity:\n\nOn Bike");
                        sendBroadcast(intent);
                    }
                }break;
                case DetectedActivity.ON_FOOT:{
                    Log.d(TAG, "handDetectedActivity: ON_FOOT" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        if (inCar) { inCar = false; }
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Current Activity:\n\nOn Foot");
                        sendBroadcast(intent);
                    }
                }break;
                case DetectedActivity.RUNNING:{
                    Log.d(TAG, "handDetectedActivity: RUNNING" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        if (inCar) { inCar = false; }
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Current Activity:\n\nRunning");
                        sendBroadcast(intent);
                    }
                }break;
                case DetectedActivity.STILL:{
                    Log.d(TAG, "handDetectedActivity: STILL" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        if (inCar) { inCar = false; }
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Current Activity:\n\nStill");
                        sendBroadcast(intent);
                    }
                }break;
                case DetectedActivity.WALKING:{
                    Log.d(TAG, "handleDetectedActivity: WALKING" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        if (inCar) { inCar = false; }
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Current Activity:\n\nWalking");
                        sendBroadcast(intent);

                    }
                }break;
                case DetectedActivity.UNKNOWN:
                {
                    Log.d(TAG, "handleDetectedActivity: UNKNOWN - " + activity.getConfidence());
                    Intent intent = new Intent("updateIntent");
                    if(activity.getConfidence() == 40) {
                        if (inCar) { inCar = false; }
                        intent.putExtra("activityUpdate", "Current Activity:\n\nUnrecognized");
                        sendBroadcast(intent);
                    }


                }break;

            }
        }
    }
        //Hebrew version
        else if (prefss.getBoolean("Hebrew", true))
        {
            for (DetectedActivity activity: probableActivities){
                switch(activity.getType()){
                    case DetectedActivity.IN_VEHICLE:{
                        Log.d(TAG, "handDetectedActivity: IN_VEHICLE" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            //MainActivity.CurrentState.setText("Driving - In Car");
                            //return "Driving - In Car";
                            if (!inCar) { inCar = true; }
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות נוכחית:\n\nברכב");
                            sendBroadcast(intent);
                        }


                        break;
                    }
                    case DetectedActivity.ON_BICYCLE:{
                        Log.d(TAG, "handDetectedActivity: ON_BICYCLE" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            if (inCar) { inCar = false; }
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות נוכחית:\n\nאופניים");
                            sendBroadcast(intent);
                        }
                    }break;
                    case DetectedActivity.ON_FOOT:{
                        Log.d(TAG, "handDetectedActivity: ON_FOOT" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            if (inCar) { inCar = false; }
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות נוכחית:\n\nרגלית");
                            sendBroadcast(intent);
                        }
                    }break;
                    case DetectedActivity.RUNNING:{
                        Log.d(TAG, "handDetectedActivity: RUNNING" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            if (inCar) { inCar = false; }
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות נוכחית:\n\nריצה");
                            sendBroadcast(intent);
                        }
                    }break;
                    case DetectedActivity.STILL:{
                        Log.d(TAG, "handDetectedActivity: STILL" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            if (inCar) { inCar = false; }
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות נוכחית:\n\nדומם");
                            sendBroadcast(intent);
                        }
                    }break;
                    case DetectedActivity.WALKING:{
                        Log.d(TAG, "handleDetectedActivity: WALKING" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            if (inCar) { inCar = false; }
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות נוכחית:\n\nהליכה");
                            sendBroadcast(intent);

                        }
                    }break;
                    case DetectedActivity.UNKNOWN:
                    {
                        Log.d(TAG, "handleDetectedActivity: UNKNOWN - " + activity.getConfidence());
                        Intent intent = new Intent("updateIntent");
                        if(activity.getConfidence() == 40) {
                            if (inCar) { inCar = false; }
                            intent.putExtra("activityUpdate", "פעילות נוכחית:\n\nלא מזוהה");
                            sendBroadcast(intent);
                        }


                    }break;

                }
            }
        }
    }

}




