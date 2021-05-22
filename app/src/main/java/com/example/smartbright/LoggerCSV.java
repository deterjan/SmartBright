package com.example.smartbright;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.smartbright.Definitions.DBG;

public class LoggerCSV implements Logger {
    private static final String TAG = LoggerCSV.class.getSimpleName();

    private static final String LOGS_PATH = "/data/data/com.example.smartbright/files/";
    private String currentLogFilename;
    private List<String> keys;
    private FileOutputStream outputStream;

    private Context context;
    final private Object fileLock = new Object();

    final private static int LOG_MAX_LINES = 1000;
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

    @Override
    public void createFile(Context c){
        synchronized (fileLock) {
            currentLogFilename = "smartbright_" + (System.currentTimeMillis()/1000L) + ".log";
            try {
                outputStream = c.openFileOutput(currentLogFilename, Context.MODE_APPEND);
                appendHeader();
                if (DBG) Log.d(TAG, "Log File:" + currentLogFilename + " created");
            } catch (Exception e) {
                if (DBG) Log.e(TAG, "Can't open file " + currentLogFilename + ":" + e);
            }
        }
    }

    private void appendHeader(){
        synchronized (fileLock){
            try {
                outputStream.write(getHeader().getBytes());
                outputStream.write(NEWLINE);
            } catch (IOException ioe) {
                if (DBG) Log.e(TAG, "ERROR: Can't write header to file: " + ioe);
            }
        }
    }

    private void flushLogs() {
        String[] logsPathFileList = new File(LOGS_PATH).list();
        if (logsPathFileList == null) return;

        for (String fname : logsPathFileList) {
            if (fname.endsWith(".log") && !fname.equals(currentLogFilename)) {
                FileUpload.uploadLog(LOGS_PATH + fname, fname);
            }
        }
    }

    @Override
    public void appendValues(Map<String, String> values) {
        synchronized (fileLock){
            try {
                if (lines >= LOG_MAX_LINES) {
                    closeFile();
                    FileUpload.uploadLog(
                            LOGS_PATH + currentLogFilename, currentLogFilename);

                    // before creating a new log, flush logs previously not uploaded
                    flushLogs();

                    // createFile is also synchronized on fileLock
                    // but a thread can acquire a lock it already owns, so its ok
                    createFile(context);
                    lines = 0;
                }

                outputStream.write(getLine(values).getBytes());
                outputStream.write(NEWLINE);
                lines++;

            } catch (IOException ioe) {
                if (DBG) Log.e(TAG, "ERROR: Can't write string to file: " + ioe);
            }
        }

    }

    @Override
    public void closeFile(){
        synchronized (fileLock) {
            try {
                outputStream.close();
            } catch (IOException ioe){
                if (DBG) Log.e(TAG, "Can't close file " + currentLogFilename + ":" + ioe);
            }
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
