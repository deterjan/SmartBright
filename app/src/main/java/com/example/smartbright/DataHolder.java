package com.example.smartbright;

public class DataHolder {

    private boolean voice_permission = false;
    public void setVoicePermission(boolean voice_permission){this.voice_permission = voice_permission;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
