package com.projectgame.projectgame;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton; // Cambia a ImageButton
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class InitialPag extends AppCompatActivity {

    public static MediaPlayer mediaPlayer; // Declarar el MediaPlayer como estático
    public static boolean isMusicPlaying = false; // Estado de la música como estático

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

        // Botón para activar/desactivar la música (cambiado a ImageButton)
        ImageButton buttonToggleMusic = findViewById(R.id.buttonToggleMusic);
        buttonToggleMusic.setOnClickListener(v -> toggleMusic());

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MOVER A LA SIGUIENTE PAGINA {SecondPag}
                Intent intent = new Intent(InitialPag.this, SecondPagFirebase.class);
                startActivity(intent);
            }
        });

        ImageButton buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(InitialPag.this, SettingsActivity.class);
            startActivity(intent);
        });

    }

    private void toggleMusic() {
        if (isMusicPlaying) {
            mediaPlayer.pause(); // Pausar la música
            isMusicPlaying = false;
            ((ImageButton) findViewById(R.id.buttonToggleMusic)).setImageResource(R.drawable.ic_volume_off); // Cambia el icono
        }
        else {
            mediaPlayer.start(); // Reproducir la música
            isMusicPlaying = true;
            ((ImageButton) findViewById(R.id.buttonToggleMusic)).setImageResource(R.drawable.ic_volume_up); // Cambia el icono
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isMusicPlaying) {
            mediaPlayer.pause(); // Pausa la música cuando la actividad se detiene
            isMusicPlaying = false; // Actualiza el estado de la música
            ((ImageButton) findViewById(R.id.buttonToggleMusic)).setImageResource(R.drawable.ic_volume_off); // Cambia el icono
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
