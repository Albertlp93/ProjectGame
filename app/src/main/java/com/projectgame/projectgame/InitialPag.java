package com.projectgame.projectgame;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class InitialPag extends AppCompatActivity {

    private MediaPlayer mediaPlayer; // Declarar el MediaPlayer
    private boolean isMusicPlaying = false; // Estado de la música

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LAYOUT
        setContentView(R.layout.activity_initial_pag);

        // INICIALIZAR BOTONES
        Button buttonStart = findViewById(R.id.button);

        // Inicializa el MediaPlayer con la música oficial
        mediaPlayer = MediaPlayer.create(this, R.raw.musica_oficial);
        mediaPlayer.setLooping(true); // Reproducir en bucle
        mediaPlayer.start(); // Comienza a reproducir la música al iniciar la actividad

        // Botón para activar/desactivar la música
        Button buttonToggleMusic = findViewById(R.id.buttonToggleMusic);
        buttonToggleMusic.setOnClickListener(v -> toggleMusic());

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MOVER A LA SIGUIENTE PAGINA {SecondPag}
                Intent intent = new Intent(InitialPag.this, SecondPag.class);
                startActivity(intent);
            }
        });
    }

    private void toggleMusic() {
        if (isMusicPlaying) {
            mediaPlayer.pause(); // Pausar la música
            isMusicPlaying = false;
            ((Button) findViewById(R.id.buttonToggleMusic)).setText("Activar Música"); // Cambia el texto del botón
        } else {
            mediaPlayer.start(); // Reproducir la música
            isMusicPlaying = true;
            ((Button) findViewById(R.id.buttonToggleMusic)).setText("Detener Música"); // Cambia el texto del botón
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Liberar recursos al destruir la actividad
            mediaPlayer = null;
        }
    }
}
