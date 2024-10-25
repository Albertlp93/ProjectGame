package com.projectgame.projectgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SecondPag extends AppCompatActivity {

    private EditText editTextUsuario, editTextContraseña; // EditText para ingresar usuario y contraseña
    private Button buttonStart;                            // Botón para iniciar sesión
    private UserRepository userRepository;                // Repositorio para manejar usuarios
    private TextView textViewCrearCuenta;                 // TextView para el mensaje de crear cuenta
    private Button buttonCrearCuenta;                     // Botón para crear cuenta

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_pag); // Layout correspondiente

        // Inicialización de los campos y botones
        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextContraseña = findViewById(R.id.editTextContraseña);
        buttonStart = findViewById(R.id.buttonStart);
        textViewCrearCuenta = findViewById(R.id.textView5); // Inicializa el TextView para crear cuenta
        buttonCrearCuenta = findViewById(R.id.buttonCrearCuenta); // Inicializa el botón de crear cuenta

        // Inicializar el repositorio de usuarios
        userRepository = new UserRepository(this);

        // Configura el onClickListener para el botón "START"
        buttonStart.setOnClickListener(v -> {
            String usuario = editTextUsuario.getText().toString();    // Obtener el nombre de usuario
            String contraseña = editTextContraseña.getText().toString(); // Obtener la contraseña

            // Verificar si el usuario existe en la base de datos
            userRepository.verificarUsuario(usuario, contraseña)
                    .subscribeOn(Schedulers.io()) // Ejecutar en un hilo de fondo
                    .observeOn(AndroidSchedulers.mainThread()) // Volver al hilo principal para la UI
                    .subscribe(existe -> {
                        if (existe) {
                            // Si el usuario existe, ir al juego
                            Intent intent = new Intent(SecondPag.this, ThirdPag.class); // Clase de la thirdpag
                            startActivity(intent);
                            finish(); // Cierra la actividad actual si ya no la necesitas
                        } else {
                            // Si no existe, mostrar mensaje de error
                            Toast.makeText(SecondPag.this, "Usuario desconocido", Toast.LENGTH_SHORT).show();
                            buttonCrearCuenta.setVisibility(View.VISIBLE); // Mostrar el botón para crear cuenta
                        }
                    }, throwable -> {
                        // Manejar el error de la consulta
                        Toast.makeText(SecondPag.this, "Error en la consulta", Toast.LENGTH_SHORT).show();
                    });
        });

        // Configura el onClickListener para el mensaje de crear cuenta
        textViewCrearCuenta.setOnClickListener(v -> {
            // Ir a la actividad de crear una nueva cuenta
            Intent intent = new Intent(SecondPag.this, CrearCuentaActivity.class);
            startActivity(intent);
        });

        // Configura el onClickListener para el botón "Crear Cuenta"
        buttonCrearCuenta.setOnClickListener(v -> {
            Intent intent = new Intent(SecondPag.this, CrearCuentaActivity.class);
            startActivity(intent);
        });
    }
}
