package com.projectgame.projectgame;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
public class ThirdPag extends AppCompatActivity {
    //ATRIBUTOS
    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LAYOUT
        setContentView(R.layout.activity_third_pag);

        //INICIALIZAR BOTONES
        Button buttonIniciar = findViewById(R.id.buttonIniciar);     //Iniciar juego
        Button buttonHistorico = findViewById(R.id.buttonHistorico); //Historico juego

        //OBTENER - Nombre Usuario
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        //BOTON - INICIAR JUEGO
        buttonIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //MOVER A LA SIGUIENTE PAGINA {gamePag}
                Intent intent = new Intent(ThirdPag.this, gamePag.class);
                    //Pasar el nombre de usuario a la siguiente actividad
                    intent.putExtra("nombreUsuario", nombreUsuario);
                    startActivity(intent);
            }
        });

        //BOTON - HISTORICO
        buttonHistorico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //MOVER A LA SIGUIENTE PAGINA {HistoricalPag}
                Intent intent = new Intent(ThirdPag.this, HistoricalPag.class);
                intent.putExtra("nombreUsuario", nombreUsuario);
                startActivity(intent);
            }
        });
    }
}