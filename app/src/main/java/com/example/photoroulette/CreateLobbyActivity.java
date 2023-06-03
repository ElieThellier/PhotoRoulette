package com.example.photoroulette;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreateLobbyActivity extends AppCompatActivity {

    private EditText playerNameEditText;
    private Button createLobbyButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lobby);

        playerNameEditText = findViewById(R.id.player_name_edit_text);
        createLobbyButton = findViewById(R.id.create_lobby_button);
        backButton = findViewById(R.id.back_button);

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
                        createLobby();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                createLobbyButton.startAnimation(animation);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
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
                        onBackPressed();
                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                backButton.startAnimation(animation);
            }
        });
    }

    private void createLobby() {
        // Generate a 4-digit lobby reference number
        Random random = new Random();
        String lobbyReference = String.format("%04d", random.nextInt(10000)); // Generate a String between 0000 and 9999

        FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The lobby already exists in the database
                    createLobby();
                } else {
                    // The element doesn't exist in the database
                    // Get the name of the lobby creator
                    String playerName = playerNameEditText.getText().toString();

                    if(!playerName.isEmpty()){
                        // Create a new Lobby object with the lobby Reference and the player's name and the list of players
                        List<String> players = new ArrayList<>();
                        players.add(playerName);
                        Lobby lobby = new Lobby(lobbyReference, playerName, players, "NO", "");

                        // Save the lobby to the database
                        FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).setValue(lobby);

                        // Start the LobbyActivity with the lobby ID and the creator's name as extras
                        Intent intent = new Intent(CreateLobbyActivity.this, LobbyActivity.class);
                        intent.putExtra("lobbyReference", lobbyReference); // Convert lobbyReference to String
                        intent.putExtra("playerName", playerName);
                        startActivity(intent);

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                        // Finish the Activity
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }
}
