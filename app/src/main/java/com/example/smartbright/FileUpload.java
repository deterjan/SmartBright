package com.example.smartbright;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.smartbright.Definitions.TAG;

public class FileUpload {
    private static FirebaseStorage storage = FirebaseStorage.getInstance();
    private static StorageReference rootStorageRef = storage.getReference();
    private static  StorageMetadata metadata;
    private static StorageReference logStorageRef = rootStorageRef.child("Arff.log").child("test123");

    public static void uploadTest(byte[] bytes) {
        UploadTask uploadTask = logStorageRef.putBytes(bytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Failed upload");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Successful upload");
            }
        });
    }
}
