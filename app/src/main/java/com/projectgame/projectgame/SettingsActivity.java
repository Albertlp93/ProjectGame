package com.projectgame.projectgame;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button buttonSelectMusic = findViewById(R.id.buttonSelectMusic);
        buttonSelectMusic.setOnClickListener(v -> {
            // Abre el selector de archivos para elegir música
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            // Cargar la música seleccionada
            if (uri != null) {
                InitialPag.mediaPlayer.reset();
                try {
                    InitialPag.mediaPlayer.setDataSource(this, uri);
                    InitialPag.mediaPlayer.prepare();
                    InitialPag.mediaPlayer.start();
                    InitialPag.isMusicPlaying = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
