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

import static com.example.smartbright.Definitions.TAG;

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

        // Sensor obj
        Sensor sensor = event.sensor;
        int type = sensor.getType();

        // Bool to assert that we made any change
        boolean do_log = false;

        try {
            if (type == Sensor.TYPE_GYROSCOPE){

                // Get vals
                Float gyro_x = event.values[0];
                Float gyro_y = event.values[1];
                Float gyro_z = event.values[2];

                // Change hashmap to be printed to log
                sensorsValues.put("gyro_x",gyro_x.toString());
                sensorsValues.put("gyro_y",gyro_y.toString());
                sensorsValues.put("gyro_z",gyro_z.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "gyro_x " + gyro_x + " gyro_y " + gyro_y + " gyro_z " + gyro_z);

            }
            if (type == Sensor.TYPE_ACCELEROMETER) {

                // Get vals
                Float acc_x = event.values[0];
                Float acc_y = event.values[1];
                Float acc_z = event.values[2];

                // Change hashmap to be printed to log
                sensorsValues.put("acc_x",acc_x.toString());
                sensorsValues.put("acc_y",acc_y.toString());
                sensorsValues.put("acc_z",acc_z.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "acc_x " + acc_x + " acc_y " + acc_y + " acc_z " + acc_z);

            }
            if (type == Sensor.TYPE_LIGHT){

                // Get lxlight value
                Float lxLight = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("ambient_light",lxLight.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "Light " + lxLight);

            }
            if (type == Sensor.TYPE_AMBIENT_TEMPERATURE){

                // Get temperature
                Float temp = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("temperature",temp.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "Temp " + temp);
            }
            if (type == Sensor.TYPE_PROXIMITY){
                // Get detect flag
                Float proximity = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("proximity",proximity.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "proximity " + proximity);
            }
            if (type == Sensor.TYPE_STATIONARY_DETECT){
                // Get detect flag
                Float stationary_detect = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("stationary_detect",stationary_detect.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "stationary_detect " + stationary_detect);
            }
            if (type == Sensor.TYPE_RELATIVE_HUMIDITY){
                // Get detect flag
                Float humidity = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("humidity",humidity.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "humidity " + humidity);
            }
            if (type == Sensor.TYPE_PRESSURE){
                // Get detect flag
                Float pressure = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("pressure",pressure.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "pressure " + pressure);
            }
            if (type == Sensor.TYPE_MOTION_DETECT){
                // Get detect flag
                Float motion_detect = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("motion_detect",motion_detect.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "motion_detect " + motion_detect);
            }
            if (type == Sensor.TYPE_HEART_RATE){
                // Get detect flag
                Float heart_rate = event.values[0];

                // change Hashmap to be printed
                sensorsValues.put("heart_rate",heart_rate.toString());

                // Make sure we log
                do_log = true;
                Log.w("myTag", "heart_rate " + heart_rate);
            }
        } catch (Exception e) {
            Log.d(TAG , "Error in sensor reading");
        }

        // Log
        if (do_log) {
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
        //BrightnessObserver brightnessObserver = new BrightnessObserver();


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
}
