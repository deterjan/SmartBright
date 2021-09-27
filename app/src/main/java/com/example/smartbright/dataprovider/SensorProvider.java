package com.example.smartbright.dataprovider;

import static com.example.smartbright.Definitions.DBG;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.HashMap;

public class SensorProvider implements SensorEventListener, DataProvider {
    private static final String TAG = SensorProvider.class.getSimpleName();

    private final SensorManager sensorManager;

    private final Sensor lightSensor;
    private final Sensor accelerationSensor;
    private final Sensor gyroSensor;
    private final Sensor temperatureSensor;
    private final Sensor humiditySensor;
    private final Sensor pressureSensor;
    private final Sensor proximitySensor;

    private float gyroX;
    private float gyroY;
    private float gyroZ;

    private float accX;
    private float accY;
    private float accZ;

    private float ambientLight;
    private float temperature;
    private float proximity;
    private float humidity;
    private float pressure;

    public SensorProvider(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        registerSensorListeners();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        int type = sensor.getType();

        try {
            if (type == Sensor.TYPE_GYROSCOPE) {
                gyroX = event.values[0];
                gyroY = event.values[1];
                gyroZ = event.values[2];
                if (DBG) Log.v(TAG, "gyroX " + gyroX + " gyroY " + gyroY + " gyroZ " + gyroZ);
            }
            if (type == Sensor.TYPE_ACCELEROMETER) {
                accX = event.values[0];
                accY = event.values[1];
                accZ = event.values[2];
                if (DBG) Log.v(TAG, "acc_x " + accX + " acc_y " + accY + " acc_z " + accZ);
            }
            if (type == Sensor.TYPE_LIGHT) {
                ambientLight = event.values[0];
                if (DBG) Log.v(TAG, "ambient light " + ambientLight);
            }
            if (type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                temperature = event.values[0];
                if (DBG) Log.v(TAG, "temp " + temperature);
            }
            if (type == Sensor.TYPE_PROXIMITY) {
                proximity = event.values[0];
                if (DBG) Log.v(TAG, "proximity " + proximity);
            }
            if (type == Sensor.TYPE_RELATIVE_HUMIDITY) {
                humidity = event.values[0];
                if (DBG) Log.v(TAG, "humidity " + humidity);
            }
            if (type == Sensor.TYPE_PRESSURE) {
                pressure = event.values[0];
                if (DBG) Log.v(TAG, "pressure " + pressure);
            }
        } catch (Exception e) {
            if (DBG) Log.e(TAG, "Error in sensor reading");
        }
    }

    @Override
    public HashMap<String, Object> getData() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("gyroX", gyroX);
        map.put("gyroY", gyroY);
        map.put("gyroZ", gyroZ);

        map.put("accX", accX);
        map.put("accY", accY);
        map.put("accZ", accZ);

        map.put("ambientLight", ambientLight);
        map.put("temperature", temperature);
        map.put("proximity", proximity);
        map.put("humidity", humidity);
        map.put("pressure", pressure);

        return map;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing really
    }

    private void registerSensorListeners(){
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensorListeners(){
        sensorManager.unregisterListener(this);
    }
}

