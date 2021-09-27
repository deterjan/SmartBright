package com.example.smartbright.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartbright.DataCollectionManager;
import com.example.smartbright.Definitions;
import com.example.smartbright.MainActivity;
import com.example.smartbright.R;
import com.example.smartbright.UniqueIDManager;
import com.example.smartbright.dataprovider.ForegroundAppProvider;
import com.example.smartbright.dataprovider.PowerReadingProvider;
import com.example.smartbright.dataprovider.SensorProvider;
import com.example.smartbright.logger.Logger;
import com.example.smartbright.logger.LoggerCSV;
import com.example.smartbright.dataprovider.ActivityRecognitionProvider;
import com.example.smartbright.dataprovider.LocationProvider;
import com.example.smartbright.dataprovider.UserSatisfactionProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.smartbright.Definitions.DBG;

// TODO ondestroy

@TargetApi(Build.VERSION_CODES.R)
public class ServiceClassPhone extends Service {

    private static final String TAG = ServiceClassPhone.class.getSimpleName();
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private Logger logger; // logger object
    private Map<String, String> sensorsValues; // sensor values map for logging

    private final IBinder mBinder = new LocalBinder();
    private ContentResolver contentResolver;

    // whether we are asking server for predicted brightness
    private int lastBrightness; // keep last brightness to fix brightnessObserver bug
    public static boolean shouldMakeRequests = false;

    ActivityRecognitionProvider activityProvider;
    LocationProvider locationProvider;
    UserSatisfactionProvider satisfactionProvider;
    SensorProvider sensorProvider;
    PowerReadingProvider powerProvider;
    ForegroundAppProvider appProvider;

    DataCollectionManager dataCollectionManager;

    private static boolean isServiceRunning;

    @Override
    public void onCreate() {
        // make sure device has unique id
        UniqueIDManager.initializeID(this);

        sensorsValues = new HashMap<>();
        contentResolver = getContentResolver();

        // Create log file
        LoggerCSV.initialize(this, Definitions.sensorsLogged);
        logger = LoggerCSV.getInstance();

        // put initial brightness value in map
        int brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0);
        sensorsValues.put("screen_brightness", Integer.toString(brightness));
        sensorsValues.put("user_changed_brightness", "1");
        lastBrightness = brightness;


        // observer for tracking changes to screen brightness
        ContentObserver brightnessObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                int brightness = Settings.System.getInt(contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS, 0);
                if (lastBrightness != brightness) {
                    sensorsValues.put("screen_brightness", Integer.toString(brightness));

                    if (DBG) Log.v(TAG, "screen_brightness " + brightness +
                            " user changed? " + sensorsValues.get("user_changed_brightness"));
                    logger.appendValues(sensorsValues);
                    sensorsValues.put("user_changed_brightness", "1");
                }

                lastBrightness = brightness;
            }
        };
        contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false, brightnessObserver);

        // timer to do brightness inference task/request
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (shouldMakeRequests) {
                    makePredictionRequestToServer();
                }
            }
        }, 3000, 10000);

        activityProvider = new ActivityRecognitionProvider(this);
        locationProvider = new LocationProvider(this);
        satisfactionProvider = new UserSatisfactionProvider(this);
        sensorProvider = new SensorProvider(this);
        powerProvider = new PowerReadingProvider(this);
        appProvider = new ForegroundAppProvider(this);
        dataCollectionManager = new DataCollectionManager(
                activityProvider,
                locationProvider,
                satisfactionProvider,
                sensorProvider,
                powerProvider,
                appProvider
        );

        // set collector to run every COLLECTOR_PERIOD ms
        mCollectorHandler.postDelayed(mCollectorRefresh, COLLECTOR_PERIOD_MSEC);

        if (DBG) Log.i(TAG, "Starting service!");
        Toast.makeText(this, "Started service!", Toast.LENGTH_SHORT).show();
        isServiceRunning = true;
    }

    public static boolean isRunning() {
        return isServiceRunning;
    }

    // create a permanent notification so service can always run in background
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NU Brightness Study")
                .setContentText(input)
                .setSmallIcon(R.drawable.nu_icon_two)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_REDELIVER_INTENT;
    }


    @RequiresApi(api = 26)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public class LocalBinder extends Binder {
        ServiceClassPhone getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServiceClassPhone.this;
        }
    }

    // ask server to make a brightness prediction based on sensor values
    private void makePredictionRequestToServer() {
        RequestQueue queue = Volley.newRequestQueue(ServiceClassPhone.this);
        JSONObject jsonBody = new JSONObject();
        try {
            String header = logger.getHeader();
            jsonBody.put("header", header);
            String values = logger.getLine(sensorsValues);
            jsonBody.put("observations", values);
        } catch (JSONException e) {
            if (DBG) Log.e(TAG, e.toString());
        }

        final String url = Definitions.PREDICT_URL + UniqueIDManager.getUniqueID();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        int prediction = response.getInt("prediction");
                        broadcastSetBrightnessIntent(prediction);
                    } catch (JSONException e) {
                        if (DBG) Log.e(TAG, e.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                if (DBG) Log.e(TAG, e.toString());
                if (DBG) Log.e(TAG, url);
            }
        });
        queue.add(jsonObjectRequest);
    }

    // send a broadcast message to change screen brightness
    private void broadcastSetBrightnessIntent(int prediction) {
        Intent intent = new Intent("setBrightness");
        intent.putExtra("brightness", prediction);
        LocalBroadcastManager.getInstance(ServiceClassPhone.this).sendBroadcast(intent);
        sensorsValues.put("user_changed_brightness", "0");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    final static public long COLLECTOR_PERIOD_MSEC = 200; // 0.2 sec
    Handler mCollectorHandler = new Handler();
    Runnable mCollectorRefresh = new Runnable() {
        @Override
        public void run() {
            try {

                try {
                    satisfactionProvider.askUserSatisfaction();
                } catch (Exception e) {
                    Log.e(TAG, "Error in asking user satisfaction");
                }

                try {
                    activityProvider.detectActivities();
                } catch (Exception e) {
                    Log.e(TAG, "Error in collecting activities");
                }

                try {
                    locationProvider.startCollectingLocation();
                } catch (Exception e) {
                    Log.e(TAG, "Error in collecting location");
                }

                try {
                    powerProvider.doPowerReadings();
                } catch (Exception e) {
                    Log.e(TAG, "Error in collecting power data");
                }

                //try {
                    dataCollectionManager.recordData();
                //} catch (Exception e) {
                //    Log.e(TAG, "Error in data recording");
                //}

                mCollectorHandler.postDelayed(mCollectorRefresh, COLLECTOR_PERIOD_MSEC);
            } catch (Exception e) {
                Log.e(TAG, "Error occurred in collector: " + e);
                e.fillInStackTrace();
                mCollectorHandler.postDelayed(mCollectorRefresh, COLLECTOR_PERIOD_MSEC);
            }
        }
    };
}
