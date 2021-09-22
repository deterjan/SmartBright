package com.example.smartbright;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;


/**
 * Created by emir on 1/28/18.
 */

public class DetectedActivitiesIntentService extends IntentService {

    private static final boolean DGB = Definitions.DBG;
    private final static String TAG = "DetectedActivitiesIS";

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
        //   this.context=c;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Get the list of the probable activities associated with the current state of the
            // device. Each activity is associated with a confidence level, which is an int between
            // 0 and 100.
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            String mostActivity = getActivityString(getApplicationContext(), result.getMostProbableActivity().getType());

            // Log each activity.
            String act;
            if (DGB) Log.i(TAG, "activities detected");
            for (DetectedActivity da : detectedActivities) {

                act = getActivityString(getApplicationContext(), da.getType());
                int confCurAct = da.getConfidence();

                if (DGB) Log.i(TAG, act + " " + confCurAct + "%");
            }

            if (DGB) Log.i(TAG, "activities detected the most: " + mostActivity);
        }catch (Exception e){
            Log.d(TAG, "Error in Activity Recog: " + e);
        }

    }

    public String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "UNIDENTIFIABLE";
        }
    }
}
