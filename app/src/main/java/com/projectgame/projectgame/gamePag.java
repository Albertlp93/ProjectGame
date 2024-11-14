package com.projectgame.projectgame;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color; // Importación de Color corregida
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.RequiresApi;
import android.os.Build;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import java.io.OutputStream;

public class gamePag extends AppCompatActivity {

    // ATRIBUTOS
    private Dice dice1;
    private Dice dice2;
    private ImageView diceImage1;
    private ImageView diceImage2;
    private TextView diceResult;
    private TextView coinsTextView;
    private Button buttonVolver;
    private Button buttonRecargar;
    private Button buttonMostrarResultados;
    private ImageButton buttonCaptureScreenshot;
    private BaseDeDatosHelper dbHelper;
    private int[] diceImages = {
            R.drawable.dice_1,
            R.drawable.dice_2,
            R.drawable.dice_3,
            R.drawable.dice_4,
            R.drawable.dice_5,
            R.drawable.dice_6
    };

    private int playerBet = 0;
    private int playerCoins;
    private String nombreUsuario;
    private static final int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pag);

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
        buttonCaptureScreenshot = findViewById(R.id.buttonCaptureScreenshot);

        dbHelper = new BaseDeDatosHelper(this);
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");
        playerCoins = dbHelper.obtenerPuntuacion(nombreUsuario);
        updateCoinsDisplay();

        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.GONE);
        setupBetButtons();

        buttonCaptureScreenshot.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    captureAndSaveScreenshotScopedStorage();
                } else {
                    captureAndSaveScreenshotLegacy();
                }
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                requestStoragePermission();
            } else {
                captureAndSaveScreenshotScopedStorage();
            }
        });

        rollButton.setOnClickListener(v -> {
            if (playerBet == 0) {
                Toast.makeText(this, "Debe realizar una apuesta primero", Toast.LENGTH_SHORT).show();
            } else if (playerCoins < 5) {
                Toast.makeText(this, "No tiene suficientes monedas para apostar", Toast.LENGTH_SHORT).show();
            } else {
                playerCoins -= 5;
                updateCoinsDisplay();
                rollDiceWithAnimation();
            }
        });

        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(gamePag.this, ThirdPag.class);
            dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
        });

        buttonRecargar.setOnClickListener(v -> recargarMonedas());

        buttonMostrarResultados.setOnClickListener(v -> {
            dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins);
            Intent intent = new Intent(gamePag.this, HistoricalPag.class);
            intent.putExtra("puntuacion", playerCoins);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
        });
    }

    // Método para actualizar el texto de las monedas
    private void updateCoinsDisplay() {
        coinsTextView.setText("Monedas: " + playerCoins);
    }

    // Método para recargar monedas
    private void recargarMonedas() {
        playerCoins += 10;
        Toast.makeText(this, "Se han recargado 10 monedas", Toast.LENGTH_SHORT).show();
        updateCoinsDisplay();

        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.GONE);
    }

    // Configurar botones de apuesta
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

    // Restablecer colores de botones de apuesta
    private void resetBetButtonColors(GridLayout betPanel) {
        for (int i = 2; i <= 12; i++) {
            Button betButton = betPanel.findViewById(getResources().getIdentifier("btn" + i, "id", getPackageName()));
            betButton.setBackgroundColor(Color.BLUE);
        }
    }

    // Verificar y solicitar permiso de almacenamiento
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                captureAndSaveScreenshotScopedStorage();
            } else {
                captureAndSaveScreenshotLegacy();
            }
        } else {
            Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
        }
    }


    // Método para capturar y guardar la captura de pantalla en Android 10+ (API 29 o superior)
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void captureAndSaveScreenshotScopedStorage() {
        Bitmap screenshot = getScreenshot();
        if (screenshot == null) return;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "VictoryScreenshot_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            screenshot.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Captura guardada en la galería", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar la captura", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para capturar y guardar la captura de pantalla en versiones anteriores a Android 10
    private void captureAndSaveScreenshotLegacy() {
        Bitmap screenshot = getScreenshot();
        if (screenshot == null) return;

        String imagePath = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                screenshot,
                "VictoryScreenshot_" + System.currentTimeMillis(),
                "Victory screenshot after winning the game"
        );

        if (imagePath != null) {
            Toast.makeText(this, "Captura guardada en la galería", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar la captura", Toast.LENGTH_SHORT).show();
        }
    }

    // Método auxiliar para capturar la vista actual
    private Bitmap getScreenshot() {
        View rootView = findViewById(android.R.id.content);
        rootView.setDrawingCacheEnabled(true);
        Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return screenshot;
    }


    // Método para animar y mostrar el resultado de los dados
    private void rollDiceWithAnimation() {
        // Animación de los dados
        animateDice(diceImage1);
        animateDice(diceImage2);

        // Usamos un Handler para introducir una demora antes de mostrar el resultado
        new Handler().postDelayed(() -> {
            int result1 = dice1.roll();
            int result2 = dice2.roll();
            int sum = result1 + result2;

            diceImage1.setImageResource(diceImages[result1 - 1]);
            diceImage2.setImageResource(diceImages[result2 - 1]);
            diceResult.setText("Resultado: " + sum);

            if (sum == playerBet) {
                playerCoins += 10;
                Toast.makeText(gamePag.this, "¡Has ganado!", Toast.LENGTH_SHORT).show();
                dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins);
            } else {
                Toast.makeText(gamePag.this, "Lo siento, vuelve a intentarlo.", Toast.LENGTH_SHORT).show();
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

    private void animateDice(ImageView diceImage) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(diceImage, "rotation", 0f, 360f);
        rotateAnimator.setDuration(500);
        rotateAnimator.start();
    }

    private class Dice {
        public int roll() {
            return (int) (Math.random() * 6) + 1;
        }
    }
}
