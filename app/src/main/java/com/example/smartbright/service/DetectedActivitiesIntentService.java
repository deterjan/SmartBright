package com.example.smartbright.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import static com.example.smartbright.Definitions.DBG;

import com.example.smartbright.dataprovider.ActivityRecognitionProvider;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;


/**
 * Created by emir on 1/28/18.
 */

public class DetectedActivitiesIntentService extends IntentService {
    private static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    private static ActivityRecognitionProvider provider;

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
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

            String lastActivity = getActivityString(result.getMostProbableActivity().getType());
            int lastConfidence = result.getMostProbableActivity().getConfidence();

            provider.setActivity(lastActivity);
            provider.setConfidence(lastConfidence);

            // Log each activity.
            if (DBG) {
                String act;
                for (DetectedActivity da : detectedActivities) {
                    act = getActivityString(da.getType());
                    int confCurAct = da.getConfidence();
                    Log.i(TAG, act + " " + confCurAct + "%");
                }
            }

            if (DBG) Log.i(TAG, "activities detected the most: " + lastActivity);
        } catch (Exception e) {
            Log.d(TAG, "Error in Activity Recognition: " + e);
        }

    }

    public String getActivityString(int detectedActivityType) {
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
    public static void setProvider(ActivityRecognitionProvider p) {
        provider = p;
    }
}
