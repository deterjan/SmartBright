package com.example.smartbright;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import static com.example.smartbright.Definitions.TAG;

public class FileUpload {
    private static FirebaseStorage storage = FirebaseStorage.getInstance();
    private static StorageReference rootStorageRef = storage.getReference();
    private static StorageMetadata metadata;

    public static void uploadLog(String filepath, String filename) {
        StorageReference logStorageRef = rootStorageRef.child("logs").child(filename);

        Uri file = Uri.fromFile(new File(filepath));
        UploadTask uploadTask = logStorageRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Failed upload " + filename);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Successful upload " + filename);
            }
        });
    }


}
