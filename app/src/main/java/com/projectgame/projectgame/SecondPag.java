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
    //ATRIBUTOS
        //BOTONES
        private Button buttonStart;         //Iniciar sesión
        private Button buttonCrearCuenta;   //Crear cuenta

        //CAMPOS
        private UserRepository userRepository;                //Repositorio para manejar usuarios
        private TextView textViewCrearCuenta;                 //Mensaje de crear cuenta
        private EditText editTextUsuario, editTextContraseña; //Ingresar usuario y Contraseña

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String sp_user_unknown = getString(R.string.sp_user_unknown);
        String sp_error_query = getString(R.string.sp_error_query);

        //LAYOUT
        setContentView(R.layout.activity_secondary_pag);

        //INICIALIZAR BOTONES
        buttonStart       = findViewById(R.id.buttonStart);       //Iniciar sesion
        buttonCrearCuenta = findViewById(R.id.buttonCrearCuenta); //Crear cuenta

        //INICIALIZAR CAMPOS
        textViewCrearCuenta = findViewById(R.id.textView5);         //Crear cuenta
        editTextUsuario     = findViewById(R.id.editTextUsuario);   //Ingresar usuario
        editTextContraseña  = findViewById(R.id.editTextContraseña);//Ingresar contraseña


        //INICIAR REPOSITORIO USUARIOS
        userRepository = new UserRepository(this);

        //BOTON - START
        buttonStart.setOnClickListener(v -> {
            String usuario    = editTextUsuario.getText().toString();    // Obtener el nombre de usuario ingresado
            String contraseña = editTextContraseña.getText().toString(); // Obtener la contraseña ingresada

            //Verificar si el usuario existe en la base de datos
            userRepository.verificarUsuario(usuario, contraseña)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(existe -> {
                        //EXISTE USUARIO
                        if (existe) {
                            //MOVER A LA SIGUIENTE PAGINA {ThirdPag}
                            Intent intent = new Intent(SecondPag.this, ThirdPag.class);
                                //Pasar el nombre de usuario y la contraseña la siguiente actividad
                                 intent.putExtra("nombreUsuario", usuario);
                                 intent.putExtra("contraseña", contraseña);
                                 startActivity(intent);
                                 finish();
                        }
                        else {
                            Toast.makeText(SecondPag.this, sp_user_unknown, Toast.LENGTH_SHORT).show();
                            //Mostrar el botón para crear cuenta
                            buttonCrearCuenta.setVisibility(View.VISIBLE);
                        }
                    }, throwable -> {
                        Toast.makeText(SecondPag.this, sp_error_query, Toast.LENGTH_SHORT).show();
                    });
        });

        //TEXTO/BOTON - CREAR CUENTA
        textViewCrearCuenta.setOnClickListener(v -> {

            //MOVER A LA SIGUIENTE PAGINA {CrearCuentaActivity}
            Intent intent = new Intent(SecondPag.this, CrearCuentaActivity.class);
            startActivity(intent);
        });

        //BOTON - CREAR CUENTA
        buttonCrearCuenta.setOnClickListener(v -> {

            //MOVER A LA SIGUIENTE PAGINA {CrearCuentaActivity}
            Intent intent = new Intent(SecondPag.this, CrearCuentaActivity.class);
            startActivity(intent);
        });
    }
}
