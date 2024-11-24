package com.projectgame.projectgame;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.MediaPlayer;
import android.widget.Button;

public class GameActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Inicializar el MediaPlayer con la música oficial
        mediaPlayer = MediaPlayer.create(this, R.raw.musica_oficial);
        mediaPlayer.setLooping(true); // Reproducir en bucle

        // Botón para activar/desactivar la música
        Button toggleMusicButton = findViewById(R.id.toggleMusicButton);
        toggleMusicButton.setOnClickListener(v -> toggleMusic());
    }

    private void toggleMusic() {
        if (isMusicPlaying) {
            mediaPlayer.pause(); // Pausar la música
        } else {
            mediaPlayer.start(); // Reproducir la música
        }
        isMusicPlaying = !isMusicPlaying; // Cambiar el estado
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Liberar recursos al destruir la actividad
            mediaPlayer = null;
        }
    }

    private static final int PICK_AUDIO_REQUEST = 1;

    // Método para abrir el selector de archivos
    private void selectAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    // Manejar el resultado de la selección de archivos
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            // Aquí puedes usar el URI para crear un nuevo MediaPlayer
            mediaPlayer = MediaPlayer.create(this, audioUri);
            mediaPlayer.setLooping(true);
        }
    }


}