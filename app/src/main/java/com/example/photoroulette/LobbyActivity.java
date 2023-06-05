package com.example.photoroulette;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LobbyActivity extends AppCompatActivity {

    private TextView lobbyReferenceTextView;
    private TextView playersTextView;
    private Button startGameButton;
    private String lobbyReference;
    private String playerName;
    private ImageButton backButton;
    ArrayList<String> players = new ArrayList<>();
    String chooser = "";
    private ValueEventListener playersValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        lobbyReferenceTextView = findViewById(R.id.lobby_reference_text_view);
        playersTextView = findViewById(R.id.players_text_view);
        startGameButton = findViewById(R.id.start_game_button);
        backButton = findViewById(R.id.back_button);

        // Get the lobbyReference and playerName from the extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lobbyReference = extras.getString("lobbyReference");
            playerName = extras.getString("playerName");
        }

        // Set the lobbyReference text
        lobbyReferenceTextView.setText("Nom de la salle :\n\n"+lobbyReference);

        playersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Lobby lobby = dataSnapshot.getValue(Lobby.class);
                    List<String> players = lobby.getPlayers();
                    String playersText = "Joueurs :\n\n";
                    for (String player : players) {
                        playersText += "- " + player + "\n";
                    }
                    playersTextView.setText(playersText);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        };

        FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).addValueEventListener(playersValueEventListener);

        startGameButton.setOnClickListener(new View.OnClickListener() {
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
                        DatabaseReference buttonPlayersRef = FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).child("players");
                        buttonPlayersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Handle the retrieved data here
                                if (dataSnapshot.exists()) {
                                    // Retrieve the list from the dataSnapshot
                                    for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                                        String player = playerSnapshot.getValue(String.class);
                                        players.add(player);
                                    }
                                    // Use the retrieved list of players
                                    // chose the player randomly
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(players.size());
                                    chooser = players.get(randomIndex);

                                    // Start the ChooseActivity with the lobbyReference and playerName as extras
                                    Intent intent = new Intent(LobbyActivity.this, ChooseActivity.class);
                                    intent.putExtra("lobbyReference", lobbyReference);
                                    intent.putExtra("playerName", playerName);
                                    intent.putExtra("chooser", chooser);
                                    startActivity(intent);

                                    // Animation de transition pour la nouvelle activité
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    // Finish the Activity
                                    finish();
                                } else {
                                    // Handle the case where the data does not exist
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle any errors that occur during data retrieval
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                startGameButton.startAnimation(animation);
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

    @Override
    public void onBackPressed() {
        // Remove the player from the lobby
        DatabaseReference lobbyRef = FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference);
        DatabaseReference playersRef = lobbyRef.child("players");
        playersRef.removeEventListener(playersValueEventListener);

        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String player = playerSnapshot.getValue(String.class);
                    if (player.equals(playerName)) {
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
