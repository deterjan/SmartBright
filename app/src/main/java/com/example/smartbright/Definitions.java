package com.example.smartbright;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Definitions {

    final public static String TAG = "SmartBrightMainActivity";
    final static public boolean DBG = true;
    final static public String DEVICE_UNIQUE_ID = "DEVICE_UNIQUE_ID";
    final static public String DEVICE_ID = "DEVICE_ID";

    // Sensors being logged
    final static public List<String> sensorsLogged = new ArrayList<> (Arrays.asList("ambient_light","acc_x","acc_y","acc_z","gyro_x","gyro_y","gyro_z"));

}
