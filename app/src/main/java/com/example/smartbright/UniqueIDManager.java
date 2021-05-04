package com.example.smartbright;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.UUID;

public class UniqueIDManager {
    private static final String TAG = UniqueIDManager.class.getSimpleName();

    private static final String ID_KEY = "DEVICE_UNIQUE_ID";
    private static String uniqueID;

    public static String initializeID(Context mainActivityContext) {
        // prevent double init
        if (uniqueID != null) {
            return uniqueID;
        }
        else if (mainActivityContext == null) {
            // cant generate unique id without context
            return null;
        }

        try{
            SharedPreferences prefs;
            prefs = mainActivityContext.getSharedPreferences(ID_KEY,
                    Context.MODE_PRIVATE);
            uniqueID = prefs.getString(ID_KEY, null);

            if (uniqueID != null) {
                return uniqueID;
            }
            else {
                String randomUUID = UUID.randomUUID().toString();
                prefs.edit().putString(ID_KEY, randomUUID).apply();
                uniqueID = randomUUID;
            }
        } catch (Exception e){
            return null;
        }
        return uniqueID;
    }

    public static String getID(){
        return uniqueID;
    }

}
