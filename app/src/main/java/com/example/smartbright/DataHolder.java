package com.example.smartbright;

public class DataHolder {

    private boolean voice_permission = false;
    public void setVoicePermission(boolean voicePermission) {
        this.voice_permission = voicePermission;
    }

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() { return holder; }
}
