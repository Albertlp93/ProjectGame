package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SecondPag extends AppCompatActivity {

    private EditText editTextUsuario, editTextContraseña; // EditText para ingresar usuario y contraseña
    private Button buttonStart, buttonCrearCuenta;        // Botones para iniciar sesión y crear cuenta
    private BaseDeDatosHelper dbHelper;                  // Base de datos SQLite

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_pag); // Layout correspondiente

        // Inicialización de los campos y botones
        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextContraseña = findViewById(R.id.editTextContraseña);
        buttonStart = findViewById(R.id.buttonStart);
        buttonCrearCuenta = findViewById(R.id.buttonCrearCuenta);

        // Inicializar el helper de la base de datos
        dbHelper = new BaseDeDatosHelper(this);

        // Configura el onClickListener para el botón "START"
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = editTextUsuario.getText().toString();    // Obtener el nombre de usuario
                String contraseña = editTextContraseña.getText().toString(); // Obtener la contraseña

                // Verificar si el usuario existe en la base de datos
                if (dbHelper.verificarUsuario(usuario, contraseña)) {
                    // Si el usuario existe, ir al juego
                    Intent intent = new Intent(SecondPag.this, gamePag.class); // Clase del juego
                    startActivity(intent);
                    finish(); // Cierra la actividad actual si ya no la necesitas
                } else {
                    // Si no existe, mostrar mensaje de error y permitir crear cuenta
                    Toast.makeText(SecondPag.this, "Usuario desconocido", Toast.LENGTH_SHORT).show();
                    buttonCrearCuenta.setVisibility(View.VISIBLE); // Mostrar el botón para crear cuenta
                }
            }
        });

        // Configura el onClickListener para el botón "Crear Cuenta"
        buttonCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir a la actividad de crear una nueva cuenta
                Intent intent = new Intent(SecondPag.this, CrearCuentaActivity.class);
                startActivity(intent);
            }
        });
    }
}
