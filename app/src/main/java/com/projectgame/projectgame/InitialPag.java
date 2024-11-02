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

        //LAYOUT
        setContentView(R.layout.activity_initial_pag);

        //INICIALIZAR BOTONES
        Button buttonStart = findViewById(R.id.button);


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //MOVER A LA SIGUIENTE PAGINA {SecondPag}
                Intent intent = new Intent(InitialPag.this, SecondPag.class);
                startActivity(intent);

            }
        });
    }
}
