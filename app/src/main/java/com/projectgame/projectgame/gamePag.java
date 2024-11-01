package com.projectgame.projectgame;

import android.content.Intent; // Librería para ejecutar botones
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
    private Dice dice1;
    private Dice dice2;
    private ImageView diceImage1;
    private ImageView diceImage2;
    private TextView diceResult;
    private TextView coinsTextView;
    private Button buttonVolver;
    private Button buttonRecargar; // Botón para recargar monedas
    private Button buttonMostrarResultados; // Botón para mostrar resultados
    private BaseDeDatosHelper dbHelper; // Declarar dbHelper
    private int[] diceImages = {
            R.drawable.dice_1,
            R.drawable.dice_2,
            R.drawable.dice_3,
            R.drawable.dice_4,
            R.drawable.dice_5,
            R.drawable.dice_6
    };

    // Variable para almacenar la apuesta
    private int playerBet = 0;
    private int playerCoins = 50; // Inicializa con 30 monedas
    private String nombreUsuario; // Agregar una variable para el nombre del usuario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pag);

        // Inicialización de los dados
        dice1 = new Dice();
        dice2 = new Dice();

        diceImage1 = findViewById(R.id.diceImage1);
        diceImage2 = findViewById(R.id.diceImage2);
        diceResult = findViewById(R.id.diceResult);
        coinsTextView = findViewById(R.id.coinsTextView);
        buttonRecargar = findViewById(R.id.buttonRecargar); // Inicializar el botón recargar
        buttonVolver = findViewById(R.id.buttonVolver);
        buttonMostrarResultados = findViewById(R.id.buttonMostrarResultados); // Inicializa el botón para mostrar resultados

        // Inicializar dbHelper
        dbHelper = new BaseDeDatosHelper(this);

        // Ocultar los botones de recargar y mostrar resultados al inicio
        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.GONE);

        // Inicialización de botones
        Button rollButton = findViewById(R.id.rollButton);

        // Actualiza la visualización de monedas
        updateCoinsDisplay();

        // Configurar los botones de apuestas
        setupBetButtons();

        // Configurar el botón "Tirar Dados"
        rollButton.setOnClickListener(v -> {
            if (playerBet == 0) {
                Toast.makeText(this, "Debe realizar una apuesta primero", Toast.LENGTH_SHORT).show();
            } else if (playerCoins < 5) { // Verifica si tiene suficientes monedas
                Toast.makeText(this, "No tiene suficientes monedas para apostar", Toast.LENGTH_SHORT).show();
            } else {
                playerCoins -= 5;
                updateCoinsDisplay();
                rollDiceWithAnimation();
            }
        });

        // Configurar el botón volver para ir a la actividad SecondPag
        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(gamePag.this, ThirdPag.class); // Cambiar a ThirdPag
            startActivity(intent); // Iniciar la actividad
        });

        // Configurar el botón "Recargar" para que llame a la función recargarMonedas
        buttonRecargar.setOnClickListener(v -> recargarMonedas());

        // Configurar el botón "Mostrar Resultados"
        buttonMostrarResultados.setOnClickListener(v -> {
            Intent intent = new Intent(gamePag.this, HistoricalPag.class);
            intent.putExtra("puntuacion", playerCoins); // Pasa la puntuación del jugador
            startActivity(intent);
        });
    }

    // Función para actualizar la visualización de monedas
    private void updateCoinsDisplay() {
        coinsTextView.setText("Monedas: " + playerCoins);
    }

    // Función para configurar los botones de apuesta
    private void setupBetButtons() {
        GridLayout betPanel = findViewById(R.id.betPanel);

        // Array de IDs de los botones de apuesta
        int[] betButtonIds = {
                R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn10, R.id.btn11, R.id.btn12
        };

        for (int id : betButtonIds) {
            final Button betButton = findViewById(id);
            betButton.setOnClickListener(v -> {
                // Cambiar color del botón seleccionado
                resetBetButtonColors(betPanel);
                betButton.setBackgroundColor(Color.GREEN); // Cambiar color a verde
                playerBet = Integer.parseInt(betButton.getText().toString()); // Almacenar la apuesta
            });
        }
    }

    // Función para volver al color original tras seleccionar un botón de apuesta
    private void resetBetButtonColors(GridLayout betPanel) {
        for (int i = 2; i <= 12; i++) {
            Button betButton = betPanel.findViewById(getResources().getIdentifier("btn" + i, "id", getPackageName()));
            betButton.setBackgroundColor(Color.BLUE); // Cambiar color a azul
        }
    }

    // Función para animar el lanzamiento de los dados
    private void rollDiceWithAnimation() {
        // Animar el dado 1
        animateDice(diceImage1);
        // Animar el dado 2
        animateDice(diceImage2);

        // Usar Handler para esperar un tiempo antes de mostrar el resultado
        new Handler().postDelayed(() -> {
            // Desvanecer los dados
            fadeOutDice(diceImage1);
            fadeOutDice(diceImage2);
            new Handler().postDelayed(() -> {
                // Mostrar el resultado de los dados después de la animación
                int result1 = dice1.roll();
                int result2 = dice2.roll();
                int sum = result1 + result2;

                // Cambiar las imágenes de los dados al resultado final
                diceImage1.setImageResource(diceImages[result1 - 1]);
                diceImage2.setImageResource(diceImages[result2 - 1]);

                // Re-aparecer los dados con un difuminado
                fadeInDice(diceImage1);
                fadeInDice(diceImage2);

                // Mostrar la suma de los dados
                diceResult.setText("Resultado: " + sum);

                // Verificar si la apuesta coincide con el resultado
                if (sum == playerBet) {
                    playerCoins += 10;
                    Toast.makeText(gamePag.this, "¡Has ganado!", Toast.LENGTH_SHORT).show();
                    // Aquí deberías actualizar la puntuación en la base de datos
                    dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins); // nombreUsuario debe ser el nombre del jugador
                } else {
                    Toast.makeText(gamePag.this, "Lo siento, vuelve a intentarlo.", Toast.LENGTH_SHORT).show();
                }

                // Reiniciar la apuesta
                playerBet = 0;
                resetBetButtonColors(findViewById(R.id.betPanel));
                updateCoinsDisplay();

                // Aquí inicia el flujo de la interfaz
                if (playerCoins <= 0) {
                    // Muestra el botón de recargar si las monedas se han agotado
                    buttonRecargar.setVisibility(View.VISIBLE);
                    buttonMostrarResultados.setVisibility(View.VISIBLE); // Asegúrate de que se muestre cuando no hay monedas
                } else {
                    // Muestra el botón para mostrar resultados
                    buttonMostrarResultados.setVisibility(View.VISIBLE);
                }


            }, 80);
        }, 100);  // Tiempo de espera animación
    }

    // Función para recargar monedas
    private void recargarMonedas() {
        playerCoins += 10; // Recargar 10 monedas
        Toast.makeText(this, "Se han recargado 10 monedas", Toast.LENGTH_SHORT).show();
        updateCoinsDisplay(); // Actualizar la visualización de las monedas

        // Ocultar el botón después de recargar
        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.GONE); // Ocultar resultados al recargar
    }

    // Animación de desvanecimiento (fade out)
    private void fadeOutDice(ImageView diceImage) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(diceImage, "alpha", 1f, 0f);
        fadeOut.setDuration(300); // Duración de la animación de desvanecimiento
        fadeOut.start();
    }

    // Animación de re-aparición (fade in)
    private void fadeInDice(ImageView diceImage) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(diceImage, "alpha", 0f, 1f);
        fadeIn.setDuration(300); // Duración de la animación de desvanecimiento
        fadeIn.start();
    }

    // Animación de rotación para los dados
    private void animateDice(ImageView diceImage) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(diceImage, "rotation", 0f, 360f);
        rotateAnimator.setDuration(500); // Duración de la animación
        rotateAnimator.start();
    }

    // Clase para generar números aleatorios (dados)
    private class Dice {
        public int roll() {
            return (int) (Math.random() * 6) + 1;
        }
    }
}
