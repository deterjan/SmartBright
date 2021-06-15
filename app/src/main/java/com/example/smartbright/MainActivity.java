package com.example.smartbright;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
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
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.smartbright.Definitions.LOG_MAX_LINES;
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

        // Get all permissions
        pManager.getAllPermissions();

        if (!pManager.checkForUsageStatsPermission()){
            Log.d(TAG, "check for usage permission showing dialog1");
            pManager.permissionForUsageStats();
        }
        if(!pManager.checkForUsageStatsPermission()){
            Log.d(TAG, "check for usage permission showing dialog2");
            pManager.showAppUsageStatsPermissionDialog();
        }

        if (!pManager.getToken()) pManager.allPermissionsAreGranted();

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

        // print some debug stuff
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                TextView t = findViewById(R.id.logTextView);
                String[] logsPathFileList = new File("/data/data/com.example.smartbright/files/").list();

                LoggerCSV logger = LoggerCSV.getInstance();

                String s1 = Integer.toString(logger.getNumLines()) + " < "
                        + Integer.toString(LOG_MAX_LINES) + "\n";
                String s2 = String.join("\n", logsPathFileList);
                t.setText(s1+s2);
            }
        }, 10000, 3000);
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

