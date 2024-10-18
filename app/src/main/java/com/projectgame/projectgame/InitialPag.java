package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class InitialPag extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_pag);

        // Referencia al botón por su id
        Button buttonStart = findViewById(R.id.button);

        // Configura el onClickListener para el botón
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear el intent para navegar a SecondPag
                Intent intent = new Intent(InitialPag.this, SecondPag.class);
                startActivity(intent); // Lanza la aanueva actividad
            }
        });
    }
}
