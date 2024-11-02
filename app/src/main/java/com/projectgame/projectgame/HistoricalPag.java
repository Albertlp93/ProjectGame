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

    //ATRIBUTOS
    private BaseDeDatosHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LAYOUT
        setContentView(R.layout.activity_historical_pag);

        //INICIALIZACIONES
        dbHelper = new BaseDeDatosHelper(this);
        int puntuacion = getIntent().getIntExtra("puntuacion", 0);
        //INICIALIZAR BOTONES
        Button buttonVolver = findViewById(R.id.buttonVolver);

        //INICIALIZAR CAMPOS
        TextView textViewPuntuacion = findViewById(R.id.textViewPuntuacion);
        textViewPuntuacion.setText("Puntuación: " + puntuacion); //Mostrar la puntuación

        //Puntuaciones de los jugadores
        mostrarPuntuaciones();


        //BOTON - VOLVER
        buttonVolver.setOnClickListener(v -> {

            //MOVER A LA SIGUIENTE PAGINA {ThirdPag}
            Intent intent = new Intent(HistoricalPag.this, ThirdPag.class);
            startActivity(intent);
            finish();
        });
    }

    //METODO - MOSTRAR PUNTUACIONES
    private void mostrarPuntuaciones() {
        //Obtener la base de datos en modo lectura
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Obtener usuarios y puntuaciones
        Cursor cursor = db.rawQuery("SELECT " + BaseDeDatosHelper.COLUMN_NOMBRE + ", " +
                BaseDeDatosHelper.COLUMN_PUNTUACION + " FROM " +
                BaseDeDatosHelper.TABLE_USUARIOS, null);

        TableLayout tableLayout = findViewById(R.id.tableLayoutResultados);
        tableLayout.removeAllViews();

        //Agregar encabezados
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
            } while (cursor.moveToNext());
        }
        else {
            TextView noDataText = new TextView(this);
            noDataText.setText("No hay datos de partidas");
            noDataText.setPadding(16, 16, 16, 16);
            tableLayout.addView(noDataText);
        }

        cursor.close();
        db.close();
    }



}
