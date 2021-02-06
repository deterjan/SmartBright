package com.example.smartbright;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.icu.lang.UCharacter.WordBreak.NEWLINE;

public class LoggerCSV implements Logger {

    // Vars
    public String FILENAME;
    private List<String> keys;
    private static FileOutputStream outputStream;
    final private static Object fileLock = new Object();

    public LoggerCSV(Context c) {
        // Create first file
        createFile(c);
    }

    public String getTime(){
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }

    public void createFile(Context c){

        // Set filename
        FILENAME = "smartbright_" + getTime() + ".log";

        // Create file
        synchronized (fileLock) {
            try {
                outputStream = c.openFileOutput(FILENAME, Context.MODE_APPEND);
                Log.d(Definitions.TAG, "Log File:" + FILENAME + " created");
            } catch (Exception e) {
                Log.e(Definitions.TAG, "Can't open file " + FILENAME + ":" + e);

            }
        }

    }

    // Close method
    public void close(){
        try {
            outputStream.close();
        } catch (IOException ioe){
            Log.e(Definitions.TAG, "Can't close file " + FILENAME + ":" + ioe);
        }
    }


    @Override
    public void appendValues(Map<String, String> values) {
        // Init line
        StringBuilder line = new StringBuilder();

        line.append(getTime());
        line.append(",");

        for (String key: keys){
            line.append(values.get(key));
            line.append(",");
        }
        // Delete last comma
        line.deleteCharAt(line.length()-1);

        synchronized (fileLock){
            try {
                outputStream.write(line.toString().getBytes());
                outputStream.write(NEWLINE);

            } catch (IOException ioe) {
                Log.e(Definitions.TAG, "ERROR: Can't write string to file: " + ioe);
            }
        }

    }

    @Override
    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

}
