package com.example.photoroulette;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button createLobbyButton;
    private Button joinLobbyButton;
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://photoroulette-43c4f-default-rtdb.europe-west1.firebasedatabase.app/");
    private static FirebaseStorage storage = FirebaseStorage.getInstance("gs://photoroulette-43c4f.appspot.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createLobbyButton = findViewById(R.id.create_lobby_button);
        joinLobbyButton = findViewById(R.id.join_lobby_button);

        createLobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // L'animation a commencé, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // L'animation est terminée, démarrer la nouvelle activité
                        Intent intent = new Intent(MainActivity.this, CreateLobbyActivity.class);
                        startActivity(intent);

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                createLobbyButton.startAnimation(animation);
            }
        });

        joinLobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // L'animation a commencé, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // L'animation est terminée, démarrer la nouvelle activité
                        Intent intent = new Intent(MainActivity.this, JoinLobbyActivity.class);
                        startActivity(intent);

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                joinLobbyButton.startAnimation(animation);
            }
        });

        // Check lobbies and delete empty ones
        checkAndDeleteEmptyLobbies();
        deleteUnusedFiles();
    }

    private void checkAndDeleteEmptyLobbies() {
        DatabaseReference lobbiesRef = FirebaseDatabase.getInstance().getReference().child("lobbies");
        lobbiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot lobbySnapshot : dataSnapshot.getChildren()) {
                    String lobbyReference = lobbySnapshot.getKey();
                    DatabaseReference playersRef = lobbiesRef.child(lobbyReference).child("players");
                    playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot playersSnapshot) {
                            if (!playersSnapshot.exists()) {
                                // Lobby has no players, delete it
                                Log.w("pas","player");
                                lobbySnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any errors that occur during data retrieval
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during data retrieval
            }
        });
    }
    private void deleteUnusedFiles() {
        // Get Firebase Storage reference
        StorageReference storageRef = storage.getReference();

        // Get Firebase Realtime Database reference to the "lobbies" node
        DatabaseReference databaseRef = database.getReference().child("lobbies");

        List<StorageReference> foldersToDelete = new ArrayList<>();

        // Get a list of all files and folders in Firebase Storage
        storageRef.listAll().addOnSuccessListener(listResult -> {
            // Retrieve all existing lobbies from the database
            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> existingLobbies = new ArrayList<>();

                    for (DataSnapshot lobbySnapshot : dataSnapshot.getChildren()) {
                        existingLobbies.add(lobbySnapshot.getKey());
                    }

                    // Iterate through each folder
                    for (StorageReference item : listResult.getPrefixes()) {
                        // Extract the folder name
                        String folderName = item.getName();

                        // Check if the folder name exists in the existing lobbies list
                        if (!existingLobbies.contains(folderName)) {
                            foldersToDelete.add(item);
                            Log.d("DeleteUnusedFiles", "Folder to delete: " + folderName);
                        }
                    }

                    // Delete the folders
                    deleteFolders(storageRef, foldersToDelete);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors that occur during data retrieval
                }
            });

        }).addOnFailureListener(e -> {
            // Handle any errors that occur while listing files
            Log.w("DeleteUnusedFiles", "Failed to list files and folders: " + e.getMessage());
        });
    }

    private void deleteFolders(StorageReference storageRef, List<StorageReference> foldersToDelete) {
        // Delete each folder in the list
        for (StorageReference folderRef : foldersToDelete) {
            Log.d("DeleteFolders", "Folder to delete: " + folderRef.getName());

            // Delete the contents of the folder recursively
            deleteFolderContents(folderRef);

            // Delete the folder
            folderRef.delete().addOnSuccessListener(aVoid -> {
                // Folder deleted successfully
                Log.d("DeleteFolders", "Folder deleted: " + folderRef.getName());
            }).addOnFailureListener(exception -> {
                // Folder deletion failed
                Log.e("DeleteFolders", "Failed to delete folder: " + folderRef.getName(), exception);
            });
        }
    }

    private void deleteFolderContents(StorageReference folderRef) {
        // List all items (files and sub-folders) in the folder
        folderRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> items = listResult.getItems();
            List<StorageReference> prefixes = listResult.getPrefixes();

            // Delete each file in the folder
            for (StorageReference item : items) {
                item.delete().addOnSuccessListener(aVoid -> {
                    // File deleted successfully
                    Log.d("DeleteFolders", "File deleted: " + item.getName());
                }).addOnFailureListener(exception -> {
                    // File deletion failed
                    Log.e("DeleteFolders", "Failed to delete file: " + item.getName(), exception);
                });
            }

            // Delete the contents of each sub-folder recursively
            for (StorageReference prefix : prefixes) {
                deleteFolderContents(prefix);
            }
        }).addOnFailureListener(exception -> {
            // Failed to list folder contents
            Log.e("DeleteFolders", "Failed to list folder contents: " + folderRef.getName(), exception);
        });
    }
}
