package com.example.smartbright;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.R)
public class ServiceClassPhone extends Service implements SensorEventListener {

    private final IBinder mBinder = new LocalBinder();

    // Declare vars
    Logger logger;

    private Map<String, String> sensorsValues;


    @Override
    public void onCreate() {

        sensorsValues = new HashMap<String, String>();

        // Setup the sensors
        setUpSensors();

        // Create log file
        logger = new LoggerCSV(this, Definitions.sensorsLogged);

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

        // Get lxlight value
        Float lxLight = event.values[0];

        // change Hashmap to be printed
        sensorsValues.put("ambient_light",lxLight.toString());

        // Log
        logger.appendValues(sensorsValues);

        Log.w("myTag", "Light " + lxLight);
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    private SensorManager sensorManager;
    private Sensor light;

    private void setUpSensors() {

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Light sensor
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        // Apend light to map
        sensorsValues.put("ambient_light","");

        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
