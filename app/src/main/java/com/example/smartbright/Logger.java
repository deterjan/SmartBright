package com.example.smartbright;

import android.content.Context;

import java.util.List;
import java.util.Map;

public interface Logger {

    void appendValues(Map<String, String> values);
    void setKeys(List<String> keys);
    void createFile(Context c);
    void closeFile();

    String getHeader();
    String getLine(Map<String, String> values);

}
