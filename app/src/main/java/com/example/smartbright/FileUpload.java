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

import static com.example.smartbright.Definitions.DBG;

public class FileUpload {
    private static final String TAG = FileUpload.class.getSimpleName();

    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final StorageReference rootStorageRef = storage.getReference();

    public static void uploadLog(String filepath, String filename) {
        String uid = UniqueIDManager.getID();
        StorageReference logStorageRef = rootStorageRef.child("logs").child(uid).child(filename);

        Uri uri = Uri.fromFile(new File(filepath));
        UploadTask uploadTask = logStorageRef.putFile(uri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                if (DBG) Log.e(TAG, "Failed upload " + filename);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (DBG) Log.d(TAG, "Successful upload " + filename);
                boolean deleteResult = new File(uri.getPath()).delete();
                if (DBG) Log.d(TAG, "Deleted? " + deleteResult + ", " + filename);
            }
        });
    }
}
