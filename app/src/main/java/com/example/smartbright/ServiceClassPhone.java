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
    private Sensor acceleration;
    private Sensor gyro;

    private void setUpSensors() {

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Light sensor
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsValues.put("ambient_light","");

        // Accelerometer
        acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsValues.put("acc_x","");
        sensorsValues.put("acc_y","");
        sensorsValues.put("acc_z","");

        // gyroscope
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsValues.put("gyro_x","");
        sensorsValues.put("gyro_y","");
        sensorsValues.put("gyro_z","");

        // Setup brightness of the screen


    }
}
