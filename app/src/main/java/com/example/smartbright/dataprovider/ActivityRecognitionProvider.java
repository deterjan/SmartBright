package com.example.smartbright.dataprovider;

import static com.example.smartbright.Definitions.DBG;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.example.smartbright.service.DetectedActivitiesIntentService;
import com.google.android.gms.location.ActivityRecognitionClient;

import java.util.HashMap;
import java.util.Map;

public class ActivityRecognitionProvider implements DataProvider {
    private static final String TAG = ActivityRecognitionProvider.class.getSimpleName();

    public static final int MIN_PERIOD_MS = 30000;

    private final Service service;
    private final ActivityRecognitionClient mActivityRecognitionClient;

    private long lastActivityDetectionTime = 0;
    private String activity;
    private int confidence;

    public ActivityRecognitionProvider(Service service) {
        this.service = service;
        mActivityRecognitionClient = new ActivityRecognitionClient(service);
        DetectedActivitiesIntentService.setProvider(this);
    }

    public void detectActivities(){
        try {
            if (System.currentTimeMillis() - lastActivityDetectionTime > MIN_PERIOD_MS) {
                if (DBG) Log.d(TAG, "time elapsed " + (System.currentTimeMillis() - lastActivityDetectionTime));
                try {
                    mActivityRecognitionClient.requestActivityUpdates(
                            1000,
                            getActivityDetectionPendingIntent()
                    );
                    lastActivityDetectionTime = System.currentTimeMillis();

                } catch (Exception e) {
                    if (DBG) Log.d(TAG, "Exception in Activity Recognition in Service: " + e);
                }
            }
        }catch (Exception e){
            if (DBG) Log.d(TAG , "ERROR in activity recognition: " + e);
        }
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(service.getApplicationContext(), DetectedActivitiesIntentService.class);
        return PendingIntent.getService(service.getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @Override
    public Map<String, Object> getData() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("activity", activity);
        map.put("activityConfidence", confidence);

        return map;
    }
}
