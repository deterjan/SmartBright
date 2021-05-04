package com.example.smartbright;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.smartbright.MainActivity.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;

public class Permissions {
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void getAllPermissions(Activity mainActivity) {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!Settings.canDrawOverlays(mainActivity))
            permissionsNeeded.add("System Alert");
        if (!addPermission(mainActivity, permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Audio Record");
        if (!addPermission(mainActivity, permissionsList, Manifest.permission.BATTERY_STATS))
            permissionsNeeded.add("Battery Stats");
        if (!Settings.System.canWrite(mainActivity))
            permissionsNeeded.add("Write Settings");
        if (!addPermission(mainActivity, permissionsList, Manifest.permission.GET_TASKS))
            permissionsNeeded.add("Get Tasks");
        if (!addPermission(mainActivity, permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (!addPermission(mainActivity, permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (!addPermission(mainActivity, permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Access Fine Location");
        if (!addPermission(mainActivity, permissionsList, Manifest.permission.PACKAGE_USAGE_STATS))
            permissionsNeeded.add("Package Usage Stats");
        if (!addPermission(mainActivity, permissionsList, Manifest.permission.WRITE_SETTINGS))
            permissionsNeeded.add("Write Settings");
        // if (!addPermission(permissionsList, Manifest.permission.DATA_USAGE))
        //     permissionsNeeded.add("Package Usage Stats");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(mainActivity, message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mainActivity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                    }
                });
                return;
            }
            mainActivity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    private static void showMessageOKCancel(Activity mainActivity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mainActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static boolean addPermission(Activity mainActivity, List<String> permissionsList, String permission) {
        if (mainActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            return mainActivity.shouldShowRequestPermissionRationale(permission);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void onMultiplePermissionsAsked(Activity mainActivity, String[] permissions, int[] grantResults) {
        Map<String, Integer> perms = new HashMap<String, Integer>();
        // Initial
        perms.put(Manifest.permission.SYSTEM_ALERT_WINDOW, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.BODY_SENSORS, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.PACKAGE_USAGE_STATS, PackageManager.PERMISSION_GRANTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(mainActivity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + mainActivity.getPackageName()));
                mainActivity.startActivityForResult(intent, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            boolean granted = false;
            AppOpsManager appOps = (AppOpsManager) mainActivity.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), mainActivity.getPackageName());

            if (mode == AppOpsManager.MODE_DEFAULT) {
                granted = (mainActivity.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
            } else {
                granted = (mode == AppOpsManager.MODE_ALLOWED);
            }
            if (!granted) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,
                        Uri.parse("package:" + mainActivity.getPackageName()));
                mainActivity.startActivityForResult(intent, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(mainActivity.getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + mainActivity.getPackageName()));
                mainActivity.startActivityForResult(intent, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }

        // Fill with results
        for (int i = 0; i < permissions.length; i++)
            perms.put(permissions[i], grantResults[i]);
        // Check for ACCESS_FINE_LOCATION
        if (Settings.canDrawOverlays(mainActivity)
                && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && Settings.System.canWrite(mainActivity.getApplicationContext())) {
            // All Permissions Granted
            DataHolder.getInstance().setVoicePermission(true);
        }
    }
}
