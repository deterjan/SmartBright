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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

@TargetApi(Build.VERSION_CODES.R)
public class ServiceClassPhone extends Service implements SensorEventListener {

    protected void onCreate(Bundle savedInstanceState) {
        setUpSensors();
    }

    private final IBinder mBinder = new LocalBinder();

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
        System.out.println("hello");
        float lxLight = event.values[0];
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
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
