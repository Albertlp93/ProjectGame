package com.projectgame.projectgame;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
public class ThirdPag extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_pag);

        Button buttonIniciar = findViewById(R.id.buttonIniciar);
        //Button buttonConfig = findViewById(R.id.buttonConfig); "Se habilitara para la practica 2
        //Button buttonUser = findViewById(R.id.buttonUser);     "Se habilitara para la practica 2
        Button buttonHistorico = findViewById(R.id.buttonHistorico);

        //Boton - Inicial juego
        buttonIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThirdPag.this, gamePag.class);
                startActivity(intent);
            }
        });

        //Boton - Configuración
        /*
        buttonConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThirdPag.this, PENDIENTE.class);
                startActivity(intent);
            }
        });
        */

        // Boton - Usuario
        /*
        buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThirdPag.this, PENDIENTE.class);
                startActivity(intent);
            }
        });
        */

        // Boton - Historico
        buttonHistorico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThirdPag.this, HistoricalPag.class); // Cambia `HistoricalPag` si es otro el activity que quieres abrir
                startActivity(intent);
            }
        });
    }
}