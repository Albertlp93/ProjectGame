package com.projectgame.projectgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SecondPagFirebase extends AppCompatActivity {

    private static final String TAG = "SecondPagFirebase";
    private static final int RC_SIGN_IN = 1001;

    private SignInButton buttonGoogleSignIn;
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_pag_firebase);

        // Inicializar Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Inicializar SignInClient
        signInClient = Identity.getSignInClient(this);

        // Configurar botón Google Sign-In
        buttonGoogleSignIn = findViewById(R.id.signInButtonGoogle);
        buttonGoogleSignIn.setSize(SignInButton.SIZE_WIDE); // Aplicar estilo
        buttonGoogleSignIn.setOnClickListener(v -> iniciarSesionConGoogle());
    }

    private void iniciarSesionConGoogle() {
        String webClientId = getString(R.string.default_web_client_id);

        // Crear Intent de Google Sign-In
        GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder()
                .setServerClientId(webClientId)
                .build();

        signInClient.getSignInIntent(signInIntentRequest)
                .addOnSuccessListener(this, pendingIntent -> {
                    try {
                        startIntentSenderForResult(
                                pendingIntent.getIntentSender(),
                                RC_SIGN_IN,
                                null, 0, 0, 0
                        );
                    } catch (Exception e) {
                        mostrarError("Error al iniciar Google Sign-In", e);
                    }
                })
                .addOnFailureListener(e -> mostrarError("Error al obtener el Intent de Google Sign-In", e));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                SignInCredential credential = signInClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();

                if (idToken != null) {
                    autenticarConFirebase(idToken);
                } else {
                    mostrarError("ID Token es nulo. No se pudo autenticar.", null);
                }

            } catch (ApiException e) {
                mostrarError("Error en Google Sign-In", e);
            }
        }
    }

    private void autenticarConFirebase(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String userEmail = user.getEmail() == null ? "UsuarioDesconocido" : user.getEmail();
                            Toast.makeText(this, "Bienvenido: " + userEmail, Toast.LENGTH_SHORT).show();

                            // Redirigir al usuario
                            Intent intent = new Intent(SecondPagFirebase.this, ThirdPag.class);
                            intent.putExtra("nombreUsuario", userEmail);
                            startActivity(intent);
                            finish();
                        } else {
                            mostrarError("Usuario autenticado es nulo.", null);
                        }
                    } else {
                        mostrarError("Error al autenticar con Firebase", task.getException());
                    }
                });
    }

    private void mostrarError(String mensaje, Exception e) {
        Log.e(TAG, mensaje, e);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
