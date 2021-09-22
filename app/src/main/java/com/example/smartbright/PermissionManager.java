package com.example.smartbright;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class PermissionManager {

    public static final int LOCATION_PERM_CODE = 41;
    public static final int BACKGROUND_LOCATION_PERM_CODE = 42;
    public static final int ACTIVITY_RECOGNITION_PERM_CODE = 43;

    private final MainActivity mainActivity;

    public PermissionManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == LOCATION_PERM_CODE) {
            Button btn = mainActivity.findViewById(R.id.locationPermBtn);
            mainActivity.btnOnPermissionGranted(btn);
        }
        else if (requestCode == BACKGROUND_LOCATION_PERM_CODE) {
            Button btn = mainActivity.findViewById(R.id.backLocationPermBtn);
            mainActivity.btnOnPermissionGranted(btn);
        }
        else if (requestCode == ACTIVITY_RECOGNITION_PERM_CODE) {
            Button btn = mainActivity.findViewById(R.id.activityPermBtn);
            mainActivity.btnOnPermissionGranted(btn);
        }
    }

    public boolean hasAllPermissions() {
        boolean location = hasLocationPermission();
        boolean backgroundLocation = hasBackgroundLocationPermission();
        boolean activity = hasActivityRecognitionPermission();
        boolean usage = hasUsageStatsPermission();
        boolean overlay = hasDrawOverlaysPermission();

        return location && backgroundLocation && activity && usage && overlay;
    }

    public boolean hasLocationPermission() {
        return EasyPermissions.hasPermissions(mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public boolean hasBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return EasyPermissions.hasPermissions(mainActivity,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        else return true;
    }

    public boolean hasActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return EasyPermissions.hasPermissions(mainActivity,
                    android.Manifest.permission.ACTIVITY_RECOGNITION);
        }
        else return true;
    }

    public boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) mainActivity
                .getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo info;
        try {
            info = mainActivity.getPackageManager().getApplicationInfo(mainActivity
                    .getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, info.uid, mainActivity
                .getApplicationContext().getPackageName());
        return mode == MODE_ALLOWED;
    }

    public boolean hasDrawOverlaysPermission() {
        return Settings.canDrawOverlays(mainActivity);
    }

    public void requestLocationPermission() {
        if (!hasLocationPermission()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                EasyPermissions.requestPermissions(
                        mainActivity,
                        "Please allow app to always access device location by selecting \"Allow while using the app\"",
                        LOCATION_PERM_CODE,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                );
            }
            else {
                EasyPermissions.requestPermissions(
                        mainActivity,
                        "Please allow app to access device location",
                        LOCATION_PERM_CODE,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                );
            }
        }
    }

    public void requestBackgroundLocationPermission() {
        if (!hasBackgroundLocationPermission()) {

            String rationale;
            if (!hasLocationPermission()) rationale = "Please grant Location Permission first.";
            else rationale = "Please allow app to always access device location by selecting \"Allow all the time\"";

            EasyPermissions.requestPermissions(
                    mainActivity,
                    rationale,
                    BACKGROUND_LOCATION_PERM_CODE,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            );
        }
    }

    public void requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                    mainActivity,
                    "Please accept activity recognition permissfion",
                    ACTIVITY_RECOGNITION_PERM_CODE,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
            );
        }
    }

    public void requestDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(mainActivity)) {
            new AlertDialog.Builder(mainActivity)
                    .setTitle("Draw Overlays Permission")
                    .setMessage("Please enable draw overlays permission for SmartBright in the following screen")
                    .setPositiveButton("OK",
                            (dialog, which) -> mainActivity.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    )
                    .setIcon(android.R.drawable.ic_dialog_info).show();
        }
    }

    public void requestUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {
            new AlertDialog.Builder(mainActivity)
                    .setTitle("Usage Stats Permission")
                    .setMessage("Please enable usage stats permission for SmartBright in the following screen")
                    .setPositiveButton("OK",
                            (dialog, which) -> mainActivity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    )
                    .setIcon(android.R.drawable.ic_dialog_info).show();
        }
    }
}
