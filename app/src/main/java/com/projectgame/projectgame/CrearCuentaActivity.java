package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers; // IMPORTACIÓN CORRECTA
import io.reactivex.rxjava3.schedulers.Schedulers; // IMPORTACIÓN CORRECTA

public class CrearCuentaActivity extends AppCompatActivity {
    // ATRIBUTOS
    private EditText editTextNuevoUsuario;
    private EditText editTextNuevaContraseña; // Si decides usar la contraseña
    private Button buttonRegistrar;
    private UserRepository userRepository; // Repositorio para manejar usuarios
    private Button buttonVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        // INICIALIZAR VISTAS
        editTextNuevoUsuario = findViewById(R.id.editTextNuevoUsuario);
        editTextNuevaContraseña = findViewById(R.id.editTextNuevaContraseña);

        // INICIALIZAR BOTONES
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        buttonVolver = findViewById(R.id.buttonVolver); // Inicializar el botón volver

        // INICIALIZAR REPOSITORIO DE USUARIOS
        userRepository = new UserRepository(this);

        // BOTÓN - registrar el usuario
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreUsuario = editTextNuevoUsuario.getText().toString().trim();
                String pssword = editTextNuevaContraseña.getText().toString().trim(); // Si decides usarla
                String cc_new_user = getString(R.string.cc_new_user);
                String cc_error_register = getString(R.string.cc_error_register);
                String cc_access = getString(R.string.cc_access);

                if (!nombreUsuario.isEmpty() && !pssword.isEmpty()) {
                    // Guardar el usuario en la base de datos de forma asíncrona
                    userRepository.crearUsuario(nombreUsuario, pssword)
                            .subscribeOn(Schedulers.io()) // Ejecutar en un hilo de fondo
                            .observeOn(AndroidSchedulers.mainThread()) // Volver al hilo principal para la UI
                            .subscribe(() -> {
                                Toast.makeText(CrearCuentaActivity.this, cc_new_user, Toast.LENGTH_SHORT).show();
                                // Limpiar los campos
                                editTextNuevoUsuario.setText("");
                                editTextNuevaContraseña.setText(""); // Si decides usar la contraseña
                            }, throwable -> {
                                Toast.makeText(CrearCuentaActivity.this, cc_error_register, Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(CrearCuentaActivity.this, cc_access, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // BOTÓN - INICIO
        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MOVER A LA SIGUIENTE PAGINA {SecondPag}
                Intent intent = new Intent(CrearCuentaActivity.this, SecondPag.class);
                startActivity(intent);
            }
        });
    }
}
