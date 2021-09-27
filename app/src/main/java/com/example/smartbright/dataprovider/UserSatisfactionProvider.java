package com.example.smartbright.dataprovider;

import static com.example.smartbright.Definitions.DBG;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

import com.example.smartbright.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// todo satisfaction logging
public class UserSatisfactionProvider implements DataProvider {
    private static final String TAG = UserSatisfactionProvider.class.getSimpleName();

    final static public String USER_RATING_TIME = "USER_RATING_TIME";
    final static public long FOUR_HOURS_MS = 14400000;
    final static public long ASK_USER_SATISFACTION_PERIOD = FOUR_HOURS_MS;

    private final Service service;
    private boolean screenClicked = false;
    private Integer lastUserSatisfaction = 2; // default satisfaction is OKAY

    public UserSatisfactionProvider(Service service) {
        this.service = service;
    }

    @Override
    public Map<String, Object> getData() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("satisfaction", lastUserSatisfaction);
        return map;
    }

    public boolean isPhoneLocked() {
        KeyguardManager myKM = (KeyguardManager) service.getApplicationContext().
                getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.isKeyguardLocked();
    }

    private void askForSatisfaction() {
        final ArrayList<Integer> mSatisfactionItems = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(service.getApplicationContext());
        builder.setTitle(R.string.satisfaction_title)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(R.array.satisfactions, 2,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // If the user checked the item, add it to the selected items
                                mSatisfactionItems.add(which);
                                if (DBG) Log.d(TAG, "User satisfactions: " + which);
                            }
                        })
                // Set the action buttons
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        for (int i = 0; i < mSatisfactionItems.size(); i++) {
                            if (DBG) Log.d(TAG, "User satisfactions: " + mSatisfactionItems.get(i));
                            Log.i("SATISFACTION ", mSatisfactionItems.get(i).toString());
                            lastUserSatisfaction = mSatisfactionItems.get(i);
                            // mLogger.logStringEntry("Satis: " + mSatisfactionItems.get(i));
                        }
                        if (mSatisfactionItems.size() == 0) {
                            // mLogger.logStringEntry("Satis: " + 0);
                            lastUserSatisfaction = 2;
                        }
                        screenClicked = false;
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });


        AlertDialog dialog = builder.create();
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        dialog.getWindow().setType(LAYOUT_FLAG);
        // dialog.getWindow().getAttributes().privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        if (DBG) Log.d(TAG, "Created a AlertBuilder");
    }

    // TODO change logging mechanism for user satisfaction
    public void askUserSatisfaction() {
        // user dialog to collect user screen satisfactions.
        SharedPreferences prefsUserInput = service.getSharedPreferences(USER_RATING_TIME,
                Context.MODE_PRIVATE);
        long lastTimeUserRating = prefsUserInput.getLong(USER_RATING_TIME, 0);

        // Log.i("SATISFACTION", "time since last: " + (System.currentTimeMillis() - lastTimeUserRating));
        // Log.i("SATISFACTION", "screen clicked: " + screen_clicked);
        // Log.i("SATISFACTION", "locked: " + isPhoneLocked());

        if (System.currentTimeMillis() - lastTimeUserRating >= ASK_USER_SATISFACTION_PERIOD
                && !screenClicked && !isPhoneLocked()) {
            screenClicked = true;
            try {
                askForSatisfaction();
                prefsUserInput.edit().putLong(USER_RATING_TIME,
                        System.currentTimeMillis()).commit();
            } catch (Exception e) {
                Log.d(TAG, "Error in alert dialog: " + e);
                screenClicked = false;
            }
        }
    }
}
