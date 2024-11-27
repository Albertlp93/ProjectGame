package com.projectgame.projectgame;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int diceRollSound; // ID del sonido del dado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Inicializar efectos de sonido
        initializeSoundPool();

        // Configurar botón para lanzar los dados
        Button rollDiceButton = findViewById(R.id.rollDiceButton);
        rollDiceButton.setOnClickListener(v -> playDiceRollSound());

        // Configurar botón para detener la música
        Button stopMusicButton = findViewById(R.id.toggleMusicButton);
        stopMusicButton.setOnClickListener(v -> toggleMusic());
    }

    private void initializeSoundPool() {
        // Configurar atributos de audio para el SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5) // Máximo número de sonidos simultáneos
                .setAudioAttributes(audioAttributes)
                .build();

        // Cargar el sonido del dado
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                Log.d("GameActivity", "Sonido de dados cargado correctamente");
            } else {
                Log.e("GameActivity", "Error al cargar el sonido de dados");
            }
        });

        diceRollSound = soundPool.load(this, R.raw.dado_sonido, 1);
    }

    private void playDiceRollSound() {
        if (soundPool != null) {
            soundPool.play(diceRollSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    private void toggleMusic() {
        Intent musicServiceIntent = new Intent(this, BackgroundMusicService.class);
        stopService(musicServiceIntent); // Detiene la música
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
