package com.projectgame.projectgame;

import android.content.Intent;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class gamePag extends AppCompatActivity {

    //ATRIBUTOS
    private Dice dice1;
    private Dice dice2;
    private ImageView diceImage1;
    private ImageView diceImage2;
    private TextView diceResult;
    private TextView coinsTextView;
    private Button buttonVolver;
    private Button buttonRecargar;
    private Button buttonMostrarResultados;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LAYOUT
        setContentView(R.layout.activity_game_pag);

        dice1 = new Dice();
        dice2 = new Dice();

        //INICIALIZAR BOTONES
        buttonRecargar = findViewById(R.id.buttonRecargar);
        buttonVolver = findViewById(R.id.buttonVolver);
        Button rollButton = findViewById(R.id.rollButton);
        buttonMostrarResultados = findViewById(R.id.buttonMostrarResultados);

        //INICIALIZAR CAMPOS
        coinsTextView = findViewById(R.id.coinsTextView);

        //INICIALIZAR IMAGENES
        diceImage1 = findViewById(R.id.diceImage1);
        diceImage2 = findViewById(R.id.diceImage2);
        diceResult = findViewById(R.id.diceResult);


        dbHelper = new BaseDeDatosHelper(this);

        //Obtener el nombre de usuario de la actividad anterior
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        //Puntuacion del usuario
        playerCoins = dbHelper.obtenerPuntuacion(nombreUsuario);
        updateCoinsDisplay();

        //Ocultar botones al inicio
        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.GONE);

        setupBetButtons();

        rollButton.setOnClickListener(v -> {
            if (playerBet == 0) {
                Toast.makeText(this, "Debe realizar una apuesta primero", Toast.LENGTH_SHORT).show();
            }
            else if (playerCoins < 5) {
                Toast.makeText(this, "No tiene suficientes monedas para apostar", Toast.LENGTH_SHORT).show();
            }
            else {
                playerCoins -= 5;
                updateCoinsDisplay();
                rollDiceWithAnimation();
            }
        });

        //BOTON - VOLVER
        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(gamePag.this, ThirdPag.class);
            dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
        });

        //BOTON - RECARGAR
        buttonRecargar.setOnClickListener(v -> recargarMonedas());

        buttonMostrarResultados.setOnClickListener(v -> {
            // Actualizar la puntuación del usuario en la base de datos
            dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins);

            //MOVER A LA SIGUIENTE PAGINA {HistoricalPag}
            Intent intent = new Intent(gamePag.this, HistoricalPag.class);
                //Pasa la puntuación actual del jugador
                intent.putExtra("puntuacion", playerCoins);
                startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Obtener la puntuación actualizada del usuario desde la base de datos
        playerCoins = dbHelper.obtenerPuntuacion(nombreUsuario);
        updateCoinsDisplay();
    }

    private void updateCoinsDisplay() {
        coinsTextView.setText("Monedas: " + playerCoins);
    }

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

    private void resetBetButtonColors(GridLayout betPanel) {
        for (int i = 2; i <= 12; i++) {
            Button betButton = betPanel.findViewById(getResources().getIdentifier("btn" + i, "id", getPackageName()));
            betButton.setBackgroundColor(Color.BLUE);
        }
    }

    private void rollDiceWithAnimation() {
        animateDice(diceImage1);
        animateDice(diceImage2);

        new Handler().postDelayed(() -> {
            fadeOutDice(diceImage1);
            fadeOutDice(diceImage2);
            new Handler().postDelayed(() -> {
                int result1 = dice1.roll();
                int result2 = dice2.roll();
                int sum = result1 + result2;

                diceImage1.setImageResource(diceImages[result1 - 1]);
                diceImage2.setImageResource(diceImages[result2 - 1]);

                fadeInDice(diceImage1);
                fadeInDice(diceImage2);

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
            }, 80);
        }, 100);
    }

    private void recargarMonedas() {
        playerCoins += 10;
        Toast.makeText(this, "Se han recargado 10 monedas", Toast.LENGTH_SHORT).show();
        updateCoinsDisplay();

        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.GONE);
    }

    private void fadeOutDice(ImageView diceImage) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(diceImage, "alpha", 1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.start();
    }

    private void fadeInDice(ImageView diceImage) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(diceImage, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.start();
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

