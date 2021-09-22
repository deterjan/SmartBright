package com.example.smartbright;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
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
import com.example.smartbright.logger.Logger;
import com.example.smartbright.logger.LoggerCSV;
import com.example.smartbright.servicehelper.ActivityRecognitionHelper;
import com.example.smartbright.servicehelper.LocationHelper;
import com.example.smartbright.servicehelper.UserSatisfactionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static com.example.smartbright.Definitions.BATTERY_READING_PERIOD_MS;
import static com.example.smartbright.Definitions.DBG;

// TODO ondestroy

@TargetApi(Build.VERSION_CODES.R)
public class ServiceClassPhone extends Service implements SensorEventListener {

    private static final String TAG = ServiceClassPhone.class.getSimpleName();
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private Logger logger; // logger object
    private Map<String, String> sensorsValues; // sensor values map for logging

    private final IBinder mBinder = new LocalBinder();
    private ContentResolver contentResolver;

    // whether we are asking server for predicted brightness
    private int lastBrightness; // keep last brightness to fix brightnessObserver bug
    public static boolean shouldMakeRequests = false;

    ActivityRecognitionHelper activityHelper;
    LocationHelper locationHelper;
    UserSatisfactionHelper satisfactionHelper;

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

        activityHelper = new ActivityRecognitionHelper(this);
        locationHelper = new LocationHelper(this);
        satisfactionHelper = new UserSatisfactionHelper(this);


        // set collector to run every COLLECTOR_PERIOD ms
        mCollectorHandler.postDelayed(mCollectorRefresh, COLLECTOR_PERIOD);

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
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int prediction = response.getInt("prediction");
                            broadcastSetBrightnessIntent(prediction);
                        } catch (JSONException e) {
                            if (DBG) Log.e(TAG, e.toString());
                        }
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

    public class LocalBinder extends Binder {
        ServiceClassPhone getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServiceClassPhone.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // Sensor obj


        // Log
        sensorsValues.put("foreground_app", getForegroundAppName());
        // check if screen is on
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        sensorsValues.put("screen_interactive", powerManager.isInteractive() ? "1" : "0");

        // user satisfaction
        // sensorsValues.put("last_user_satisfaction", lastUserSatisfaction.toString());

        if (System.currentTimeMillis() - lastReadingTime > BATTERY_READING_PERIOD_MS) {
            lastReadingTime = System.currentTimeMillis();
            doPowerReadings();
            sensorsValues.put("current", current_now_ma.toString());
            sensorsValues.put("voltage", voltage_now.toString());
            sensorsValues.put("power", power_now.toString());
        }

        logger.appendValues(sensorsValues);

        /*
        if (currentLoc != null) {
            sensorsValues.put("location_altitude", ((Double) currentLoc.getAltitude()).toString());
            sensorsValues.put("location_latitude", ((Double) currentLoc.getLatitude()).toString());
            sensorsValues.put("location_longitude", ((Double) currentLoc.getLongitude()).toString());
            sensorsValues.put("location_accuracy", ((Float) currentLoc.getAccuracy()).toString());
        } else {
            Log.e(TAG, "THIS IS NULL");
        }
        */
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }


    private String getForegroundAppName() {
        // user needs to enable "USAGE DATA ACCESS" for smartbright
        String currentApp = "NULL";
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        if (appList == null) {
            // todo problem!
        }
        else if (appList.size() == 0) {
            // todo problem!
        }
        else {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!mySortedMap.isEmpty()) {
                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }

        if (DBG) Log.v(TAG, "Current App in foreground is: " + currentApp);
        return currentApp;
    }

    // power logging
    private Double current_now_ma = .0; // in milliamperes
    private Double voltage_now = .0; // in millivolts
    private Double power_now = .0;
    private long lastReadingTime = System.currentTimeMillis();

    private void doPowerReadings() {
        BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        double current_now_ua = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent b = this.registerReceiver(null, ifilter);
        voltage_now = b.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) * 1.0;

        current_now_ma = current_now_ua / 1000; // from microamperes to milliamperes
        power_now = current_now_ma * voltage_now / 1000; // mW
    }

    final static public long COLLECTOR_PERIOD = 2000; // 2 sec
    Handler mCollectorHandler = new Handler();
    Runnable mCollectorRefresh = new Runnable() {
        @Override
        public void run() {
            try {

                try {
                    satisfactionHelper.askUserSatisfaction();
                } catch (Exception e) {
                    Log.d("SATISFACTION", "Error in asking user satisfaction");
                }

                try{
                    activityHelper.getActivities();
                }catch (Exception e){
                    Log.d(TAG , "Error in activities");
                }

                try{
                    locationHelper.startCollectingLocation();
                }catch (Exception e){
                    Log.d(TAG, "Error on collecting location");
                }



                mCollectorHandler.postDelayed(mCollectorRefresh, COLLECTOR_PERIOD); // in every 2 sec
            } catch (Exception e) {
                Log.i("SATISFACTION", "Error occured in collector: " + e);
                e.fillInStackTrace();
                mCollectorHandler.postDelayed(mCollectorRefresh, COLLECTOR_PERIOD); // run every 2000 sec
            }
        }
    };



}
