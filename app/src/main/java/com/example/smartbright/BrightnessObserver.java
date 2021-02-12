package com.example.smartbright;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;


public class BrightnessObserver extends ContentObserver {

    ContentResolver contentResolver;

    public BrightnessObserver(Handler handler){
        super(handler);
    }

    public void setContentResolver(ContentResolver cr){
        contentResolver = cr;
    }

    @Override
    public void onChange(boolean selfChange){
        this.onChange(selfChange, null);

        // Get brightness
        // https://stackoverflow.com/questions/46119279/how-to-detect-if-screen-brightness-has-changed-in-android
        int val = Settings.System.getInt(contentResolver,Settings.System.SCREEN_BRIGHTNESS,0);

    }

    @Override
    public void onChange(boolean selfChange, Uri uri){

    }


}
