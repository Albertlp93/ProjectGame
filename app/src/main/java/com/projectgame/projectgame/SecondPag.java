package com.projectgame.projectgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;


import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SecondPag extends AppCompatActivity {
    // ATRIBUTOS
    private Button buttonStart;         // Iniciar sesión
    private Button buttonCrearCuenta;   // Crear cuenta
    private SignInButton buttonGoogleSignIn;  // Inicio de sesión con Google

    // CAMPOS
    private UserRepository userRepository;                // Repositorio para manejar usuarios
    private TextView textViewCrearCuenta;                 // Mensaje de crear cuenta
    private EditText editTextUsuario, editTextContraseña; // Ingresar usuario y Contraseña

    private FirebaseAuth firebaseAuth; // Autenticación de Firebase
    private SignInClient signInClient; // Cliente para Google Sign-In
    private static final int RC_SIGN_IN = 1001; // Código de solicitud para el inicio de sesión con Google
    private static final String TAG = "SecondPag";

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        String sp_user_unknown = getString(R.string.sp_user_unknown);
        String sp_error_query = getString(R.string.sp_error_query);

        // LAYOUT
        setContentView(R.layout.activity_secondary_pag);

        // INICIALIZAR BOTONES
        buttonStart = findViewById(R.id.buttonStart);       // Iniciar sesión
        buttonCrearCuenta = findViewById(R.id.buttonCrearCuenta); // Crear cuenta
        buttonGoogleSignIn = findViewById(R.id.signInButtonGoogle); // Iniciar sesión con Google

        // INICIALIZAR CAMPOS
        textViewCrearCuenta = findViewById(R.id.textView5);         // Crear cuenta
        editTextUsuario = findViewById(R.id.editTextUsuario);       // Ingresar usuario
        editTextContraseña = findViewById(R.id.editTextContraseña); // Ingresar contraseña

        // INICIAR REPOSITORIO USUARIOS
        userRepository = new UserRepository(this);

        // INICIALIZAR AUTENTICACIÓN DE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();

        // INICIALIZAR CLIENTE DE INICIO DE SESIÓN CON GOOGLE
        signInClient = Identity.getSignInClient(this);

        // BOTÓN - START
        buttonStart.setOnClickListener(v -> {
            String usuario = editTextUsuario.getText().toString();    // Obtener el nombre de usuario ingresado
            String contraseña = editTextContraseña.getText().toString(); // Obtener la contraseña ingresada

            // Verificar si el usuario existe en la base de datos
            userRepository.verificarUsuario(usuario, contraseña)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(existe -> {
                        // EXISTE USUARIO
                        if (existe) {
                            // MOVER A LA SIGUIENTE PAGINA {ThirdPag}
                            Intent intent = new Intent(SecondPag.this, ThirdPag.class);
                            // Pasar el nombre de usuario a la siguiente actividad
                            intent.putExtra("nombreUsuario", usuario);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SecondPag.this, sp_user_unknown, Toast.LENGTH_SHORT).show();
                            // Mostrar el botón para crear cuenta
                            buttonCrearCuenta.setVisibility(View.VISIBLE);
                        }
                    }, throwable -> {
                        Toast.makeText(SecondPag.this, sp_error_query, Toast.LENGTH_SHORT).show();
                    });
        });

        // TEXTO/BOTÓN - CREAR CUENTA
        textViewCrearCuenta.setOnClickListener(v -> {
            // MOVER A LA SIGUIENTE PAGINA {CrearCuentaActivity}
            Intent intent = new Intent(SecondPag.this, CrearCuentaActivity.class);
            startActivity(intent);
        });

        // BOTÓN - CREAR CUENTA
        buttonCrearCuenta.setOnClickListener(v -> {
            // MOVER A LA SIGUIENTE PAGINA {CrearCuentaActivity}
            Intent intent = new Intent(SecondPag.this, CrearCuentaActivity.class);
            startActivity(intent);
        });

        // BOTÓN - INICIAR SESIÓN CON GOOGLE
        buttonGoogleSignIn.setOnClickListener(v -> iniciarSesionConGoogle());
    }

    private void iniciarSesionConGoogle() {
        // Crear una solicitud para el inicio de sesión
        GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder()
                .setServerClientId(getString(R.string.default_web_client_id)) // Reemplaza con tu ID de cliente web
                .build();

        // Obtener el PendingIntent para el inicio de sesión
        signInClient.getSignInIntent(signInIntentRequest)
                .addOnSuccessListener(this, pendingIntent -> {
                    try {
                        // Iniciar la actividad usando el IntentSender del PendingIntent
                        startIntentSenderForResult(
                                pendingIntent.getIntentSender(),
                                RC_SIGN_IN,
                                null, // No se necesitan datos adicionales
                                0, 0, 0); // Flags para el intent
                    } catch (Exception e) {
                        // Manejar errores al intentar iniciar el Intent
                        Log.e(TAG, "Error al iniciar IntentSender para Google Sign-In", e);
                        Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Manejar fallos al obtener el PendingIntent
                    if (e instanceof ApiException) {
                        Log.e(TAG, "API Exception al obtener el Intent de Google Sign-In: " + e.getMessage(), e);
                    } else {
                        Log.e(TAG, "Error inesperado al obtener el Intent de Google Sign-In: " + e.getMessage(), e);
                    }
                    Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                // Obtener las credenciales directamente del intent
                SignInCredential credential = signInClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    autenticarConFirebase(idToken);
                } else {
                    Log.e(TAG, "ID Token is null");
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Log.e(TAG, "Error en Google Sign-In: " + e.getMessage());
                Toast.makeText(this, "Inicio de sesión fallido: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void autenticarConFirebase(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userEmail = firebaseAuth.getCurrentUser().getEmail();
                        Toast.makeText(this, "Bienvenido: " + userEmail, Toast.LENGTH_SHORT).show();

                        // Redirigir al usuario a la página principal
                        Intent intent = new Intent(SecondPag.this, ThirdPag.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Error al autenticar con Firebase: " + task.getException().getMessage());
                        Toast.makeText(this, "Inicio de sesión fallido", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
