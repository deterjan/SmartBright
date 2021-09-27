package com.example.smartbright.dataprovider;

import static com.example.smartbright.Definitions.DBG;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

// todo make sure location is turned on in phone settings?
public class LocationProvider implements DataProvider {
    private static final String TAG = LocationProvider.class.getSimpleName();

    public static final int MIN_PERIOD_MS = 20000;
    public static final int PERIOD_MS = 10000;

    private final Service service;

    private boolean startedCollectingLocation = false;
    private Location mLocation;

    public LocationProvider(Service service) {
        this.service = service;
    }

    public void startCollectingLocation() {
        if (ContextCompat.checkSelfPermission(service, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(service, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT > Build.VERSION_CODES.P &&
                        ContextCompat.checkSelfPermission(service, Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED)) {

            if (DBG) Log.e(TAG, "Location permission not granted!");

            startedCollectingLocation = false;
            return;
        }
        if (startedCollectingLocation) return;

        if (DBG) Log.d(TAG, "Staring to collect location data....");
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(service);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        if (DBG) Log.d(TAG, "location: " + location.getLatitude()
                                + " " + location.getAccuracy() + " " + location.getLongitude());
                    } else {
                        if (DBG) Log.e(TAG, "location: null");
                    }
                }
            }
        };

        LocationRequest mLocationRequest = LocationRequest.create()
                .setInterval(PERIOD_MS)
                .setFastestInterval(MIN_PERIOD_MS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        startedCollectingLocation = true;
    }

    private void onNewLocation(Location location) {
        if (DBG) Log.d(TAG, "New location: " + location);
        mLocation = location;
    }

    @Override
    public Map<String, Object> getData() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("altitude", mLocation.getAltitude());
        map.put("latitude", mLocation.getLatitude());
        map.put("longitude", mLocation.getLongitude());
        map.put("accuracy", (double) mLocation.getAccuracy());

        return map;
    }
}
