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

    // ATRIBUTOS
    private Button buttonVolver, btnObtenerUbicacion;
    private TextView latitudText, longitudText, precisionText, altitudText;
    private String nombreUsuario;
    private String passwordUsuario;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdata_pag);

        // INICIALIZAR BOTONES Y CAMPOS
        buttonVolver = findViewById(R.id.buttonVolver);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);
        latitudText = findViewById(R.id.latitudText);
        longitudText = findViewById(R.id.longitudText);
        precisionText = findViewById(R.id.precisionText);
        altitudText = findViewById(R.id.altitudText);

        // OBTENER NOMBRE Y CONTRASEÑA DEL USUARIO
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");
        passwordUsuario = getIntent().getStringExtra("contraseña");

        // MOSTRAR NOMBRE Y CONTRASEÑA
        TextView userNameDisplay = findViewById(R.id.userNameText);
        TextView passwordUserDisplay = findViewById(R.id.userPasswordText);

        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            userNameDisplay.setText(getString(R.string.udp_UserName) + nombreUsuario);
        } else {
            userNameDisplay.setText(getString(R.string.udp_UserName) + getString(R.string.udp_UserNameError));
        }

        if (passwordUsuario != null) {
            passwordUserDisplay.setText(getString(R.string.udp_UserPassword) + passwordUsuario);
        } else {
            passwordUserDisplay.setText(getString(R.string.udp_UserPassword) + getString(R.string.udp_UserPasswordError));
        }

        // INICIALIZAR CLIENTE DE UBICACIÓN
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // BOTÓN VOLVER
        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(UserDataPag.this, ThirdPag.class);
            intent.putExtra("nombreUsuario", nombreUsuario);
            intent.putExtra("contraseña", passwordUsuario);
            startActivity(intent);
            finish();
        });

        // BOTÓN OBTENER UBICACIÓN
        btnObtenerUbicacion.setOnClickListener(v -> obtenerUbicacion());
    }

    // MÉTODO - OBTENER UBICACIÓN
    private void obtenerUbicacion() {
        if (!verificarPermisos()) {
            solicitarPermisos();
            return;
        }

        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            actualizarUIConUbicacion(location);
                        } else {
                            solicitarNuevaUbicacion();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.udp_not_location), Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            Toast.makeText(this, getString(R.string.udp_not_permision), Toast.LENGTH_SHORT).show();
        }
    }

    // MÉTODO - VERIFICAR PERMISOS
    private boolean verificarPermisos() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // MÉTODO - SOLICITAR PERMISOS
    private void solicitarPermisos() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    // MÉTODO - OBTENER NUEVA UBICACIÓN
    private void solicitarNuevaUbicacion() {
        if (!verificarPermisos()) {
            solicitarPermisos();
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        actualizarUIConUbicacion(location);
                        fusedLocationProviderClient.removeLocationUpdates(this);
                    } else {
                        Toast.makeText(UserDataPag.this, getString(R.string.udp_not_location), Toast.LENGTH_SHORT).show();
                    }
                }
            }, getMainLooper());
        } catch (SecurityException e) {
            Toast.makeText(this, getString(R.string.udp_not_permision), Toast.LENGTH_SHORT).show();
        }
    }

    // MÉTODO - ACTUALIZAR UI CON UBICACIÓN
    @SuppressLint("SetTextI18n")
    private void actualizarUIConUbicacion(Location location) {
        latitudText.setText(getString(R.string.udp_latitude) + location.getLatitude());
        longitudText.setText(getString(R.string.udp_longitude) + location.getLongitude());
        precisionText.setText(getString(R.string.udp_precision) + location.getAccuracy() + getString(R.string.udp_distance));
        altitudText.setText(getString(R.string.udp_altitude) + location.getAltitude() + getString(R.string.udp_distance));
    }

    // MÉTODO - RESPUESTA DE PERMISOS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Llamar a la implementación de la superclase
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener ubicación
                obtenerUbicacion();
            } else {
                // Permiso denegado, mostrar mensaje
                Toast.makeText(this, getString(R.string.udp_not_permision), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
