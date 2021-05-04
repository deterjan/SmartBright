package com.example.smartbright;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Definitions {
    final static public boolean DBG = true;

    final static public String PREDICT_URL = "http://picard.ece.northwestern.edu:5000/predict/";

    // Sensors being logged
    final static public List<String> sensorsLogged =
            new ArrayList<> (Arrays.asList("ambient_light","acc_x","acc_y","acc_z",
                    "gyro_x","gyro_y","gyro_z",
                    "temperature","pressure","humidity","heart_rate",
                    "stationary_detect","proximity","motion_detect",
                    "screen_brightness","user_changed_brightness","foreground_app"));

}
