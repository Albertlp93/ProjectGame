package com.projectgame.projectgame;

import android.os.Bundle;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int diceRollSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //INIICIALIZAR MUSICAS Y SONIDOS
        BackgroundMusicManager.getInstance().startMusic(this, R.raw.musica_oficial);
        initializeSoundPool();

        //INCILIZAR BOTONES
        Button toggleMusicButton = findViewById(R.id.toggleMusicButton); // Activar/desactivar musica
        toggleMusicButton.setOnClickListener(v -> toggleMusic());
        Button rollDiceButton = findViewById(R.id.rollDiceButton);// Configurar botón para lanzar los dados
        rollDiceButton.setOnClickListener(v -> playDiceRollSound());
    }

    //METODO - INICIALIZAR SONIDOS DADOS
    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Cargar el sonido del dado
        diceRollSound = soundPool.load(this, R.raw.dado_sonido, 1);
    }

    //METODO - ACTIVAR/DESACTIVAR MUSICA
    private void toggleMusic() {
        BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
        if (musicManager.isPlaying()) {
            musicManager.pauseMusic();
        } else {
            musicManager.resumeMusic();
        }
    }

    //METODO - SONIDO AL LANZAR DADOS
    private void playDiceRollSound() {
        if (soundPool != null && diceRollSound != 0) {
            BackgroundMusicManager.getInstance().pauseMusic();
            soundPool.play(diceRollSound, 1.0f, 1.0f, 1, 0, 1.0f);
            new android.os.Handler().postDelayed(() -> {
                BackgroundMusicManager.getInstance().resumeMusic();
            }, 500); // 500 ms
        }
    }


    //METODO - CIERRE SONDIOS PARA LIBERAR MEMORIA
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BackgroundMusicManager.getInstance().stopMusic();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
