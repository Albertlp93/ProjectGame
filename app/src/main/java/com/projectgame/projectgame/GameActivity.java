package com.projectgame.projectgame;

import android.os.Bundle;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int diceRollSound; // ID del sonido del dado
    private BackgroundMusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Inicializar música de fondo
        musicManager = BackgroundMusicManager.getInstance();
        musicManager.startMusic(this, R.raw.musica_oficial);

        // Inicializar efectos de sonido
        initializeSoundPool();

        // Configurar botón para lanzar los dados
        Button rollDiceButton = findViewById(R.id.rollDiceButton);
        rollDiceButton.setOnClickListener(v -> playDiceRollSound());

        // Configurar botón para activar/desactivar música
        Button toggleMusicButton = findViewById(R.id.toggleMusicButton);
        toggleMusicButton.setOnClickListener(v -> toggleMusic());
    }

    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        diceRollSound = soundPool.load(this, R.raw.dado_sonido, 1);
    }

    private void playDiceRollSound() {
        if (soundPool != null) {
            soundPool.play(diceRollSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    private void toggleMusic() {
        if (musicManager != null) {
            musicManager.stopMusic();
        }
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

