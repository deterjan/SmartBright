package com.example.smartbright;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import android.provider.Settings;

import android.widget.CompoundButton;
import android.widget.Switch;

import static com.example.smartbright.PermissionsManager.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();


    final private static boolean DBG = Definitions.DBG;

    private final PermissionsManager pManager = new PermissionsManager(this);

    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int brightness = intent.getIntExtra("brightness", 50);
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            // TODO are these required to change brightness??
            //Get the current window attributes
            //  WindowManager.LayoutParams layoutpars = window.getAttributes();
            //Set the brightness of this window
            // layoutpars.screenBrightness = brightness / (float) 255;
            //Apply attribute changes to this window
            // window.setAttributes(layoutpars);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // make sure device has unique id
        String uid = UniqueIDManager.initializeID(this);
        if (DBG) Log.d(TAG, "Device ID: " + uid);

        // Get all permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (DBG) Log.d(TAG, "Getting all permissions");
            pManager.getAllPermissions();
        }
        if (!pManager.checkForUsageStatsPermission()){
            Log.d(TAG, "check for usage permission showing dialog1");
            pManager.permissionForUsageStats();
        }
        if(!pManager.checkForUsageStatsPermission()){
            Log.d(TAG, "check for usage permission showing dialog2");
            pManager.showAppUsageStatsPermissionDialog();
        }

        //Get the content resolver
        cResolver = getContentResolver();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter("setBrightness"));

        Switch sw = findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ServiceClassPhone.shouldMakeRequests = isChecked;
            }
        });

        // Start foreground service so it doesn't get killed by system
        Intent serviceIntent = new Intent(this, ServiceClassPhone.class);
        serviceIntent.putExtra("inputExtra", "NU Screen Study");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    // Permissions stuff
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            pManager.onMultiplePermissionsAsked(permissions, grantResults);
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

