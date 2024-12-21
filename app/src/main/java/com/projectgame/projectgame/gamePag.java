package com.projectgame.projectgame;


import android.animation.ObjectAnimator;
import android.app.AlertDialog;
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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class gamePag extends AppCompatActivity {

    // ATRIBUTOS

    private FirebaseFirestore db;
    private SoundPool soundPool;
    private int diceRollSound;
    private int playerBet = 50;
    private int playerCoins;
    private Dice dice1;
    private Dice dice2;
    private ImageView diceImage1;
    private ImageView diceImage2;
    private TextView diceResult;
    private TextView coinsTextView;
    private Button buttonVolver;
    private Button buttonRecargar;
    private Button buttonMostrarResultados;
    private String nombreUsuario;
    private UserRepository userRepository;

    private ImageButton buttonCaptureScreenshot;

    private ImageButton helpButton;
    private int[] diceImages = {
            R.drawable.dice_1,
            R.drawable.dice_2,
            R.drawable.dice_3,
            R.drawable.dice_4,
            R.drawable.dice_5,
            R.drawable.dice_6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pag);

        // INICIALIZAR REPOSITORIO
        userRepository = new UserRepository(this);
        db = FirebaseFirestore.getInstance();

        obtenerTopDiez();

        dice1 = new Dice();
        dice2 = new Dice();

        // INICIALIZAR BOTONES
        buttonRecargar = findViewById(R.id.buttonRecargar);
        buttonVolver = findViewById(R.id.buttonVolver);
        Button rollButton = findViewById(R.id.rollButton);
        buttonMostrarResultados = findViewById(R.id.buttonMostrarResultados);
        helpButton = findViewById(R.id.helpButton);
        initializeSoundPool();

        // Encontrar el botón de captura de pantalla
        buttonCaptureScreenshot = findViewById(R.id.buttonCaptureScreenshot);

        // Configurar funcionalidad del botón de ayuda
        helpButton.setOnClickListener(v -> mostrarAyuda());

        // Configurar el listener para el botón
        buttonCaptureScreenshot.setOnClickListener(v -> captureScreenshot());

        //INICIALIZAR TEXTOS
        coinsTextView = findViewById(R.id.coinsTextView);
        String gp_error_score = getString(R.string.gp_error_score);

        //INICIALIZAR COMPONENTES
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
                            }
                            else {
                                playerCoins = puntuacion;
                            }
                            updateCoinsDisplay();
                        },
                        throwable -> Log.e("gamePag", gp_error_score, throwable)
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

    private void mostrarAyuda() {
        // Crear un AlertDialog para mostrar la información
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.help_dialog_title));
        builder.setMessage(getString(R.string.help_dialog_message));
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void captureScreenshot() {
        // Obtener la vista raíz de la actividad
        View rootView = getWindow().getDecorView().getRootView();

        // Habilitar el dibujo en caché de la vista
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        // Guardar la imagen
        saveScreenshot(bitmap);
    }

    private void saveScreenshot(Bitmap bitmap) {

        String gp_screenshot_sav          = getString(R.string.gp_screenshot_sav);
        String gp_error_saving_screenshot = getString(R.string.gp_error_saving_screenshot);

        // Crear un nombre único para el archivo
        String fileName = "screenshot_" + System.currentTimeMillis() + ".png";

        // Guardar en el almacenamiento público
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Capturas");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Toast.makeText(this, gp_screenshot_sav + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Opcional: Añadir la imagen a la galería
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (IOException e) {
            Toast.makeText(this, gp_error_saving_screenshot + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Screenshot", gp_error_saving_screenshot, e);
        }
    }

    // METODO - ACTUALIZAR PUNTUACION INICIAL EN FIRESTORE
    private void actualizarPuntuacionInicial() {

        String gp_score_update       = getString(R.string.gp_score_update);
        String gp_error_score_update = getString(R.string.gp_error_score_update);

        userRepository.crearUsuario(nombreUsuario, playerCoins)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d("gamePag", gp_score_update),
                        throwable -> Log.e("gamePag", gp_error_score_update, throwable)
                );

    }

    // METODO - OBTENER TOP 10 JUGADORES
    private void obtenerTopDiez() {

        String gp_player          = getString(R.string.gp_player);
        String gp_victories       = getString(R.string.gp_victories);
        String gp_error_gettin10  = getString(R.string.gp_error_gettin10);
        String gp_top_10_nt_found = getString(R.string.gp_top_10_nt_found);

        db.collection("usuarios")
                .orderBy("victorias", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (var document : queryDocumentSnapshots.getDocuments()) {
                            String nombre = document.getString("nombre");
                            Long victorias = document.getLong("victorias");
                            Log.d("Top10", gp_player + nombre + gp_victories + victorias);
                        }
                    } else {
                        Log.d("Top10", gp_top_10_nt_found);
                    }
                })
                .addOnFailureListener(e -> Log.e("Top10", gp_error_gettin10, e));
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

        // Reproducir el sonido del dado
        if (soundPool != null) {
            soundPool.play(diceRollSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }

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
            } else {
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

        String gp_score_update       = getString(R.string.gp_score_update);
        String gp_error_score_update = getString(R.string.gp_error_score_update);

        userRepository.crearUsuario(nombreUsuario, playerCoins)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d("gamePag", gp_score_update),
                        throwable -> Log.e("gamePag", gp_error_score_update, throwable)
                );
    }

    //CLASE DE LOS DADOS

    private class Dice {
        public int roll() {
            return (int) (Math.random() * 6) + 1;
        }
    }

    //METODO - INICIALIZAR SONIDO DADOS
    private void initializeSoundPool() {

        String gp_sound       = getString(R.string.gp_sound);
        String gp_error_sound = getString(R.string.gp_error_sound);

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

        // Listener para confirmar que el sonido está cargado
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                Log.d("SoundPool", gp_sound);
            } else {
                Log.e("SoundPool", gp_error_sound);
            }
        });
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

    //METODO - INCREMENTAR PREMIO EN FIREBASE
    private void incrementarPremio() {

        int incremento = 50;

        String gp_premio_incrementado = getString(R.string.gp_premio_incrementado);
        String gp_error_incrementando = getString(R.string.gp_error_incrementando);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("premioActual")
                .document("O8pIDi42aYUwBU3gMb58")
                .update("premio", FieldValue.increment(incremento))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", gp_premio_incrementado))
                .addOnFailureListener(e -> Log.e("Firestore", gp_error_incrementando, e));
    }

    //METODO - Obtener premio + actualizar puntuacion jugador
    private void obtenerYSumarPremio() {

        String gp_win_prize   = getString(R.string.gp_win_prize);
        String gp_error_prize = getString(R.string.gp_error_prize);

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
                            Toast.makeText(gamePag.this, gp_win_prize + premio, Toast.LENGTH_SHORT).show();
                            // Reiniciar el premio a 0 en Firebase
                            reiniciarPremio();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", gp_error_prize, e));
    }

    //METODO - Reiniciar premio a 0 en Firebase
    private void reiniciarPremio() {

        String gp_prize_reset       = getString(R.string.gp_prize_reset);
        String gp_error_prize_reset = getString(R.string.gp_error_prize_reset);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("premioActual")
                .document("O8pIDi42aYUwBU3gMb58")
                .update("premio", 0)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", gp_prize_reset))
                .addOnFailureListener(e -> Log.e("Firestore", gp_error_prize_reset, e));
    }
}
