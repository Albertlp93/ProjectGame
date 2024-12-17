package com.projectgame.projectgame;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class gamePag extends AppCompatActivity {

    // ATRIBUTOS

    private FirebaseFirestore db;
    private SoundPool soundPool;
    private int diceRollSound;
    private Dice dice1;
    private Dice dice2;
    private ImageView diceImage1;
    private ImageView diceImage2;
    private TextView diceResult;
    private TextView coinsTextView;
    private Button buttonVolver;
    private Button buttonRecargar;
    private Button buttonMostrarResultados;
    private int[] diceImages = {
            R.drawable.dice_1,
            R.drawable.dice_2,
            R.drawable.dice_3,
            R.drawable.dice_4,
            R.drawable.dice_5,
            R.drawable.dice_6
    };

    private int playerBet = 50;
    private int playerCoins;
    private String nombreUsuario;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pag);

        // INICIALIZAR REPOSITORIO
        userRepository = new UserRepository(this);
        db = FirebaseFirestore.getInstance();

        obtenerTopDiez(); // Método para recuperar el Top 10 de jugadores

        dice1 = new Dice();
        dice2 = new Dice();

        // INICIALIZAR BOTONES Y COMPONENTES
        buttonRecargar = findViewById(R.id.buttonRecargar);
        buttonVolver = findViewById(R.id.buttonVolver);
        Button rollButton = findViewById(R.id.rollButton);
        buttonMostrarResultados = findViewById(R.id.buttonMostrarResultados);
        coinsTextView = findViewById(R.id.coinsTextView);
        diceImage1 = findViewById(R.id.diceImage1);
        diceImage2 = findViewById(R.id.diceImage2);
        diceResult = findViewById(R.id.diceResult);

        nombreUsuario = getIntent().getStringExtra("nombreUsuario");
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            nombreUsuario = "UsuarioDesconocido";
        }

        // OBTENER O INICIALIZAR PUNTUACION DEL USUARIO
        userRepository.obtenerPuntuacion(nombreUsuario)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        puntuacion -> {
                            if (puntuacion <= 0) {
                                // Si el usuario no tiene créditos, inicializar con 50
                                playerCoins = 50;
                                actualizarPuntuacionInicial();
                            } else {
                                playerCoins = puntuacion;
                            }
                            updateCoinsDisplay();
                        },
                        throwable -> Log.e("gamePag", "Error obteniendo puntuación", throwable)
                );

        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.VISIBLE);
        setupBetButtons();

        rollButton.setOnClickListener(v -> {
            String gp_first_bet = getString(R.string.gp_first_bet);
            String gp_no_coins = getString(R.string.gp_no_coins);

            if (playerBet == 0) {
                Toast.makeText(this, gp_first_bet, Toast.LENGTH_SHORT).show();
            } else if (playerCoins < 5) {
                Toast.makeText(this, gp_no_coins, Toast.LENGTH_SHORT).show();
            } else {
                playerCoins -= 5;
                updateCoinsDisplay();
                rollDiceWithAnimation();
            }
        });

        // BOTON - VOLVER
        buttonVolver.setOnClickListener(v -> {
            actualizarPuntuacionFirestore();
            Intent intent = new Intent(gamePag.this, ThirdPag.class);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
        });

        // BOTON - RECARGAR
        buttonRecargar.setOnClickListener(v -> recargarMonedas());

        // BOTON - MOSTRAR RESULTADOS
        buttonMostrarResultados.setOnClickListener(v -> {
            actualizarPuntuacionFirestore();
            Intent intent = new Intent(gamePag.this, HistoricalPag.class);
            intent.putExtra("puntuacion", playerCoins);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
        });
    }

    // METODO - ACTUALIZAR PUNTUACION INICIAL EN FIRESTORE
    private void actualizarPuntuacionInicial() {
        userRepository.crearUsuario(nombreUsuario, playerCoins)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d("gamePag", "Puntuación actualizada"),
                        throwable -> Log.e("gamePag", "Error actualizando puntuación", throwable)
                );

    }

    // METODO - OBTENER TOP 10 JUGADORES
    private void obtenerTopDiez() {
        db.collection("usuarios")
                .orderBy("victorias", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (var document : queryDocumentSnapshots.getDocuments()) {
                            String nombre = document.getString("nombre");
                            Long victorias = document.getLong("victorias");
                            Log.d("Top10", "Jugador: " + nombre + ", Victorias: " + victorias);
                        }
                    } else {
                        Log.d("Top10", "No se encontraron jugadores en el top 10");
                    }
                })
                .addOnFailureListener(e -> Log.e("Top10", "Error obteniendo el top 10", e));
    }

    // METODO - ACTUALIZACION TEXTO DE MONEDAS
    private void updateCoinsDisplay() {
        String gp_coins = getString(R.string.gp_coins);
        coinsTextView.setText(gp_coins + playerCoins);
    }

    // METODO - RECARGAR MONEDAS
    private void recargarMonedas() {
        String gp_recharged = getString(R.string.gp_recharged);
        String gp_recharged_coins = getString(R.string.gp_recharged_coins);
        playerCoins += 25;
        Toast.makeText(this, gp_recharged + playerCoins + gp_recharged_coins, Toast.LENGTH_SHORT).show();
        updateCoinsDisplay();

        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.GONE);
    }

    // METODO - CONFIGURACION BOTONES DE APUESTA
    private void setupBetButtons() {
        GridLayout betPanel = findViewById(R.id.betPanel);

        int[] betButtonIds = {
                R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn10, R.id.btn11, R.id.btn12
        };

        for (int id : betButtonIds) {
            final Button betButton = findViewById(id);
            betButton.setOnClickListener(v -> {
                resetBetButtonColors(betPanel);
                betButton.setBackgroundColor(Color.GREEN);
                playerBet = Integer.parseInt(betButton.getText().toString());
            });
        }
    }

    // METODO - RESTABLECER COLORES DE LOS BOTONES DE APUESTA
    private void resetBetButtonColors(GridLayout betPanel) {
        for (int i = 2; i <= 12; i++) {
            Button betButton = betPanel.findViewById(getResources().getIdentifier("btn" + i, "id", getPackageName()));
            betButton.setBackgroundColor(Color.BLUE);
        }
    }

    // METODO - ANIMACION DE LOS DADOS
    private void rollDiceWithAnimation() {
        String gp_results = getString(R.string.gp_results);
        String gp_won = getString(R.string.gp_won);
        String gp_victory = getString(R.string.gp_victory);
        String gp_lost = getString(R.string.gp_lost);

        animateDice(diceImage1);
        animateDice(diceImage2);

        new Handler().postDelayed(() -> {
            int result1 = dice1.roll();
            int result2 = dice2.roll();
            int sum = result1 + result2;

            diceImage1.setImageResource(diceImages[result1 - 1]);
            diceImage2.setImageResource(diceImages[result2 - 1]);
            diceResult.setText(gp_results + sum);

            if (sum == playerBet) {
                // Si gana la apuesta
                obtenerYSumarPremio();
                Toast.makeText(gamePag.this, gp_won, Toast.LENGTH_SHORT).show();
            }
            else {
                // Si pierde la apuesta, sumar 50 al premio
                incrementarPremio();
                Toast.makeText(gamePag.this, gp_lost, Toast.LENGTH_SHORT).show();
            }

            playerBet = 0;
            resetBetButtonColors(findViewById(R.id.betPanel));
            updateCoinsDisplay();

            if (playerCoins <= 0) {
                buttonRecargar.setVisibility(View.VISIBLE);
                buttonMostrarResultados.setVisibility(View.VISIBLE);
            } else {
                buttonMostrarResultados.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }

    // METODO - ANIMACION DE LOS DADOS
    private void animateDice(ImageView diceImage) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(diceImage, "rotation", 0f, 360f);
        rotateAnimator.setDuration(500);
        rotateAnimator.start();
    }

    // METODO - ACTUALIZAR PUNTUACION EN FIRESTORE
    private void actualizarPuntuacionFirestore() {
        userRepository.crearUsuario(nombreUsuario, playerCoins)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d("gamePag", "Puntuación actualizada"),
                        throwable -> Log.e("gamePag", "Error actualizando puntuación", throwable)
                );
    }


    // CLASE DE LOS DADOS

    private class Dice {
        public int roll() {
            return (int) (Math.random() * 6) + 1;
        }
    }

    //METODO - INICIALIZAR SONIDO DADOS
    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Cargar el sonido del dado
        diceRollSound = soundPool.load(this, R.raw.dado_sonido, 1);
    }

    //METODO - LIBERAR RECURSOS
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    // MeTODO: Incrementar premio en 50 puntos en Firebase
    private void incrementarPremio() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("premioActual")
                .document("O8pIDi42aYUwBU3gMb58")
                .update("premio", FieldValue.increment(50))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Premio incrementado en 50"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error incrementando premio", e));
    }

    // MeTODO: Obtener el valor del premio y actualizar la puntuación del jugador
    private void obtenerYSumarPremio() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("premioActual")
                .document("O8pIDi42aYUwBU3gMb58")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long premio = documentSnapshot.getLong("premio");
                        if (premio != null) {
                            // Sumar el premio a la puntuación del jugador
                            playerCoins += premio.intValue();
                            updateCoinsDisplay();
                            Toast.makeText(gamePag.this, "Ganaste el premio de $" + premio, Toast.LENGTH_SHORT).show();
                            // Reiniciar el premio a 0 en Firebase
                            reiniciarPremio();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error obteniendo el premio", e));
    }

    // MeTODO: Reiniciar el premio a 0 en Firebase
    private void reiniciarPremio() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("premioActual")
                .document("O8pIDi42aYUwBU3gMb58")
                .update("premio", 0)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Premio reiniciado a 0"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error reiniciando premio", e));
    }

}
