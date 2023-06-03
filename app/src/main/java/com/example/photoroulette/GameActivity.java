package com.example.photoroulette;

import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner playerSpinner;
    private Button submitButton;

    private List<String> playerList;
    private String lobbyReference;
    private String playerName;
    private String chooser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        recyclerView = findViewById(R.id.recycler_view);
        playerSpinner = findViewById(R.id.playerSpinner);
        submitButton = findViewById(R.id.submitButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lobbyReference = extras.getString("lobbyReference");
            playerName = extras.getString("playerName");
            chooser = extras.getString("chooser");
        }

        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).child("currentImage");
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String imageUrl = dataSnapshot.getValue(String.class);
                Uri imageUri = Uri.parse(imageUrl);
                ArrayList<Uri> uriList = new ArrayList<>();
                uriList.add(imageUri);
                recyclerView.setLayoutManager(new LinearLayoutManager(GameActivity.this));
                recyclerView.setAdapter(new MainAdp(uriList));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        // Retrieve the list of players from the database and display them
        FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Lobby lobby = dataSnapshot.getValue(Lobby.class);
                    List<String> players = lobby.getPlayers();

                    playerList = new ArrayList<>(players);

                    // Create an ArrayAdapter for the playerList
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(GameActivity.this, android.R.layout.simple_spinner_item, playerList);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    playerSpinner.setAdapter(spinnerAdapter);
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
                        String selectedPlayer = playerSpinner.getSelectedItem().toString();

                        // Handle the submission logic

                        // Affiche le résultat
                        if (selectedPlayer.equals(chooser)) {
                            Toast.makeText(GameActivity.this, "Bonne réponse !", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GameActivity.this, "Mauvaise réponse.", Toast.LENGTH_SHORT).show();
                        }

                        // Va sur une autre page avec Restart et Quit
                        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                        intent.putExtra("lobbyReference", lobbyReference);
                        intent.putExtra("playerName", playerName);
                        intent.putExtra("chooser", chooser);
                        intent.putExtra("selectedPlayer", selectedPlayer);
                        startActivity(intent);

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        // Finish the Activity
                        finish();
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
