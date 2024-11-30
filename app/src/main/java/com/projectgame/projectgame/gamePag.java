package com.projectgame.projectgame;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color; // Importación de Color corregida
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.RequiresApi;
import android.os.Build;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import java.io.OutputStream;
import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;
import android.media.AudioAttributes;
import android.media.SoundPool;


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
    private static final int CALENDAR_PERMISSION_CODE = 2;
    private SoundPool soundPool;
    private int diceRollSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_pag);

        //INICIALIZAR SONIDO DADOS
        initializeSoundPool();

        dice1 = new Dice();
        dice2 = new Dice();

        //INICIALIZAR BOTONES Y COMPONENTES
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
        buttonMostrarResultados.setVisibility(View.VISIBLE);
        setupBetButtons();

        buttonCaptureScreenshot.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    captureAndSaveScreenshotScopedStorage();
                }
                else {
                    captureAndSaveScreenshotLegacy();
                }
            }
            else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                requestStoragePermission();
            }
            else {
                captureAndSaveScreenshotScopedStorage();
            }
        });

        rollButton.setOnClickListener(v -> {

            String gp_first_bet = getString(R.string.gp_first_bet);
            String gp_no_coins = getString(R.string.gp_no_coins);

            if (playerBet == 0) {
                Toast.makeText(this, gp_first_bet, Toast.LENGTH_SHORT).show();
            }
            else if (playerCoins < 5) {
                Toast.makeText(this, gp_no_coins, Toast.LENGTH_SHORT).show();
            }
            else {
                playerCoins -= 5;
                updateCoinsDisplay();
                rollDiceWithAnimation();
            }
        });

        //BOTON - VOLVER
        buttonVolver.setOnClickListener(v -> {
            //MOVER A LA SIGUIENTE PAGINA {ThirdPag}
            Intent intent = new Intent(gamePag.this, ThirdPag.class);
            dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
        });

        //BOTON - RECARGAR
        buttonRecargar.setOnClickListener(v -> recargarMonedas());

        //BOTON - MOSTRAR RESULTADOS
        buttonMostrarResultados.setOnClickListener(v -> {
            dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins);

            //MOVER A LA SIGUIENTE PAGINA {HistoricalPag}
            Intent intent = new Intent(gamePag.this, HistoricalPag.class);
            intent.putExtra("puntuacion", playerCoins);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
        });

        //BOTON - AYUDA
        ImageButton helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> {
            //MOVER A LA SIGUIENTE PAGINA {HelpActivity}
            Intent intent = new Intent(gamePag.this, HelpActivity.class);
            startActivity(intent);
        });

    }

    //METODO - ACTUALIZACION TEXTO DE MONEDAS
    private void updateCoinsDisplay() {
        String gp_coins = getString(R.string.gp_coins);
        coinsTextView.setText(gp_coins + playerCoins);
    }

    //METODO - REECRGAR MONEDAASA
    private void recargarMonedas() {
        String gp_recharged = getString(R.string.gp_recharged);
        String gp_recharged_coins = getString(R.string.gp_recharged_coins);
        playerCoins += 25;
        Toast.makeText(this, gp_recharged + playerCoins + gp_recharged_coins, Toast.LENGTH_SHORT).show();
        updateCoinsDisplay();

        buttonRecargar.setVisibility(View.GONE);
        buttonMostrarResultados.setVisibility(View.GONE);
    }

    //METODO - CONFIGURACION BOTONES DE APUESTA
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

    //METODO- RESTABLECER COLORES DE LOS BOTONES DE APUESTA
    private void resetBetButtonColors(GridLayout betPanel) {
        for (int i = 2; i <= 12; i++) {
            Button betButton = betPanel.findViewById(getResources().getIdentifier("btn" + i, "id", getPackageName()));
            betButton.setBackgroundColor(Color.BLUE);
        }
    }

    //METODO - PERMISOS DE ALMACENAMIENTO
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    //METODO - SOLICITAR PERMISOS DE ALMACENAMIENTO
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //METODO - MANEJO DE PERMISOS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String gp_storage_denied = getString(R.string.gp_storage_denied);
        String gp_calendar_granted = getString(R.string.gp_calendar_granted);
        String gp_calendar_denied = getString(R.string.gp_calendar_denied);


        // Manejo de permisos de almacenamiento
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    captureAndSaveScreenshotScopedStorage();
                }
                else {
                    captureAndSaveScreenshotLegacy();
                }
            }
            else {
                Toast.makeText(this, gp_storage_denied, Toast.LENGTH_SHORT).show();
            }
        }

        // Manejo de permisos de calendario
        if (requestCode == CALENDAR_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, gp_calendar_granted, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, gp_calendar_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }



    //METODO - CAPTURA DE PANTALLA
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void captureAndSaveScreenshotScopedStorage() {
        String gp_calendar_denied = getString(R.string.gp_calendar_denied);
        String gp_screenshot_saved = getString(R.string.gp_screenshot_saved);
        String gp_error_saving_screenshot = getString(R.string.gp_error_saving_screenshot);

        Bitmap screenshot = getScreenshot();
        if (screenshot == null) return;


        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "VictoryScreenshot_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            screenshot.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, gp_screenshot_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, gp_error_saving_screenshot, Toast.LENGTH_SHORT).show();
        }
    }

    //METODO - CAPTURA DE PANTALLA LEGACY
    private void captureAndSaveScreenshotLegacy() {
        String gp_screenshot_saved = getString(R.string.gp_screenshot_saved);
        String gp_error_saving_screenshot = getString(R.string.gp_error_saving_screenshot);

        Bitmap screenshot = getScreenshot();
        if (screenshot == null) return;

        String imagePath = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                screenshot,
                "VictoryScreenshot_" + System.currentTimeMillis(),
                "Victory screenshot after winning the game"
        );

        if (imagePath != null) {
            Toast.makeText(this, gp_screenshot_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, gp_error_saving_screenshot, Toast.LENGTH_SHORT).show();
        }
    }

    //METODO - OBTENER PANTALLA
    private Bitmap getScreenshot() {
        View rootView = findViewById(android.R.id.content);
        rootView.setDrawingCacheEnabled(true);
        Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        return screenshot;
    }


    //METODO - GUARDAR VICTORIA EN GOOGLE CALENDAR
    private void saveVictoryToGoogleCalendar(String title, String nombreUsuario, int coinsWon, int winningRoll) {
        String gp_victory_saved_calendar = getString(R.string.gp_victory_saved_calendar);
        String gp_event_created_URI = getString(R.string.gp_event_created_URI);
        String gp_error_saving_calendar = getString(R.string.gp_error_saving_calendar);
        String gp_error_insertion = getString(R.string.gp_error_insertion);
        String gp_error_saving_event = getString(R.string.gp_error_saving_event);
        String gp_error_exception = getString(R.string.gp_error_exception);


        // Verificar permisos en tiempo de ejecución
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
                    CALENDAR_PERMISSION_CODE);
            return;
        }

        // Verifica si hay un calendario guardado en preferencias
        long calendarId = getSavedCalendarId();

        if (calendarId == -1) {
            // Si no hay un calendario guardado, busca uno predeterminado
            calendarId = getDefaultCalendarId();
            if (calendarId == -1) {
                // Si no se encuentra un calendario predeterminado, permite al usuario seleccionar uno
                showCalendarSelectionDialog(title, nombreUsuario, coinsWon, winningRoll);
                return;
            }
            saveSelectedCalendarId(calendarId); // Guarda el calendario seleccionado para futuras ejecuciones
        }

        // Configurar los valores del evento
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (30 * 60 * 1000); // Duración del evento: 30 minutos

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION,
                "Nombre de usuario: " + nombreUsuario +
                        "\nMonedas ganadas: " + coinsWon +
                        "\nTirada ganadora: " + winningRoll);
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().getID());

        // Intentar guardar el evento en el calendario
        try {
            ContentResolver cr = getContentResolver();
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            if (uri != null) {
                Toast.makeText(this, gp_victory_saved_calendar, Toast.LENGTH_SHORT).show();
                Log.i("GoogleCalendar", gp_event_created_URI + uri.toString());
            }
            else {
                Toast.makeText(this, gp_error_saving_calendar, Toast.LENGTH_SHORT).show();
                Log.e("GoogleCalendar", gp_error_insertion);
            }
        }
        catch (Exception e) {
            Toast.makeText(this, gp_error_saving_event, Toast.LENGTH_SHORT).show();
            Log.e("GoogleCalendar", gp_error_exception + e.getMessage());
        }
    }

    //METODO - OBTENER ID DEL CALENDARIO PREDETERMINADO
    private long getDefaultCalendarId() {

        String gp_error_clendar_ID = getString(R.string.gp_error_clendar_ID);

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    CalendarContract.Calendars.CONTENT_URI,
                    new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME},
                    CalendarContract.Calendars.VISIBLE + " = 1", // Solo calendarios visibles
                    null,
                    null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    Log.d("Calendars", "ID: " + id + ", Nombre: " + name);
                    return id; // Devuelve el primer calendario visible encontrado
                }
            }
        } catch (Exception e) {
            Log.e("GoogleCalendar", gp_error_clendar_ID + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close(); // Asegurar que el cursor se cierre
            }
        }

        return -1; // Si no se encuentra un calendario válido
    }

    //METODO - MOSTRAR SELECCION DE CALENDARIO
    private void showCalendarSelectionDialog(String title, String nombreUsuario, int coinsWon, int winningRoll) {
        String gp_calendar_select = getString(R.string.gp_calendar_select);
        String gp_calendar_no_available = getString(R.string.gp_calendar_no_available);
        String gp_calendar_failed_load = getString(R.string.gp_calendar_failed_load);

        Cursor cursor = getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME},
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                null
        );

        if (cursor != null) {
            ArrayList<String> calendarNames = new ArrayList<>();
            ArrayList<Long> calendarIds = new ArrayList<>();

            while (cursor.moveToNext()) {
                calendarIds.add(cursor.getLong(0));
                calendarNames.add(cursor.getString(1));
            }
            cursor.close();

            if (calendarNames.isEmpty()) {
                Toast.makeText(this, gp_calendar_no_available, Toast.LENGTH_SHORT).show();

                return;
            }

            // Mostrar un diálogo para que el usuario seleccione un calendario
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(gp_calendar_select);
            builder.setItems(calendarNames.toArray(new String[0]), (dialog, which) -> {
                long selectedCalendarId = calendarIds.get(which);
                saveSelectedCalendarId(selectedCalendarId); // Guarda el ID seleccionado
                saveVictoryToGoogleCalendar(title, nombreUsuario, coinsWon, winningRoll); // Reintenta guardar la victoria
            });
            builder.show();
        } else {
            Toast.makeText(this, gp_calendar_failed_load, Toast.LENGTH_SHORT).show();
        }
    }

    //METODO - GUARDAR ID DEL CALENDARIO SELECCIONADO
    private void saveSelectedCalendarId(long calendarId) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("selectedCalendarId", calendarId);
        editor.apply();
    }

    //METODO - OBTENER ID DEL CALENDARIO SELECCIONADO
    private long getSavedCalendarId() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        return prefs.getLong("selectedCalendarId", -1);
    }

    //METODO - ANIMACION DE LOS DADOS
    private void rollDiceWithAnimation() {
        String gp_results = getString(R.string.gp_results);
        String gp_won = getString(R.string.gp_won);
        String gp_victory = getString(R.string.gp_victory);
        String gp_lost = getString(R.string.gp_lost);


        // Reproducir el sonido de los dados
        if (soundPool != null && diceRollSound != 0) {
            soundPool.play(diceRollSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }

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
            diceResult.setText(gp_results + sum);

            if (sum == playerBet) {
                playerCoins += 10;
                Toast.makeText(gamePag.this, gp_won, Toast.LENGTH_SHORT).show();
                dbHelper.actualizarPuntuacion(nombreUsuario, playerCoins);

                // Guardar la victoria en Google Calendar
                saveVictoryToGoogleCalendar(gp_victory, nombreUsuario, 10, sum);
                saveVictoryToGoogleCalendar(gp_victory, nombreUsuario, 10, sum);
            } else {
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

    //METODO - ANIMACION DE LOS DADOS
    private void animateDice(ImageView diceImage) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(diceImage, "rotation", 0f, 360f);
        rotateAnimator.setDuration(500);
        rotateAnimator.start();
    }

    //METODO - CLASE DE LOS DADOS
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

}
