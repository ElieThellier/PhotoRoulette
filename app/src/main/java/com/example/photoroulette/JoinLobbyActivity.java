package com.example.photoroulette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinLobbyActivity extends AppCompatActivity {

    private EditText lobbyReferenceEditText;
    private EditText playerNameEditText;
    private Button joinLobbyButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_lobby);

        lobbyReferenceEditText = findViewById(R.id.lobby_reference_edit_text);
        playerNameEditText = findViewById(R.id.player_name_edit_text);
        joinLobbyButton = findViewById(R.id.join_lobby_button);
        backButton = findViewById(R.id.back_button);

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
                        joinLobby();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                joinLobbyButton.startAnimation(animation);
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

    private void joinLobby() {
        // Get the lobby Reference entered by the user
        String lobbyReference = lobbyReferenceEditText.getText().toString();

        // Get the name of the player who wants to join the lobby
        String playerName = playerNameEditText.getText().toString();

        // Check if the lobby Reference exists in the database
        if (!lobbyReference.isEmpty() && !playerName.isEmpty()) {
            FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // The lobby exists, so add the player to the lobby
                        Lobby lobby = dataSnapshot.getValue(Lobby.class);
                        lobby.addPlayer(playerName);
                        FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).setValue(lobby);

                        // Start the LobbyActivity with the lobby ID and the player's name as extras
                        Intent intent = new Intent(JoinLobbyActivity.this, LobbyActivity.class);
                        intent.putExtra("lobbyReference", lobbyReference);
                        intent.putExtra("playerName", playerName);
                        startActivity(intent);

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                        // Finish the Activity
                        finish();
                    } else {
                        // The lobby doesn't exist, so show an error message to the user
                        Toast.makeText(JoinLobbyActivity.this, "Lobby not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors here
                }
            });
        }
    }
}
