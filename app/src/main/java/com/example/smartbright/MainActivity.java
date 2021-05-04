package com.example.smartbright;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.Manifest;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.IBinder;

import android.util.Log;

import android.provider.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    final public static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1234;
    final private static boolean DBG = Definitions.DBG;
    ServiceClassPhone myService;
    boolean mBound = false;

    //Variable to store brightness value
    private int brightness;
    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window
    private Window window;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServiceClassPhone.LocalBinder binder = (ServiceClassPhone.LocalBinder) service;
            myService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

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
            Permissions.getAllPermissions(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }

        //Get the content resolver
        cResolver = getContentResolver();
        //Get the current window
        window = getWindow();

        // Start application
        // Intent intent = new Intent(MainActivity.this, ServiceClassPhone.class);

        // TODO this causes app to crash:
        // startService(intent); // it is needed since service should run after activity is destroyed.

        // bindService(intent, connection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter("setBrightness"));

        Switch sw = findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ServiceClassPhone.shouldMakeRequests = true;
                } else {
                    ServiceClassPhone.shouldMakeRequests = false;
                }
            }
        });

        // TODO foreground
        Intent serviceIntent = new Intent(this, ServiceClassPhone.class);
        serviceIntent.putExtra("inputExtra", "NU Screen Study");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    // Permissions stuff
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            Permissions.onMultiplePermissionsAsked(this, permissions, grantResults);
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

