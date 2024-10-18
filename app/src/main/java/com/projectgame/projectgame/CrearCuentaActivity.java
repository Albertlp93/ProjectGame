
package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CrearCuentaActivity extends AppCompatActivity {

    private EditText editTextNuevoUsuario;
    private EditText editTextNuevaContraseña; // Si decides usar la contraseña
    private Button buttonRegistrar;
    private UserRepository userRepository; // Repositorio para manejar usuarios
    private Button buttonVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        // Inicializar vistas
        editTextNuevoUsuario = findViewById(R.id.editTextNuevoUsuario);
        editTextNuevaContraseña = findViewById(R.id.editTextNuevaContraseña);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        buttonVolver = findViewById(R.id.buttonVolver); // Inicializar el botón volver

        // Inicializar el repositorio de usuarios
        userRepository = new UserRepository(this);

        // Configurar el botón para registrar el usuario
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreUsuario = editTextNuevoUsuario.getText().toString().trim();
                String contraseña = editTextNuevaContraseña.getText().toString().trim(); // Si decides usarla

                if (!nombreUsuario.isEmpty() && !contraseña.isEmpty()) {
                    // Guardar el usuario en la base de datos de forma asíncrona
                    userRepository.crearUsuario(nombreUsuario, contraseña)
                            .subscribeOn(Schedulers.io()) // Ejecutar en un hilo de fondo
                            .observeOn(AndroidSchedulers.mainThread()) // Volver al hilo principal para la UI
                            .subscribe(() -> {
                                Toast.makeText(CrearCuentaActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                                // Limpiar los campos
                                editTextNuevoUsuario.setText("");
                                editTextNuevaContraseña.setText(""); // Si decides usar la contraseña
                            }, throwable -> {
                                Toast.makeText(CrearCuentaActivity.this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(CrearCuentaActivity.this, "Por favor ingresa un nombre de usuario y una contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configurar el botón para volver a la pantalla inicial
        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para redirigir a la actividad InitialPag
                Intent intent = new Intent(CrearCuentaActivity.this, SecondPag.class);
                startActivity(intent); // Iniciar la nueva actividad
            }
        });
    }
}