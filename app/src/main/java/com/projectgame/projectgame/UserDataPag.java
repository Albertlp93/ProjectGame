package com.projectgame.projectgame;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class UserDataPag extends AppCompatActivity {

    //ATRIBUTOS
    private Button buttonVolver, btnObtenerUbicacion;
    private TextView latitudText, longitudText, precisionText, altitudText;
    private String nombreUsuario;
    private FusedLocationProviderClient fusedLocationProviderClient;  //Ubicacion del cliente
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; //Solicitud de permisos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LAYOUT
        setContentView(R.layout.activity_userdata_pag);

        //INICIALIZAR BOTONES
        buttonVolver = findViewById(R.id.buttonVolver);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);

        //INICIALIZAR CAMPOS
        latitudText = findViewById(R.id.latitudText);
        longitudText = findViewById(R.id.longitudText);
        precisionText = findViewById(R.id.precisionText);
        altitudText = findViewById(R.id.altitudText);

        //OBTENER - Nombre Usuario
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        // Inicializar el cliente de ubicación
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //BOTON - VOLVER
        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(UserDataPag.this, ThirdPag.class);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
            finish();
        });

        //BOTON - OBTENER UBICACION
        btnObtenerUbicacion.setOnClickListener(v -> obtenerUbicacion());
    }

    //METODO - OBTENER UBICACION
    private void obtenerUbicacion() {

        //Verificar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Solicitar permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        //Obtener la última ubicación conocida
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                actualizarUIConUbicacion(location);
            }
            else { //Si no hay última ubicación, realiza una solicitud activa
                solicitarNuevaUbicacion();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
        });
    }

    //METODO - OBTENER NUEVA UBICACION
    private void solicitarNuevaUbicacion() {
        //Verificar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Si los permisos no han sido otorgados, solicita los permisos necesarios
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        //Crear solicitud de ubicación
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        //Actualizaciones de ubicación
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    actualizarUIConUbicacion(location);
                    fusedLocationProviderClient.removeLocationUpdates(this); // Detener actualizaciones
                }
                else {
                    Toast.makeText(UserDataPag.this, "No se pudo obtener la ubicación activa", Toast.LENGTH_SHORT).show();
                }
            }
        }, getMainLooper());
    }

    @SuppressLint("SetTextI18n")
    private void actualizarUIConUbicacion(Location location) {
        latitudText.setText("Latitud: " + location.getLatitude());
        longitudText.setText("Longitud: " + location.getLongitude());
        precisionText.setText("Precisión: " + location.getAccuracy() + " metros");
        altitudText.setText("Altitud: " + location.getAltitude() + " metros");

        //Guardar ubicación en la base de datos
        BaseDeDatosHelper dbHelper = new BaseDeDatosHelper(this);
        dbHelper.insertarUbicacion(location.getLatitude(), location.getLongitude(), obtenerIdUsuario(nombreUsuario));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacion();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int obtenerIdUsuario(String nombreUsuario) {
        return 1;
    }
}
