package com.example.photoroulette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Lobby {
    private String name;
    private List<String> players;
    private String lobbyReference;


    public Lobby() {
        // Required empty constructor for Firebase
    }

    public Lobby(String lobbyReference, String name, List<String> players) {
        this.name = name;
        this.lobbyReference = lobbyReference;
        this.players = players;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name){ this.name = name; }
    public List<String> getPlayers() {
        return players;
    }

    public void addPlayer(String player) {
        this.players.add(player);
    }

}


