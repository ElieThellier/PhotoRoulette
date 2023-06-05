package com.example.photoroulette;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResultActivity extends AppCompatActivity {

    private Button restartButton;
    private Button quitButton;

    private String lobbyReference;
    private String playerName;
    private String selectedPlayer;
    private String resultText = "";
    private String playerOfImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        restartButton = findViewById(R.id.restartButton);
        quitButton = findViewById(R.id.quitButton);
        TextView resultTextView = findViewById(R.id.resultTextView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lobbyReference = extras.getString("lobbyReference");
            playerName = extras.getString("playerName");
            selectedPlayer = extras.getString("selectedPlayer");
            playerOfImage = extras.getString("playerOfImage");
        }

        if (selectedPlayer.equals(playerOfImage)) {
            resultText = "Vrai !\n\nC'était bien la photo de '" + playerOfImage+"'";
        } else {
            resultText = "Faux !\n\nC'était la photo de '" + playerOfImage+"'";
        }
        resultTextView.setText(resultText);
        restartButton.setOnClickListener(new View.OnClickListener() {
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

                        // Start the ChooseActivity
                        Intent intent = new Intent(ResultActivity.this, LobbyActivity.class);
                        intent.putExtra("lobbyReference", lobbyReference);
                        intent.putExtra("playerName", playerName);
                        startActivity(intent);

                        // Finish the ResultActivity
                        finish();

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                restartButton.startAnimation(animation);
            }
        });
        quitButton.setOnClickListener(new View.OnClickListener() {
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
                        // Remove the player from the lobby
                        DatabaseReference lobbyRef = FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference);
                        DatabaseReference playersRef = lobbyRef.child("players");

                        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                                    String player = playerSnapshot.getValue(String.class);
                                    if (player.equals(playerName)) {
                                        Log.w("suppr","player");
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

                        // Quit the application
                        finishAffinity();

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                quitButton.startAnimation(animation);
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