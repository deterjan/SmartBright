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

    private Context context;
    final private static Object fileLock = new Object();

    final private static int MAX_LINES = 500;
    private int lines;

    private static final byte[] SPACE  = " ".getBytes();
    private static final byte[] NEWLINE= "\n".getBytes();
    private static final byte[] COMMA= ",".getBytes();

    public LoggerCSV(Context c, List<String> keys) {
        // Set the keys
        setKeys(keys);

        this.context = c;
        // Create first file
        createFile(c);
    }

    public String getTime(){
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }

    public void createFile(Context c){
        // Set filename
        FILENAME = "smartbright_" + (System.currentTimeMillis()/1000L) + ".log";

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
        synchronized (fileLock){
            try {
                outputStream.write(getHeader().getBytes());
                outputStream.write(NEWLINE);
            } catch (IOException ioe) {
                Log.e(Definitions.TAG, "ERROR: Can't write header to file: " + ioe);
            }
        }
    }

    // Close method
    public void appendValues(Map<String, String> values) {
        synchronized (fileLock){
            try {
                if (lines >= MAX_LINES) {
                    closeFile();
                    FileUpload.uploadLog(
                            "/data/data/com.example.smartbright/files/"+FILENAME, FILENAME);
                    createFile(context);
                    lines = 0;
                }

                outputStream.write(getLine(values).getBytes());
                outputStream.write(NEWLINE);
                lines++;

            } catch (IOException ioe) {
                Log.e(Definitions.TAG, "ERROR: Can't write string to file: " + ioe);
            }
        }

    }

    public void closeFile(){
        try {
            outputStream.close();
        } catch (IOException ioe){
            Log.e(Definitions.TAG, "Can't close file " + FILENAME + ":" + ioe);
        }
    }

    @Override
    public String getHeader() {
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

        return line.toString();
    }

    @Override
    public String getLine(Map<String, String> values) {
        StringBuilder line = new StringBuilder();

        line.append(getTime());
        line.append(",");

        for (String key: keys){
            line.append(values.get(key));
            line.append(",");
        }
        // Delete last comma
        line.deleteCharAt(line.length()-1);

        return line.toString();
    }

    @Override
    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

}
