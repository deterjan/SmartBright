package com.example.smartbright.dataprovider;

import static com.example.smartbright.Definitions.DBG;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ForegroundAppProvider implements DataProvider {
    private static final String TAG = ForegroundAppProvider.class.getSimpleName();

    private final Service service;

    public ForegroundAppProvider(Service service) {
        this.service = service;
    }

    @Override
    public Map<String, Object> getData() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("foregroundApp", getForegroundAppName());
        return map;
    }

    public String getForegroundAppName() {
        // user needs to enable "USAGE DATA ACCESS" for app
        String currentApp = "NULL";
        UsageStatsManager usm = (UsageStatsManager) service.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        if (appList == null) {
            if (DBG) Log.e(TAG, "AppList is null!");
        }
        else if (appList.size() == 0) {
            if (DBG) Log.e(TAG, "AppList is empty!");
        }
        else {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!mySortedMap.isEmpty()) {
                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }

        if (DBG) Log.v(TAG, "Current App in foreground is: " + currentApp);
        return currentApp;
    }
}
