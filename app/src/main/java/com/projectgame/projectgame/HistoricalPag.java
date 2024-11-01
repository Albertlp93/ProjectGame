package com.projectgame.projectgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HistoricalPag extends AppCompatActivity {

    private BaseDeDatosHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_pag); // Inicialización del layout

        // Inicialización del TextView para mostrar la puntuación
        TextView textViewPuntuacion = findViewById(R.id.textViewPuntuacion);
        dbHelper = new BaseDeDatosHelper(this); // Inicializar el helper de la base de datos

        // Recuperar la puntuación pasada
        int puntuacion = getIntent().getIntExtra("puntuacion", 0);
        textViewPuntuacion.setText("Puntuación: " + puntuacion); // Mostrar la puntuación en el TextView

        // Mostrar puntuaciones de todos los jugadores
        mostrarPuntuaciones();

        // Inicialización del botón volver
        Button buttonVolver = findViewById(R.id.buttonVolver);

        // Botón - VOLVER
        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(HistoricalPag.this, ThirdPag.class); // Cambiar a ThirdPag
            startActivity(intent);
            finish();
        });
    }


    private void mostrarPuntuaciones() {
        // Obtener la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Consulta para obtener todos los usuarios y sus puntuaciones
        Cursor cursor = db.rawQuery("SELECT " + BaseDeDatosHelper.COLUMN_NOMBRE + ", " +
                BaseDeDatosHelper.COLUMN_PUNTUACION + " FROM " +
                BaseDeDatosHelper.TABLE_USUARIOS, null);

        TableLayout tableLayout = findViewById(R.id.tableLayoutResultados); // Obtener el TableLayout

        // Limpiar la tabla antes de agregar nuevas filas
        tableLayout.removeAllViews();

        // Agregar encabezados (opcional)
        TableRow headerRow = new TableRow(this);
        TextView headerNombre = new TextView(this);
        headerNombre.setText("Nombre");
        TextView headerPuntuacion = new TextView(this);
        headerPuntuacion.setText("Puntuación");
        headerRow.addView(headerNombre);
        headerRow.addView(headerPuntuacion);
        tableLayout.addView(headerRow);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex(BaseDeDatosHelper.COLUMN_NOMBRE));
                int puntuacionIndex = cursor.getColumnIndex(BaseDeDatosHelper.COLUMN_PUNTUACION);

                // Verifica que el índice de puntuación no sea -1
                if (puntuacionIndex != -1) {
                    int puntuacion = cursor.getInt(puntuacionIndex);

                    // Crear una nueva fila para la tabla
                    TableRow row = new TableRow(this);
                    TextView nombreTextView = new TextView(this);
                    nombreTextView.setText(nombre);
                    nombreTextView.setPadding(8, 8, 8, 8); // Añadir padding
                    nombreTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                    TextView puntuacionTextView = new TextView(this);
                    puntuacionTextView.setText(String.valueOf(puntuacion));
                    puntuacionTextView.setPadding(8, 8, 8, 8); // Añadir padding
                    puntuacionTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                    // Añadir las vistas a la fila
                    row.addView(nombreTextView);
                    row.addView(puntuacionTextView);

                    // Añadir la fila al TableLayout
                    tableLayout.addView(row);
                }
            } while (cursor.moveToNext());
        } else {
            // Si no hay datos, mostrar un mensaje
            TextView noDataText = new TextView(this);
            noDataText.setText("No hay datos de partidas");
            noDataText.setPadding(16, 16, 16, 16);
            tableLayout.addView(noDataText);
        }

        cursor.close();
        db.close(); // Cerrar la base de datos
    }



}
