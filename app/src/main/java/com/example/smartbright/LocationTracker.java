package com.example.smartbright;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;

public class LocationTracker implements LocationListener {
    private static final String TAG = LocationTracker.class.getSimpleName();

    private LocationManager lm;
    private String provider;

    private Double altitude = 0.0;
    private Double longitude = 0.0;
    private Double latitude = 0.0;
    private Float accuracy = (float) 0.0;


    @SuppressLint("MissingPermission")
    public LocationTracker(LocationManager locationManager) {
        this.lm = locationManager;
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        // Location location = locationManager.getLastKnownLocation(provider);
        // onLocationChanged(location);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.accuracy = location.getAccuracy();
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.altitude = location.getAltitude();
    }

    public Double getAltitude() {
        return altitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Float getAccuracy() {
        return accuracy;
    }
}
