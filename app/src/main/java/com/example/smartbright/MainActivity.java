package com.example.smartbright;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smartbright.logger.LoggerCSV;
import com.example.smartbright.service.ServiceClassPhone;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = MainActivity.class.getSimpleName();

    private PermissionManager permManager;

    private ContentResolver cResolver;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permManager = new PermissionManager(this);

        //Get the content resolver and set up brightness adjustment
        cResolver = getContentResolver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter("setBrightness"));

        // set up switch for brightness prediction
        SwitchCompat sw = findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ServiceClassPhone.shouldMakeRequests = isChecked;
        });
        setUpPermissionButtons();


        Timer permCheckTimer = new Timer();
        permCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (permManager.hasUsageStatsPermission()) {
                    Button btn = findViewById(R.id.usagePermBtn);
                    btnOnPermissionGranted(btn);
                }

                if (permManager.hasDrawOverlaysPermission()) {
                    Button btn = findViewById(R.id.overlayPermBtn);
                    btnOnPermissionGranted(btn);
                }

                boolean hasAllPerms = permManager.hasAllPermissions();
                boolean isServiceRunning = ServiceClassPhone.isRunning();

                if (hasAllPerms && !isServiceRunning) {
                    runOnUiThread(() -> {
                        Button startServiceBtn = findViewById(R.id.startServiceBtn);
                        startServiceBtn.setOnClickListener(v -> {
                            startService();
                            startServiceBtn.setEnabled(false);
                        });
                        startServiceBtn.setEnabled(true);
                    });
                }

                if (hasAllPerms || isServiceRunning) {
                    permCheckTimer.cancel();
                }
            }
        }, 0, 1000);
    }

    private void startService() {
        // Start as foreground service so it doesn't get killed by system
        Intent serviceIntent = new Intent(this, ServiceClassPhone.class);
        serviceIntent.putExtra("inputExtra", "NU Screen Study");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void startButtonsTimer() {

    }

    private void setUpPermissionButtons() {
        setUpPermissionButton(
                0,
                permManager.hasLocationPermission(),
                x -> { permManager.requestLocationPermission(); return null; },
                findViewById(R.id.locationPermBtn),
                findViewById(R.id.locationPermRow)
        );

        setUpPermissionButton(
                Build.VERSION_CODES.Q,
                permManager.hasBackgroundLocationPermission(),
                x -> { permManager.requestBackgroundLocationPermission(); return null; },
                findViewById(R.id.backLocationPermBtn),
                findViewById(R.id.backLocationPermRow)
        );

        setUpPermissionButton(
                Build.VERSION_CODES.Q,
                permManager.hasActivityRecognitionPermission(),
                x -> { permManager.requestActivityRecognitionPermission(); return null; },
                findViewById(R.id.activityPermBtn),
                findViewById(R.id.activityPermRow)
        );

        setUpPermissionButton(
                0,
                permManager.hasUsageStatsPermission(),
                x -> { permManager.requestUsageStatsPermission(); return null; },
                findViewById(R.id.usagePermBtn),
                findViewById(R.id.usagePermRow)
        );

        setUpPermissionButton(
                0,
                permManager.hasDrawOverlaysPermission(),
                x -> { permManager.requestDrawOverlayPermission(); return null; },
                findViewById(R.id.overlayPermBtn),
                findViewById(R.id.overlayPermRow)
        );
    }

    private void setUpPermissionButton(int requiredSdk,
                                       boolean isDisabled,
                                       Function<Void, Void> requestFunction,
                                       Button btn,
                                       LinearLayout row) {
        int sdk = Build.VERSION.SDK_INT;

        if (sdk < requiredSdk) {
            row.setVisibility(View.GONE);
        }
        else {
            btn.setOnClickListener(v -> {
                requestFunction.apply(null);
            });

            if (isDisabled) {
                btnOnPermissionGranted(btn);
            }
        }
    }

    private void setupDebugText() {
        Timer debugPrintTimer = new Timer();
        debugPrintTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Activity m = MainActivity.this;

                TextView t = findViewById(R.id.debugTextView);
                String[] logsPathFileList = new File("/data/data/com.example.smartbright/files/").list();

                LoggerCSV logger = LoggerCSV.getInstance();

                String debugText = "";
                // debugText += logger.getNumLines() + "<" + Definitions.LOG_MAX_LINES + "\n";
                // debugText += java.lang.String.join("\n", logsPathFileList) + "\n";

                debugText += "\nLocation Permissions: " + permManager.hasLocationPermission();
                debugText += "\nCan draw overlays: " + Settings.canDrawOverlays(m);
                debugText += "\nSystem write access: " + Settings.System.canWrite(m);

                debugText += "\nNetwork state access: " + EasyPermissions.hasPermissions(m,
                        Manifest.permission.ACCESS_NETWORK_STATE);
                debugText += "\nWifi state access: " + EasyPermissions.hasPermissions(m,
                        Manifest.permission.ACCESS_WIFI_STATE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    debugText += "\nActivity recognition access: " + EasyPermissions.hasPermissions(m,
                            Manifest.permission.ACTIVITY_RECOGNITION);
                }
                debugText += "\nPackage usage stats access: " + permManager.hasUsageStatsPermission();


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    debugText += "\nBackground location permission: " +
                            EasyPermissions.hasPermissions(m, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }


                t.setText(debugText);
            }
        }, 1000, 3000);
    }

    public void btnOnPermissionGranted(Button btn) {
        runOnUiThread(() -> {
            btn.setEnabled(false);
            btn.setText(R.string.granted_btn_text);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        permManager.onPermissionsGranted(requestCode, perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}

