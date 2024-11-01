
package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HistoricalPag extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_pag); // Inicialización del layout

        //INICIALIZACION BOTONES
        Button buttonVolver = findViewById(R.id.buttonVolver);

        //Boton - VOLVER
        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoricalPag.this, ThirdPag.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
