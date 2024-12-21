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

public class SecondPag extends AppCompatActivity {

    private SignInButton buttonGoogleSignIn;
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;
    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "SecondPag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary_pag);

        // Inicializar Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Inicializar SignInClient
        signInClient = Identity.getSignInClient(this);

        // Inicializar el botón de Google Sign-In
        buttonGoogleSignIn = findViewById(R.id.signInButtonGoogle);
        buttonGoogleSignIn.setOnClickListener(v -> {
            // Forzar signOut antes de intentar iniciar sesión
            signInClient.signOut().addOnCompleteListener(task -> {
                // Una vez cerrado la sesión, inicia el proceso de sign-in
                iniciarSesionConGoogle();
            });
        });

    }

    private void iniciarSesionConGoogle() {
        // Asegúrate de que este ID corresponda al Web Client ID configurado en Firebase Console
        String webClientId = getString(R.string.default_web_client_id);

        GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder()
                .setServerClientId(webClientId)
                .build();

        signInClient.getSignInIntent(signInIntentRequest)
                .addOnSuccessListener(this, pendingIntent -> {
                    try {
                        startIntentSenderForResult(
                                pendingIntent.getIntentSender(),
                                RC_SIGN_IN,
                                null,
                                0, 0, 0
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Error al iniciar IntentSender para Google Sign-In", e);
                        Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener el Intent de Google Sign-In: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                // Obtiene las credenciales de inicio de sesión
                SignInCredential credential = signInClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();

                if (idToken != null) {
                    autenticarConFirebase(idToken);
                } else {
                    Log.e(TAG, "ID Token es nulo, no se pudo autenticar.");
                    Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
                }

            } catch (ApiException e) {
                // Aquí se capturan los errores provenientes del intento de SignIn
                Log.e(TAG, "Error en Google Sign-In: " + e.getStatusCode() + ": " + e.getMessage(), e);
                Toast.makeText(this, "Inicio de sesión fallido: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void autenticarConFirebase(String idToken) {

        String spn_msg_welcome = getString(R.string.spn_msg_welcome);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String userEmail = user.getEmail();
                            if (userEmail == null || userEmail.isEmpty()) {
                                userEmail = "UsuarioDesconocido";
                                Toast.makeText(this, "Correo no disponible, asignando valor por defecto.", Toast.LENGTH_SHORT).show();
                            }

                            Toast.makeText(this, spn_msg_welcome + userEmail, Toast.LENGTH_SHORT).show();

                            // Redirigir al usuario a ThirdPag con el correo electrónico
                            Intent intent = new Intent(SecondPag.this, ThirdPag.class);
                            intent.putExtra("nombreUsuario", userEmail);
                            startActivity(intent);

                            finish();
                        } else {
                            Log.e(TAG, "Usuario autenticado es nulo. Algo salió mal.");
                            Toast.makeText(this, "Error inesperado durante la autenticación", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error al autenticar con Firebase: " + (task.getException() != null ? task.getException().getMessage() : "Desconocido"), task.getException());
                        Toast.makeText(this, "Inicio de sesión fallido", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

