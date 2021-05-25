package com.example.smartbright;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;
import static com.example.smartbright.Definitions.DBG;

import com.afollestad.materialdialogs.MaterialDialog;

public class PermissionsManager {
    private static final String TAG = PermissionsManager.class.getSimpleName();

    final public static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1234;

    private final Activity mainActivity;

    public PermissionsManager(Activity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void getAllPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<String>();

        if (!Settings.canDrawOverlays(mainActivity))
            permissionsNeeded.add("Create Dialog Notifications");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_SETTINGS))
            permissionsNeeded.add("Write Settings");
        if (!addPermission(permissionsList, Manifest.permission.BODY_SENSORS))
            permissionsNeeded.add("Get Ambient Sensor");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Access Fine Location");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Get Coarse Location Access");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            permissionsNeeded.add("Get Background Location Access");

        if (!Settings.canDrawOverlays(mainActivity))
            permissionsNeeded.add("System Alert");
        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Audio Record");
        if (!addPermission(permissionsList, Manifest.permission.BATTERY_STATS))
            permissionsNeeded.add("Battery Stats");
        if (!Settings.System.canWrite(mainActivity))
            permissionsNeeded.add("Write Settings");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (!addPermission(permissionsList, Manifest.permission.PACKAGE_USAGE_STATS))
            permissionsNeeded.add("Package Usage Stats");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mainActivity.requestPermissions(permissionsList.toArray(new String[0]),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                    }
                });
                return;
            }
            mainActivity.requestPermissions(permissionsList.toArray(new String[0]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean checkForUsageStatsPermission() {
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
        DataHolder.getInstance().setAppUsageStatsPermission(true);
        return mode == MODE_ALLOWED;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mainActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (mainActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            return mainActivity.shouldShowRequestPermissionRationale(permission);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onMultiplePermissionsAsked(String[] permissions, int[] grantResults) {
        Map<String, Integer> perms = new HashMap<>();

        perms.put(Manifest.permission.SYSTEM_ALERT_WINDOW, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.BODY_SENSORS, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.PACKAGE_USAGE_STATS, PackageManager.PERMISSION_GRANTED);

        if (!Settings.canDrawOverlays(mainActivity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + mainActivity.getPackageName()));
            mainActivity.startActivityForResult(intent, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }

        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) mainActivity.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), mainActivity.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (mainActivity.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == MODE_ALLOWED);
        }
        if (!granted) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,
                    Uri.parse("package:" + mainActivity.getPackageName()));
            mainActivity.startActivityForResult(intent, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }


        if (!Settings.System.canWrite(mainActivity.getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + mainActivity.getPackageName()));
            mainActivity.startActivityForResult(intent, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }

        // Fill with results
        for (int i = 0; i < permissions.length; i++)
            perms.put(permissions[i], grantResults[i]);
        // Check for ACCESS_FINE_LOCATION
        if (Settings.canDrawOverlays(mainActivity)
                && Settings.System.canWrite(mainActivity.getApplicationContext())
                && perms.get(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(DBG) Log.d(TAG, "All permissions are granted!");

            DataHolder.getInstance().setVoicePermission(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void permissionForUsageStats(){
        try {
            @SuppressWarnings("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager) mainActivity.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time);
            if (appList.size() == 0) {
                Log.e(TAG, "APPSIZE LIST 0");
                AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
                        .setTitle("Usage Access")
                        .setMessage("App will not run without usage access permissions.")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                // intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$SecuritySettingsActivity"));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mainActivity.startActivityForResult(intent, 0);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                int layoutFlag;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
                }

                // android.view.WindowManager$BadTokenException:
                // Unable to add window android.view.ViewRootImpl$W@f0ced5e -- permission denied for window type 2038
                alertDialog.getWindow().setType(layoutFlag);
                alertDialog.show();
            }
        } catch (Exception e){
            Log.d(TAG, "Error in usage stats dialog: " + e.toString());
            e.printStackTrace();
        }
    }

    public void showAppUsageStatsPermissionDialog() {
        MaterialDialog d = new MaterialDialog.Builder(mainActivity)
                .title("Permissions required")
                .content("Failed to retrieve app usage " +
                        "statistics. You need to enable access " +
                        "for this app through Settings. " +
                        "Tap \"OK\", and allow the permission for this app.")
                .positiveText("OK")
                .canceledOnTouchOutside(false)
                .onPositive((dialog, which) -> mainActivity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)))
                .show();

        // Get display width
        Display display = mainActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(width, w.getAttributes().height);
        }
    }
}
