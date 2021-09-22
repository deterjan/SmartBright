package com.example.smartbright;

import static com.example.smartbright.Definitions.DBG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.UUID;

public class UniqueIDManager {
    private static final String TAG = UniqueIDManager.class.getSimpleName();

    private final static String ID_KEY = "DEVICE_UNIQUE_ID";
    private final static String DEFAULT_ID = "NOT_INITIALIZED";

    private static String uniqueID = DEFAULT_ID;

    public static void initializeID(Context mainActivityContext) {
        if (!uniqueID.equals(DEFAULT_ID)) {
            if (DBG) Log.d(TAG, "Redundant call to initializeID");
        }
        else {
            SharedPreferences prefs = mainActivityContext.getSharedPreferences(ID_KEY,
                    Context.MODE_PRIVATE);
            uniqueID = prefs.getString(ID_KEY, DEFAULT_ID);

            if (uniqueID.equals(DEFAULT_ID)) {
                String randomUUID = UUID.randomUUID().toString();
                prefs.edit().putString(ID_KEY, randomUUID).apply();
                uniqueID = randomUUID;

                if (Definitions.DBG) Log.d(TAG, "Device ID initialized: " + uniqueID);
            }
        }
    }

    public static String getUniqueID() {
        return uniqueID;
    }
}
