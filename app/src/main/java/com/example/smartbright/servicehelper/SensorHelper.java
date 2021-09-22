package com.example.smartbright.servicehelper;

import static com.example.smartbright.Definitions.DBG;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class SensorHelper implements SensorEventListener {
    private static final String TAG = SensorHelper.class.getSimpleName();

    private Map<String, String> sensorsValues; // sensor values map for logging
    private final SensorManager sensorManager;

    private final Sensor light;
    private final Sensor acceleration;
    private final Sensor gyro;
    private final Sensor temperature;
    private final Sensor humidity;
    private final Sensor pressure;
    private final Sensor proximity;

    public SensorHelper(Context context){
        sensorsValues = new HashMap<>();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        register();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        int type = sensor.getType();

        try {
            if (type == Sensor.TYPE_GYROSCOPE) {
                Float gyro_x = event.values[0];
                Float gyro_y = event.values[1];
                Float gyro_z = event.values[2];

                sensorsValues.put("gyro_x", gyro_x.toString());
                sensorsValues.put("gyro_y", gyro_y.toString());
                sensorsValues.put("gyro_z", gyro_z.toString());

                if (DBG) Log.v(TAG, "gyro_x " + gyro_x + " gyro_y " + gyro_y + " gyro_z " + gyro_z);
            }
            if (type == Sensor.TYPE_ACCELEROMETER) {
                Float acc_x = event.values[0];
                Float acc_y = event.values[1];
                Float acc_z = event.values[2];

                sensorsValues.put("acc_x", acc_x.toString());
                sensorsValues.put("acc_y", acc_y.toString());
                sensorsValues.put("acc_z", acc_z.toString());

                if (DBG) Log.v(TAG, "acc_x " + acc_x + " acc_y " + acc_y + " acc_z " + acc_z);
            }
            if (type == Sensor.TYPE_LIGHT) {
                Float lxLight = event.values[0];

                sensorsValues.put("ambient_light", lxLight.toString());

                if (DBG) Log.v(TAG, "Light " + lxLight);
            }
            if (type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                Float temp = event.values[0];

                sensorsValues.put("temperature", temp.toString());

                if (DBG) Log.v(TAG, "Temp " + temp);
            }
            if (type == Sensor.TYPE_PROXIMITY) {
                Float proximity = event.values[0];

                sensorsValues.put("proximity", proximity.toString());

                if (DBG) Log.v(TAG, "proximity " + proximity);
            }
            if (type == Sensor.TYPE_RELATIVE_HUMIDITY) {
                Float humidity = event.values[0];

                sensorsValues.put("humidity", humidity.toString());

                if (DBG) Log.v(TAG, "humidity " + humidity);
            }
            if (type == Sensor.TYPE_PRESSURE) {
                Float pressure = event.values[0];

                sensorsValues.put("pressure", pressure.toString());

                if (DBG) Log.v(TAG, "pressure " + pressure);
            }
        } catch (Exception e) {
            if (DBG) Log.e(TAG, "Error in sensor reading");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void register(){
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, humidity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister(){
        sensorManager.unregisterListener(this);
    }
}

