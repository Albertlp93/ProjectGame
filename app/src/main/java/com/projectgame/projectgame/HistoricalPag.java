package com.projectgame.projectgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HistoricalPag extends AppCompatActivity {

    // ATRIBUTOS
    private FirebaseFirestore db;
    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LAYOUT
        setContentView(R.layout.activity_historical_pag);

        // INICIALIZACIONES
        db = FirebaseFirestore.getInstance();
        int puntuacion = getIntent().getIntExtra("puntuacion", 0);

        // INICIALIZAR BOTONES
        Button buttonVolver = findViewById(R.id.buttonVolver);

        // INICIALIZAR CAMPOS
        TextView textViewPuntuacion = findViewById(R.id.textViewPuntuacion);
        textViewPuntuacion.setText(String.format(getString(R.string.puntuacion_label), puntuacion)); // Mostrar la puntuación

        // Puntuaciones de los jugadores
        mostrarPuntuaciones();

        // OBTENER - Nombre Usuario
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        // BOTON - VOLVER
        buttonVolver.setOnClickListener(v -> {
            // MOVER A LA SIGUIENTE PAGINA {ThirdPag}
            Intent intent = new Intent(HistoricalPag.this, ThirdPag.class);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
            finish();
        });
    }

    // METODO - MOSTRAR PUNTUACIONES
    private void mostrarPuntuaciones() {
        String hp_name = getString(R.string.hp_name);
        String hp_score = getString(R.string.hp_score);
        String hp_no_data = getString(R.string.hp_no_data);

        TableLayout tableLayout = findViewById(R.id.tableLayoutResultados);
        tableLayout.removeAllViews();

        // Agregar encabezados
        TableRow headerRow = new TableRow(this);
        TextView headerNombre = new TextView(this);
        headerNombre.setText(hp_name);
        TextView headerPuntuacion = new TextView(this);
        headerPuntuacion.setText(hp_score);
        headerRow.addView(headerNombre);
        headerRow.addView(headerPuntuacion);
        tableLayout.addView(headerRow);

        // Obtener puntuaciones de Firestore
        // Obtener puntuaciones de Firestore
        db.collection("usuarios")
                .orderBy("puntuacion", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (var document : queryDocumentSnapshots.getDocuments()) {
                            String nombre = document.getString("nombre");
                            Long puntuacionLong = document.getLong("puntuacion");
                            int puntuacion = puntuacionLong != null ? puntuacionLong.intValue() : 0;

                            // Crear una nueva fila para la tabla
                            TableRow row = new TableRow(this);
                            TextView nombreTextView = new TextView(this);
                            nombreTextView.setText(nombre);
                            nombreTextView.setPadding(8, 8, 8, 8);
                            nombreTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                            TextView puntuacionTextView = new TextView(this);
                            puntuacionTextView.setText(String.valueOf(puntuacion));
                            puntuacionTextView.setPadding(8, 8, 8, 8);
                            puntuacionTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                            // Añadir las vistas a la fila
                            row.addView(nombreTextView);
                            row.addView(puntuacionTextView);

                            // Añadir la fila al TableLayout
                            tableLayout.addView(row);
                        }
                    } else {
                        TextView noDataText = new TextView(this);
                        noDataText.setText(hp_no_data);
                        noDataText.setPadding(16, 16, 16, 16);
                        tableLayout.addView(noDataText);
                    }
                })
                .addOnFailureListener(e -> {
                    TextView noDataText = new TextView(this);
                    noDataText.setText(hp_no_data);
                    noDataText.setPadding(16, 16, 16, 16);
                    tableLayout.addView(noDataText);
                });

    }
}
