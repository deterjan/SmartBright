package com.example.smartbright;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.example.smartbright.Definitions.DBG;

public class FileUpload {
    private static final String TAG = FileUpload.class.getSimpleName();

    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final StorageReference rootStorageRef = storage.getReference();

    private static void compressGzipFile(String file, String gzipFile) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            fis.close();
            if (DBG) Log.v(TAG, "Compressed file:  " + file);
        } catch (Exception e) {
            if (DBG) Log.e(TAG, "Failed to compress file:  " + file);
        }

    }

    public static void uploadLog(String filepath, String filename) {
        String compressedFilepath = filepath + ".gz";
        String compressedFilename = filename + ".gz";

        Log.e(TAG, compressedFilepath);
        compressGzipFile(filepath, compressedFilepath);

        Uri uri = Uri.fromFile(new File(filepath));
        boolean deleteResult = new File(uri.getPath()).delete();
        if (DBG) Log.d(TAG, "Deleted log? " + deleteResult + ", " + filename);

        String uid = UniqueIDManager.getID();
        StorageReference logStorageRef = rootStorageRef.child("logs")
                .child(uid).child(compressedFilename);

        Uri compressedUri = Uri.fromFile(new File(compressedFilepath));
        UploadTask uploadTask = logStorageRef.putFile(compressedUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            if (DBG) Log.e(TAG, "Failed upload " + filename);
        }).addOnSuccessListener(taskSnapshot -> {
            if (DBG) Log.d(TAG, "Successful upload " + filename);
            boolean deleteResult1 = new File(compressedUri.getPath()).delete();
            if (DBG) Log.d(TAG, "Deleted compressed log?  " +
                    deleteResult1 + ", " + compressedFilename);
        });
    }
}
