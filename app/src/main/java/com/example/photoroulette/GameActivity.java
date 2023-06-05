package com.example.photoroulette;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.os.Handler;

public class GameActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner playerSpinner;
    private Button submitButton;

    private String lobbyReference;
    private String playerName;
    private String selectedImageUrl;
    private String selectedPlayer;
    private String playerOfImage;
    private ImageView imageView;
    private FirebaseStorage storage;
    private StorageReference imageRef;
    private LinearLayout loadingLayout;
    private TextView loadingTextView;
    private ProgressBar loadingProgressBar;
    private static final int MAX_DOWNLOAD_ATTEMPTS = 100;
    private static final int DOWNLOAD_RETRY_DELAY = 500; // .5 second

    private void downloadAndDisplayImage(StorageReference imageRef, int downloadAttempt) {
        // Show the loading layout while the image is being loaded
        loadingLayout.setVisibility(View.VISIBLE);
        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the image data to a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                // Display the bitmap in the ImageView
                imageView.setImageBitmap(bitmap);

                // Hide the loading layout after the image is loaded
                loadingLayout.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (downloadAttempt < MAX_DOWNLOAD_ATTEMPTS) {
                    // Retry downloading the image after a delay
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            downloadAndDisplayImage(imageRef, downloadAttempt + 1);
                        }
                    }, DOWNLOAD_RETRY_DELAY);
                } else {
                    // Handle the failure after multiple attempts
                    // Display an error message or take any other appropriate action
                    Log.e("RIP", "Failed to download image after multiple attempts");
                    Toast.makeText(GameActivity.this, "Failed to download image after multiple attempts", Toast.LENGTH_SHORT).show();

                    // Hide the loading layout on failure
                    loadingLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Initialize FirebaseApp
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_game);

        storage = FirebaseStorage.getInstance("gs://photoroulette-43c4f.appspot.com");

        //recyclerView = findViewById(R.id.recycler_view);
        playerSpinner = findViewById(R.id.playerSpinner);
        submitButton = findViewById(R.id.submitButton);
        imageView = findViewById(R.id.imageView);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingTextView = findViewById(R.id.loadingTextView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lobbyReference = extras.getString("lobbyReference");
            playerName = extras.getString("playerName");
        }

        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).child("images");
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> imageUrls = new ArrayList<>();
                    List<String> playersToImages = new ArrayList<>();
                    for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                        String imageUrl = imageSnapshot.getValue(String.class);
                        imageUrls.add(imageUrl);
                        playersToImages.add(imageSnapshot.getKey());
                    }

                    // Randomly select an image from the list
                    Random random = new Random();
                    int randomIndex = random.nextInt(imageUrls.size());
                    selectedImageUrl = imageUrls.get(randomIndex);
                    playerOfImage = playersToImages.get(randomIndex);

                    StorageReference storageRef = storage.getReference().child(lobbyReference);
                    StorageReference imageRef = storageRef.child(Uri.parse(selectedImageUrl).getLastPathSegment());
                    downloadAndDisplayImage(imageRef, 1);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).child("players");
                    playersRef.addValueEventListener(new ValueEventListener() {
                        List<String> players = new ArrayList<>();

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                                    String player = playerSnapshot.getValue(String.class);
                                    players.add(player);
                                }
                                // Create an ArrayAdapter for the playerList
                                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(GameActivity.this, android.R.layout.simple_spinner_item, players);
                                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                playerSpinner.setAdapter(spinnerAdapter);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors here
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
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
                        // Get the selected player from the spinner
                        selectedPlayer = playerSpinner.getSelectedItem().toString();

                        // Handle the submission logic

                        // Va sur une autre page avec Restart et Quit
                        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                        intent.putExtra("lobbyReference", lobbyReference);
                        intent.putExtra("playerName", playerName);
                        intent.putExtra("selectedPlayer", selectedPlayer);
                        intent.putExtra("playerOfImage", playerOfImage);
                        startActivity(intent);

                        // Finish the Activity
                        finish();
                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                submitButton.startAnimation(animation);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Remove the player from the lobby
        DatabaseReference lobbyRef = FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference);
        DatabaseReference playersRef = lobbyRef.child("players");

        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String player = playerSnapshot.getValue(String.class);
                    if (player.equals(playerName)) {
                        Log.w("back","player");
                        playerSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // Player removed successfully
                                    // Handle success if needed
                                })
                                .addOnFailureListener(exception -> {
                                    // Player removal failed
                                    // Handle failure if needed
                                });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during data retrieval
            }
        });

        super.onBackPressed();
    }
}
