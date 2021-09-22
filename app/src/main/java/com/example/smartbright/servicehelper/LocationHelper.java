package com.example.smartbright.servicehelper;

import static com.example.smartbright.Definitions.DBG;

import android.Manifest;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.smartbright.ServiceClassPhone;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

// todo Location stuff logging
// todo make sure location is turned on
public class LocationHelper {
    private static final String TAG = LocationHelper.class.getSimpleName();

    private final Service service;

    private boolean startedCollectingLocation = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mLocation;

    public LocationHelper(Service service) {
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
            // if(DBG) Log.d(TAG, "location is not granted yet!");
            // Toast.makeText(service, "Please grant location permission and all requested " +
            //                "permissions for App Usage application!",
            //        Toast.LENGTH_LONG).show();
            startedCollectingLocation = false;
            return;
        }
        if (startedCollectingLocation) return;

        if (DBG) Log.d(TAG, "Location collecting starting...");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(service);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        if (DBG) Log.d(TAG, "location: " + location.getLatitude()
                                + " " + location.getAccuracy() + " " + location.getLongitude());
                        // mLogger.logStringEntry("LocAlt: " + location.getAltitude());
                        // mLogger.logStringEntry("LocLat: " + location.getLatitude());
                        // mLogger.logStringEntry("LocLon: " + location.getLongitude());
                        // mLogger.logStringEntry("LocAcc: " + location.getAccuracy());

                    } else {
                        if (DBG) Log.d(TAG, "location: null");
                    }
                }
            }
        };

        //createLocationRequest();
        mLocationRequest = LocationRequest.create()
                .setInterval(20000)
                .setFastestInterval(10000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        startedCollectingLocation = true;
    }

    private void onNewLocation(Location location) {
        if (DBG) Log.d(TAG, "New location: " + location);

        mLocation = location;

        // Notify anyone listening for broadcasts about the new location.
        // Intent intent = new Intent(ACTION_BROADCAST);
        // intent.putExtra(EXTRA_LOCATION, location);
        // LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
