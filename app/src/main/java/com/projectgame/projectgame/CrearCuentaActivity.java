package com.projectgame.projectgame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CrearCuentaActivity extends AppCompatActivity {

    private EditText editTextNuevoUsuario;
    private EditText editTextNuevaContraseña; // Si decides usar la contraseña
    private Button buttonRegistrar;
    private BaseDeDatosHelper baseDeDatosHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        // Inicializar vistas
        editTextNuevoUsuario = findViewById(R.id.editTextNuevoUsuario);
        editTextNuevaContraseña = findViewById(R.id.editTextNuevaContraseña);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);

        // Inicializar la base de datos
        baseDeDatosHelper = new BaseDeDatosHelper(this);

        // Configurar el botón para registrar el usuario
        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreUsuario = editTextNuevoUsuario.getText().toString().trim();
                String contraseña = editTextNuevaContraseña.getText().toString().trim(); // Si decides usarla

                if (!nombreUsuario.isEmpty() && !contraseña.isEmpty()) {
                    // Guardar el usuario en la base de datos
                    if (baseDeDatosHelper.crearUsuario(nombreUsuario, contraseña)) {
                        Toast.makeText(CrearCuentaActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                        // Limpiar los campos
                        editTextNuevoUsuario.setText("");
                        editTextNuevaContraseña.setText(""); // Si decides usar la contraseña
                    } else {
                        Toast.makeText(CrearCuentaActivity.this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CrearCuentaActivity.this, "Por favor ingresa un nombre de usuario y una contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
