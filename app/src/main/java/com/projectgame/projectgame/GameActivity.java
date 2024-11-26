package com.projectgame.projectgame;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private int diceRollSound; // ID del sonido de los dados
    private boolean isMusicPlaying = true; // La música inicia al arrancar la actividad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Inicializar música de fondo
        initializeMediaPlayer();

        // Inicializar efectos de sonido
        initializeSoundPool();

        // Configurar botón para activar/desactivar música
        Button toggleMusicButton = findViewById(R.id.toggleMusicButton);
        toggleMusicButton.setOnClickListener(v -> toggleMusic());

        // Configurar botón para lanzar los dados
        Button rollDiceButton = findViewById(R.id.rollDiceButton);
        rollDiceButton.setOnClickListener(v -> playDiceRollSound());
    }

    private void initializeMediaPlayer() {
        // Liberar cualquier instancia previa del MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // Crear una nueva instancia del MediaPlayer con la música oficial
        mediaPlayer = MediaPlayer.create(this, R.raw.musica_oficial);
        mediaPlayer.setLooping(true);
        mediaPlayer.start(); // Comienza la música al iniciar
        isMusicPlaying = true; // Actualiza el estado inicial
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
        diceRollSound = soundPool.load(this, R.raw.dado_sonido, 1);
    }

    private void toggleMusic() {
        if (isMusicPlaying) {
            mediaPlayer.pause(); // Pausa la música
        } else {
            mediaPlayer.start(); // Reanuda la música
        }
        isMusicPlaying = !isMusicPlaying; // Alterna el estado
    }

    private void playDiceRollSound() {
        if (soundPool != null) {
            soundPool.play(diceRollSound, 1.0f, 1.0f, 1, 0, 1.0f); // Reproduce el sonido del dado
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Liberar MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Liberar SoundPool
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
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
            if (mediaPlayer != null) {
                mediaPlayer.release(); // Libera el MediaPlayer anterior antes de crear uno nuevo
            }
            mediaPlayer = MediaPlayer.create(this, audioUri);
            mediaPlayer.setLooping(true);
            mediaPlayer.start(); // Reproduce el archivo seleccionado
            isMusicPlaying = true; // Actualiza el estado
        }
    }
}
