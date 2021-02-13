package com.example.smartbright;

import android.content.Context;

import java.util.List;
import java.util.Map;

public interface Logger {

    public void appendValues(Map<String, String> values);
    public void setKeys(List<String> keys);
    public void createFile(Context c);
    public void closeFile();

}
