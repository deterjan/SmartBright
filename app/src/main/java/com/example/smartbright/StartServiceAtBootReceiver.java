package com.example.smartbright;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class StartServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, ServiceClassPhone.class);
        serviceIntent.putExtra("inputExtra", "NU Screen Study");
        ContextCompat.startForegroundService(context, serviceIntent);
    }

}