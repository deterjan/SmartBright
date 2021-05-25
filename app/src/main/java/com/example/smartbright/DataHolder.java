package com.example.smartbright;

public class DataHolder {

    private boolean voicePermission = false;

    private boolean appUsageStatsPermission = false;

    public void setAppUsageStatsPermission(boolean appUsageStatsPermission) {
        this.appUsageStatsPermission = appUsageStatsPermission;
    }

    public void setVoicePermission(boolean voicePermission) {
        this.voicePermission = voicePermission;
    }

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() { return holder; }
}
