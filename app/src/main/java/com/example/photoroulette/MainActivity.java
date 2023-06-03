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
}
