package com.example.smartbright;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Definitions {
    final static public boolean DBG = true;

    // 100k = 1.59 hours, 770kb (without compression?)
    final public static int LOG_MAX_LINES = 100000;

    final public static int BATTERY_READING_PERIOD_MS = 1000;

    final static public String PREDICT_URL = "http://picard.ece.northwestern.edu:5000/predict/";

    // Sensors being logged
    final static public List<String> sensorsLogged =
            new ArrayList<> (Arrays.asList("ambient_light","acc_x","acc_y","acc_z",
                    "gyro_x","gyro_y","gyro_z", "temperature","pressure",
                    "humidity", "proximity",
                    "screen_brightness","user_changed_brightness","screen_interactive",
                    "foreground_app"
                    , "location_altitude", "location_latitude", "location_longitude", "location_accuracy"
                    , "current", "voltage", "power", "last_user_satisfaction"
            ));

}
