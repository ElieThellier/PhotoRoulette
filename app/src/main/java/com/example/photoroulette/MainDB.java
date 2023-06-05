package com.example.photoroulette;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainDB {

    private static final String TAG = "MainDB";

    private static Context mContext;

    public MainDB(Context context) {
        this.mContext = context;
    }

    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://photoroulette-43c4f-default-rtdb.europe-west1.firebasedatabase.app/");

    private static FirebaseStorage storage = FirebaseStorage.getInstance("gs://photoroulette-43c4f.appspot.com");

    static void uploadFile(Uri uri, String lobbyReference) {
        // Create a reference to "filename"
        StorageReference storageRef = storage.getReference().child(lobbyReference);
        storageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {

                        // Upload the new image
                        StorageReference imageRef = storageRef.child(uri.getLastPathSegment());

                        UploadTask uploadTask = imageRef.putFile(uri);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Image uploaded successfully
                                Log.d(TAG, "Image uploaded successfully");
                            }
                        });

                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Log.e(TAG, "Error while uploading image");
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful listing
                        Log.e(TAG, "Error while listing images");
                    }
                });
    }

    static void writeDB(String string, String lobbyReference, String playerName) {
        DatabaseReference gameRef = database.getReference().child("lobbies").child(lobbyReference).child("images");
        DatabaseReference playerRef = gameRef.child(playerName);
        playerRef.setValue(string);
    }

    static void readDB(String lobbyReference, String playerName) { // utile pour ouvrir une image
        // Read from the database
        DatabaseReference gameRef = database.getReference().child("lobbies").child(lobbyReference).child("images").child(playerName);
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String imageUrl = dataSnapshot.getValue(String.class);
                Uri imageUri = Uri.parse(imageUrl);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
