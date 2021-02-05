package com.example.smartbright;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    final public static String TAG = "SmartBrightMainActiviy";
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1234;
    final private static boolean DBG = Definitions.DBG;

    Random r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get all permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG , "Getting all permissions");
            getAllPermissions();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getAllPermissions(){
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!Settings.canDrawOverlays(this))
            permissionsNeeded.add("System Alert");
        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Audio Record");
        if (!Settings.System.canWrite(this))
            permissionsNeeded.add("Write Settings");
        if (!addPermission(permissionsList, Manifest.permission.BODY_SENSORS))
            permissionsNeeded.add("Read Storage");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Access Fine Location");
        if (!addPermission(permissionsList, Manifest.permission.PACKAGE_USAGE_STATS))
            permissionsNeeded.add("Package Usage Stats");
        // if (!addPermission(permissionsList, Manifest.permission.DATA_USAGE))
        //     permissionsNeeded.add("Package Usage Stats");
        getDeviceUniqueId();

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }


    private void getDeviceUniqueId(){
        try{
            //TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            //String uid = tManager.getDeviceId();
            SharedPreferences prefs;
            prefs=this.getSharedPreferences(Definitions.DEVICE_UNIQUE_ID, Context.MODE_PRIVATE);
            int unique_id = prefs.getInt(Definitions.DEVICE_ID, -1);
            if (unique_id==-1){
                r = new Random();
                int ran = r.nextInt(10000 - 1) + 1;
                prefs.edit().putInt(Definitions.DEVICE_ID , ran).commit();
                if (DBG) Log.d(TAG , "Device unique id is created: " + ran);
            }
        } catch (Exception e){
            if (DBG) Log.d(TAG , "Device unique id is not created: " + e);
        }


    }




}