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

    //builder
    public  ActivityRecognizedService(){
        super("ActivityRecognizedService");
    }
    public  ActivityRecognizedService(String name){ //another constructor
        super(name);
    }

    //Shared prefs -> taking context from main activty static context so we can use it
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

                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","In Vehicle");
                        sendBroadcast(intent);
                    }


                    break;
                }
                case DetectedActivity.ON_BICYCLE:{
                    Log.d(TAG, "handDetectedActivity: ON_BICYCLE" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","On Bike");
                        sendBroadcast(intent);
                    }
                }break;

                case DetectedActivity.RUNNING:{
                    Log.d(TAG, "handDetectedActivity: RUNNING" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Running");
                        sendBroadcast(intent);
                    }
                }break;
                case DetectedActivity.STILL:{
                    Log.d(TAG, "handDetectedActivity: STILL" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Still");
                        sendBroadcast(intent);
                    }
                }break;
                case DetectedActivity.WALKING:{
                    Log.d(TAG, "handleDetectedActivity: WALKING" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","Walking");
                        sendBroadcast(intent);

                    }
                }break;
                case DetectedActivity.ON_FOOT:{
                    Log.d(TAG, "handDetectedActivity: ON_FOOT" + activity.getConfidence());
                    if(activity.getConfidence() >= 75){
                        Intent intent = new Intent("updateIntent");
                        intent.putExtra("activityUpdate","On Foot");
                        sendBroadcast(intent);
                    }
                }break;
                case DetectedActivity.UNKNOWN:
                {
                    Log.d(TAG, "handleDetectedActivity: UNKNOWN - " + activity.getConfidence());
                    Intent intent = new Intent("updateIntent");
                    if(activity.getConfidence() == 40) {
                        intent.putExtra("activityUpdate", "Unrecognized Activity");
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
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות נוכחית:\n\nברכב");
                            sendBroadcast(intent);
                        }


                        break;
                    }
                    case DetectedActivity.ON_BICYCLE:{
                        Log.d(TAG, "handDetectedActivity: ON_BICYCLE" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות נוכחית:\n\nאופניים");
                            sendBroadcast(intent);
                        }
                    }break;
                    case DetectedActivity.WALKING:{
                        Log.d(TAG, "handleDetectedActivity: WALKING" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","הליכה");
                            sendBroadcast(intent);

                        }
                    }break;
                    case DetectedActivity.RUNNING:{
                        Log.d(TAG, "handDetectedActivity: RUNNING" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","ריצה");
                            sendBroadcast(intent);
                        }
                    }break;
                    case DetectedActivity.ON_FOOT:{
                        Log.d(TAG, "handDetectedActivity: ON_FOOT" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","פעילות רגלית");
                            sendBroadcast(intent);
                        }
                    }break;

                    case DetectedActivity.STILL:{
                        Log.d(TAG, "handDetectedActivity: STILL" + activity.getConfidence());
                        if(activity.getConfidence() >= 75){
                            Intent intent = new Intent("updateIntent");
                            intent.putExtra("activityUpdate","דומם");
                            sendBroadcast(intent);
                        }
                    }break;

                    case DetectedActivity.UNKNOWN:
                    {
                        Log.d(TAG, "handleDetectedActivity: UNKNOWN - " + activity.getConfidence());
                        Intent intent = new Intent("updateIntent");
                        if(activity.getConfidence() == 40) {
                            intent.putExtra("activityUpdate", "פעילות לא מזוהה");
                            sendBroadcast(intent);
                        }


                    }break;

                }
            }
        }
    }

}




