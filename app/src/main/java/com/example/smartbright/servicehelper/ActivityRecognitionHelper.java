package com.example.smartbright.servicehelper;

import static com.example.smartbright.Definitions.DBG;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.example.smartbright.DetectedActivitiesIntentService;
import com.google.android.gms.location.ActivityRecognitionClient;

// todo activity logging
public class ActivityRecognitionHelper {
    private static final String TAG = ActivityRecognitionHelper.class.getSimpleName();

    private final Service service;

    private long lastTimeActivity = 0;
    private ActivityRecognitionClient mActivityRecognitionClient;

    public ActivityRecognitionHelper(Service service) {
        this.service = service;
        mActivityRecognitionClient = new ActivityRecognitionClient(service);
    }

    public void getActivities(){
        try {
            if (System.currentTimeMillis() - lastTimeActivity > 30000) {
                try {
                    mActivityRecognitionClient.requestActivityUpdates(
                            1000,
                            getActivityDetectionPendingIntent()
                    );
                    lastTimeActivity = System.currentTimeMillis();

                } catch (Exception e) {
                    if (DBG)
                        Log.d(TAG, "Exception in Activity Recognition in Service: " + e);
                }
            }
        }catch (Exception e){
            if (DBG) Log.d(TAG , "ERROR in activity recognition: " + e);
        }
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(service.getApplicationContext(), DetectedActivitiesIntentService.class);
        return PendingIntent.getService(service.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
