package com.example.smartbright.dataprovider;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.util.HashMap;

public class PowerReadingProvider implements DataProvider {
    private static final String TAG = PowerReadingProvider.class.getSimpleName();

    private final Service service;

    private Double current_ma = .0; // in milliamperes
    private Double voltage_mV = .0; // in millivolts
    private Double power_mW = .0;  // in milliwatts
    private long lastReadingTime;

    public PowerReadingProvider(Service service) {
        this.service = service;
    }

    public void doPowerReadings() {
        BatteryManager batteryManager = (BatteryManager) service.getSystemService(Context.BATTERY_SERVICE);
        double current_now_ua = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent b = service.registerReceiver(null, filter);
        voltage_mV = b.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) * 1.0;

        current_ma = current_now_ua / 1000; // from microamperes to milliamperes
        power_mW = current_ma * voltage_mV / 1000; // mW

        lastReadingTime = System.currentTimeMillis();
    }

    @Override
    public HashMap<String, Object> getData() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("current_ma", current_ma);
        map.put("voltage_mV", voltage_mV);
        map.put("power_mW", power_mW);

        return map;
    }
}
