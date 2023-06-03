package com.example.photoroulette;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Random;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ChooseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private MainDB mainDB;
    private static final int REQUEST_IMAGE_PICKER = 100;

    private RecyclerView recyclerView;
    private Button pickButton;
    private Button randomButton;
    private Button sendButton;
    private TextView statusText;
    private LinearLayout waitingRoomLayout;
    private LinearLayout chooserLayout;

    private ArrayList<Uri> imageList = new ArrayList<>();
    private Uri selectedImage = null;
    private String lobbyReference;
    private String playerName;
    String chooser = "";
    private boolean isLoopRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        // Create an instance of MainDB and pass the context (GameActivity.this)
        mainDB = new MainDB(ChooseActivity.this);

        recyclerView = findViewById(R.id.recycler_view);
        pickButton = findViewById(R.id.bt_pick);
        randomButton = findViewById(R.id.bt_random);
        sendButton = findViewById(R.id.bt_send);
        statusText = findViewById(R.id.statusText);
        waitingRoomLayout = findViewById(R.id.waitingRoomLayout);
        chooserLayout = findViewById(R.id.chooserLayout);

        // Get the lobbyReference and playerName from the extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lobbyReference = extras.getString("lobbyReference");
            playerName = extras.getString("playerName");
            chooser = extras.getString("chooser");
        }

        if(playerName.equals(chooser)){
            waitingRoomLayout.setVisibility(View.GONE);
            chooserLayout.setVisibility(View.VISIBLE);
        } else{
            statusText.setText("En attente de "+chooser+".\nIl doit choisir une photo.");
        }

        final Handler handler = new Handler();
        final int delay = 100; // Temps en millisecondes entre chaque mise à jour

        // Créez votre Runnable pour la boucle de mise à jour
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Lobby lobby = dataSnapshot.getValue(Lobby.class);
                            String chose = lobby.getPhotoChose();
                            if (chose.equals("YES")) {
                                Log.w("dd", "d");
                                isLoopRunning = false;
                                // Start the GameActivity with the lobby ID and the player's name as extras
                                Intent intent = new Intent(ChooseActivity.this, GameActivity.class);
                                intent.putExtra("lobbyReference", lobbyReference);
                                intent.putExtra("playerName", playerName);
                                intent.putExtra("chooser", chooser);
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
                if (isLoopRunning) {
                    handler.postDelayed(this, delay);
                }
            }
        };

        // Planifiez la première exécution de la boucle de mise à jour
        handler.postDelayed(updateRunnable, delay);

        pickButton.setOnClickListener(new View.OnClickListener() {
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
                        String[] permissions = {"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE"};
                        if (EasyPermissions.hasPermissions(ChooseActivity.this, permissions)) {
                            openImagePicker();
                        } else {
                            EasyPermissions.requestPermissions(ChooseActivity.this,
                                    "PhotoRoulette a besoin d'accéder à votre caméra et votre espace de stockage", REQUEST_IMAGE_PICKER, permissions);
                        }

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                pickButton.startAnimation(animation);
            }
        });

        randomButton.setOnClickListener(new View.OnClickListener() {
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
                        String[] permissions = {"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE"};
                        if (EasyPermissions.hasPermissions(ChooseActivity.this, permissions)) {
                            showRandomImage();
                        } else {
                            EasyPermissions.requestPermissions(ChooseActivity.this,
                                    "PhotoRoulette a besoin d'accéder à votre caméra et votre espace de stockage", REQUEST_IMAGE_PICKER, permissions);
                        }

                        // Animation de transition pour la nouvelle activité
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                randomButton.startAnimation(animation);
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
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
                        if (selectedImage != null) {
                            mainDB.uploadFile(selectedImage, lobbyReference);
                            mainDB.writeDB(selectedImage.toString(), lobbyReference);
                            FirebaseDatabase.getInstance().getReference().child("lobbies").child(lobbyReference).child("photoChose").setValue("YES");
                            isLoopRunning = false;
                        } else {
                            Toast.makeText(ChooseActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // L'animation est répétée, vous pouvez effectuer des actions supplémentaires si nécessaire
                    }
                });

                sendButton.startAnimation(animation);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            ArrayList<Uri> selectedImages = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
            if (!selectedImages.isEmpty()) {
                selectedImage = selectedImages.get(0);
                imageList.clear();
                imageList.add(selectedImage);
                sendButton.setVisibility(View.VISIBLE);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(new MainAdp(imageList));
            }
        }
    }

    private void openImagePicker() {
        FilePickerBuilder.getInstance()
                .setActivityTitle("Select your photo")
                .setSpan(FilePickerConst.SPAN_TYPE.FOLDER_SPAN, 3)
                .setSpan(FilePickerConst.SPAN_TYPE.DETAIL_SPAN, 4)
                .setMaxCount(1)
                .setSelectedFiles(imageList)
                .pickPhoto(this, REQUEST_IMAGE_PICKER);
    }

    private List<Uri> getImagesFromDevice() {
        List<Uri> images = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        try (Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
                    images.add(imageUri);
                } while (cursor.moveToNext());
            }
        }
        return images;
    }

    private void showRandomImage() {
        List<Uri> allImages = getImagesFromDevice();
        if (!allImages.isEmpty()) {
            int randomIndex = new Random().nextInt(allImages.size());
            Uri randomImage = allImages.get(randomIndex);
            selectedImage = randomImage;
            imageList.clear();
            imageList.add(selectedImage);
            sendButton.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MainAdp(imageList));
        } else {
            Toast.makeText(getApplicationContext(), "No images found on device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_IMAGE_PICKER && perms.size() == 2) {
            openImagePicker();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
        }
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
