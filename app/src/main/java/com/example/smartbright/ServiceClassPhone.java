package com.example.smartbright;

import android.annotation.TargetApi;
import android.app.ActivityManager;
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
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static com.example.smartbright.Definitions.DBG;

// TODO ondestroy
// TODO location stuff commented
// TODO power: search power_avg in displayfilter

@TargetApi(Build.VERSION_CODES.R)
public class ServiceClassPhone extends Service implements SensorEventListener {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String TAG = ServiceClassPhone.class.getSimpleName();

    private Logger logger; // logger object
    private Map<String, String> sensorsValues; // sensor values map for logging
    private int lastBrightness; // keep last brightness to fix brightnessObserver bug

    private final IBinder mBinder = new LocalBinder();
    private ContentResolver contentResolver;

    // whether we are asking server for predicted brightness
    public static boolean shouldMakeRequests = false;

    LocationManager lm;
    LocationTracker locationTracker;

    @Override
    public void onCreate() {
        // make sure device has unique id
        String uid = UniqueIDManager.initializeID(this);
        if (DBG) Log.d(TAG, "Device ID: " + uid);

        sensorsValues = new HashMap<>();
        contentResolver = getContentResolver();

        // Setup the sensors
        setUpSensors();

        // lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //locationTracker = new LocationTracker(lm);

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
    }


    @RequiresApi(api = 26)
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

        final String url = Definitions.PREDICT_URL + UniqueIDManager.getID();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
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
        Sensor sensor = event.sensor;
        int type = sensor.getType();

        boolean shouldLog = false;

        try {
            if (type == Sensor.TYPE_GYROSCOPE) {

                // Get vals
                Float gyro_x = event.values[0];
                Float gyro_y = event.values[1];
                Float gyro_z = event.values[2];

                // Change hashmap to be printed to log
                sensorsValues.put("gyro_x", gyro_x.toString());
                sensorsValues.put("gyro_y", gyro_y.toString());
                sensorsValues.put("gyro_z", gyro_z.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "gyro_x " + gyro_x + " gyro_y " + gyro_y + " gyro_z " + gyro_z);

            }
            if (type == Sensor.TYPE_ACCELEROMETER) {

                // Get vals
                Float acc_x = event.values[0];
                Float acc_y = event.values[1];
                Float acc_z = event.values[2];

                // Change hashmap to be printed to log
                sensorsValues.put("acc_x", acc_x.toString());
                sensorsValues.put("acc_y", acc_y.toString());
                sensorsValues.put("acc_z", acc_z.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "acc_x " + acc_x + " acc_y " + acc_y + " acc_z " + acc_z);

            }
            if (type == Sensor.TYPE_LIGHT) {

                // Get lxlight value
                Float lxLight = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("ambient_light", lxLight.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "Light " + lxLight);

            }
            if (type == Sensor.TYPE_AMBIENT_TEMPERATURE) {

                // Get temperature
                Float temp = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("temperature", temp.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "Temp " + temp);
            }
            if (type == Sensor.TYPE_PROXIMITY) {
                // Get detect flag
                Float proximity = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("proximity", proximity.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "proximity " + proximity);
            }
            if (type == Sensor.TYPE_STATIONARY_DETECT) {
                // Get detect flag
                Float stationary_detect = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("stationary_detect", stationary_detect.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "stationary_detect " + stationary_detect);
            }
            if (type == Sensor.TYPE_RELATIVE_HUMIDITY) {
                // Get detect flag
                Float humidity = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("humidity", humidity.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "humidity " + humidity);
            }
            if (type == Sensor.TYPE_PRESSURE) {
                // Get detect flag
                Float pressure = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("pressure", pressure.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "pressure " + pressure);
            }
            if (type == Sensor.TYPE_MOTION_DETECT) {
                // Get detect flag
                Float motion_detect = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("motion_detect", motion_detect.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "motion_detect " + motion_detect);
            }
            if (type == Sensor.TYPE_HEART_RATE) {
                // Get detect flag
                Float heart_rate = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("heart_rate", heart_rate.toString());

                // Make sure we log
                shouldLog = true;
                if (DBG) Log.v(TAG, "heart_rate " + heart_rate);
            }
        } catch (Exception e) {
            if (DBG) Log.e(TAG, "Error in sensor reading");
        }

        // Log
        if (shouldLog) {
            // sensorsValues.put("locationAltitude", locationTracker.getAltitude().toString());
            // sensorsValues.put("locationLatitude", locationTracker.getLatitude().toString());
            // sensorsValues.put("LocationLongitude", locationTracker.getLongitude().toString());
            // sensorsValues.put("locationAccuracy", locationTracker.getAccuracy().toString());

            sensorsValues.put("foreground_app", getForegroundAppName());
            logger.appendValues(sensorsValues);
        }
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    private SensorManager sensorManager;
    private Sensor light;
    private Sensor acceleration;
    private Sensor gyro;
    private Sensor temperature;
    private Sensor walking;
    private Sensor stationary;
    private Sensor humidity;
    private Sensor pressure;
    private Sensor motion_detect;
    private Sensor heart_rate;

    private void setUpSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Light sensor
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsValues.put("ambient_light", "");

        // Accelerometer
        acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsValues.put("acc_x", "");
        sensorsValues.put("acc_y", "");
        sensorsValues.put("acc_z", "");

        // gyroscope
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsValues.put("gyro_x", "");
        sensorsValues.put("gyro_y", "");
        sensorsValues.put("gyro_z", "");

        // Temperature
        temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL);

        // Step detector (1 if step detected)
        walking = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this, walking, SensorManager.SENSOR_DELAY_NORMAL);

        // Stationary detect
        stationary = sensorManager.getDefaultSensor(Sensor.TYPE_STATIONARY_DETECT);
        sensorManager.registerListener(this, stationary, SensorManager.SENSOR_DELAY_NORMAL);

        // Humidity
        humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        sensorManager.registerListener(this, humidity, SensorManager.SENSOR_DELAY_NORMAL);

        // Pressure
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);

        // Motion detect
        motion_detect = sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT);
        sensorManager.registerListener(this, motion_detect, SensorManager.SENSOR_DELAY_NORMAL);

        // Heart Rate
        heart_rate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(this, heart_rate, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private String getForegroundAppName() {
        // TODO user needs to enable "USAGE DATA ACCESS" for smartbright
        String currentApp = "NULL";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (appList == null)
                System.out.println("applist is null");
            if (appList.size() == 0) {
                // System.out.println(appList.get(0));
                System.out.println("applist size is 0");
            }
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        if (DBG) Log.v(TAG, "Current App in foreground is: " + currentApp);
        return currentApp;
    }


}
