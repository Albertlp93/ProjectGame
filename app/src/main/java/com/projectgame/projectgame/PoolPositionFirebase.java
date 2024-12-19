package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PoolPositionFirebase extends AppCompatActivity {

    private Button buttonVolver; // Botón para volver
    private String nombreUsuario; // Variable para el nombre de usuario
    private FirebaseFirestore db; // Firestore
    private TextView botePremio; // TextView para mostrar el valor del premio
    private TextView poolPositionData; // TextView para los Top 5 jugadores

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poolposition_pag_firebase);

        // Obtener el nombre del usuario pasado por Intent
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        //INICIALIZAR VISTAS
        botePremio = findViewById(R.id.botePremio);
        poolPositionData = findViewById(R.id.poolPositionData);

        //INICIALIZAR BOTONES
        buttonVolver = findViewById(R.id.buttonVolver);

        //INICIALIZAR FIRESTONE
        db = FirebaseFirestore.getInstance();

        //OBTENER DATOS
            //valor del premio
            obtenerValorPremio();
            //Top 5 jugadores
            obtenerTop5Jugadores();

        // Configurar botón VOLVER
        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(PoolPositionFirebase.this, ThirdPag.class);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
            finish();
        });
    }

    //premio desde Firestore
    private void obtenerValorPremio() {
        // Referencia al documento con el campo 'premio'
        DocumentReference premioRef = db.collection("premioActual").document("O8pIDi42aYUwBU3gMb58");

        premioRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener el valor del campo 'premio'
                        Long premio = documentSnapshot.getLong("premio");
                        if (premio != null) {
                            botePremio.setText("Bote del Premio: $" + premio);
                        } else {
                            botePremio.setText("Bote del Premio: No disponible");
                        }
                    } else {
                        botePremio.setText("Bote del Premio: No encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener el valor del premio", e);
                    Toast.makeText(this, "Error al cargar el premio", Toast.LENGTH_SHORT).show();
                });
    }

    //obtener el Top 5 jugadores ordenados por puntuación
    private void obtenerTop5Jugadores() {
        db.collection("usuarios")
                .orderBy("puntuacion", Query.Direction.DESCENDING) // Ordenar por puntuación descendente
                .limit(5) // Limitar a los 5 mejores
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    StringBuilder top5 = new StringBuilder();
                    for (var document : querySnapshot.getDocuments()) {
                        String nombre = document.getString("nombre");
                        Long puntuacion = document.getLong("puntuacion");

                        // Formatear la información del jugador
                        if (nombre != null && puntuacion != null) {
                            top5.append(nombre).append(": ").append(puntuacion).append(" puntos\n");
                        }
                    }
                    // Mostrar solo los datos de jugadores sin mensaje adicional
                    poolPositionData.setText(top5.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener el Top 5 jugadores", e);
                    Toast.makeText(this, "Error al cargar los jugadores", Toast.LENGTH_SHORT).show();
                });
    }
};
