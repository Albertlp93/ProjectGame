package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PoolPositionFirebase extends AppCompatActivity {

    private Button buttonVolver; // Botón para volver
    private String nombreUsuario; // Variable para el nombre de usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poolposition_pag_firebase);

        // Obtener el nombre del usuario pasado por Intent
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        // Inicializar botón VOLVER
        buttonVolver = findViewById(R.id.buttonVolver);

        // Configurar botón VOLVER
        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(PoolPositionFirebase.this, ThirdPag.class);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
            finish();
        });
    }
}
