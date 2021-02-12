package com.example.smartbright;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LoggerCSV implements Logger {

    // Vars
    public String FILENAME;
    private List<String> keys;
    private static FileOutputStream outputStream;
    final private static Object fileLock = new Object();

    private static final byte[] SPACE  = " ".getBytes();
    private static final byte[] NEWLINE= "\n".getBytes();
    private static final byte[] COMMA= ",".getBytes();

    public LoggerCSV(Context c, List<String> keys) {
        // Set the keys
        setKeys(keys);

        // Create first file
        createFile(c);
    }

    public String getTime(){
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }

    public void createFile(Context c){

        // Set filename
        FILENAME = "smartbright_" + new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(new Date()) + ".log";

        // Create file
        synchronized (fileLock) {
            try {
                outputStream = c.openFileOutput(FILENAME, Context.MODE_APPEND);
                appendHeader();

                Log.d(Definitions.TAG, "Log File:" + FILENAME + " created");
            } catch (Exception e) {
                Log.e(Definitions.TAG, "Can't open file " + FILENAME + ":" + e);

            }
        }

    }

    public void appendHeader(){

        // Init line
        StringBuilder line = new StringBuilder();

        line.append("time");
        line.append(",");

        for (String key: keys){
            line.append(key);
            line.append(",");
        }
        // Delete last comma
        line.deleteCharAt(line.length()-1);

        synchronized (fileLock){
            try {
                outputStream.write(line.toString().getBytes());
                outputStream.write(NEWLINE);

            } catch (IOException ioe) {
                Log.e(Definitions.TAG, "ERROR: Can't write header to file: " + ioe);
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