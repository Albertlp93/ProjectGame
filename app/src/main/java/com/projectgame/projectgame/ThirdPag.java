package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ThirdPag extends AppCompatActivity {
    // ATRIBUTOS
    private String nombreUsuario;
    private String passwordUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_pag);

        // INICIALIZAR BOTONES
        Button buttonIniciar = findViewById(R.id.buttonIniciar);     // Iniciar juego
        Button buttonHistorico = findViewById(R.id.buttonHistorico); // Histórico juego
        Button buttonUserData = findViewById(R.id.buttonUserData);   // Datos usuario

        // OBTENER - Nombre Usuario
        nombreUsuario   = getIntent().getStringExtra("nombreUsuario");
        passwordUsuario = getIntent().getStringExtra("contraseña");

        // Si nombreUsuario es null, asignar un valor por defecto
        if (nombreUsuario == null) {
            nombreUsuario = "UsuarioDesconocido";
        }

        // Si nombreUsuario es null, asignar un valor por defecto
        if (nombreUsuario == null) {
            nombreUsuario = "UsuarioDesconocido";
        }

        // Si nombreUsuario es null, asignar un valor por defecto
        if (nombreUsuario == null) {
            nombreUsuario = "UsuarioDesconocido";
        }

        // BOTON - INICIAR JUEGO
        buttonIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                    Toast.makeText(ThirdPag.this, "El nombre de usuario no está disponible", Toast.LENGTH_SHORT).show();
                    return; // Evita iniciar la siguiente actividad sin nombre de usuario
                }

                // MOVER A LA SIGUIENTE PAGINA {gamePag}
                Intent intent = new Intent(ThirdPag.this, gamePag.class);
                // Pasar el nombre de usuario a la siguiente actividad
                intent.putExtra("nombreUsuario", nombreUsuario);
                intent.putExtra("contraseña", passwordUsuario);
                startActivity(intent);
            }
        });

        // BOTON - HISTORICO
        buttonHistorico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                    Toast.makeText(ThirdPag.this, "El nombre de usuario no está disponible", Toast.LENGTH_SHORT).show();
                    return;
                }

                // MOVER A LA SIGUIENTE PAGINA {HistoricalPag}
                Intent intent = new Intent(ThirdPag.this, HistoricalPag.class);
                intent.putExtra("nombreUsuario", nombreUsuario);
                intent.putExtra("contraseña", passwordUsuario);
                startActivity(intent);
            }
        });

        // BOTON - DATOS USUARIO
        buttonUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                    Toast.makeText(ThirdPag.this, "El nombre de usuario no está disponible", Toast.LENGTH_SHORT).show();
                    return;
                }

                // MOVER A LA SIGUIENTE PAGINA {UserDataPag}
                Intent intent = new Intent(ThirdPag.this, UserDataPagFirebase.class);
                intent.putExtra("nombreUsuario", nombreUsuario);
                intent.putExtra("contraseña", passwordUsuario);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Detener la música al entrar en esta actividad
        if (InitialPag.mediaPlayer != null) {
            InitialPag.mediaPlayer.pause(); // Pausar la música
            InitialPag.isMusicPlaying = false; // Actualiza el estado de la música
        }
    }
}
