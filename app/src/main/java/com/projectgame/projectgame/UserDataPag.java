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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class UserDataPag extends AppCompatActivity {

    // ATRIBUTOS
    private Button buttonVolver, btnObtenerUbicacion;
    private TextView latitudText, longitudText, precisionText, altitudText;
    private String nombreUsuario;
    private FusedLocationProviderClient fusedLocationProviderClient;  // Ubicación del cliente
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // Solicitud de permisos

    private FirebaseFirestore firestore; // Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LAYOUT
        setContentView(R.layout.activity_userdata_pag);

        // INICIALIZAR BOTONES
        buttonVolver = findViewById(R.id.buttonVolver);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);

        // INICIALIZAR CAMPOS
        latitudText = findViewById(R.id.latitudText);
        longitudText = findViewById(R.id.longitudText);
        precisionText = findViewById(R.id.precisionText);
        altitudText = findViewById(R.id.altitudText);

        // OBTENER - Nombre Usuario
        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        // Inicializar el cliente de ubicación
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance();

        // BOTON - VOLVER
        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(UserDataPag.this, ThirdPag.class);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
            finish();
        });

        // BOTON - OBTENER UBICACION
        btnObtenerUbicacion.setOnClickListener(v -> obtenerUbicacion());
    }

    // METODO - OBTENER UBICACION
    private void obtenerUbicacion() {
        String udp_not_location = getString(R.string.udp_not_location);

        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Obtener la última ubicación conocida
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                actualizarUIConUbicacion(location);
            } else { // Si no hay última ubicación, realiza una solicitud activa
                solicitarNuevaUbicacion();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, udp_not_location, Toast.LENGTH_SHORT).show();
        });
    }

    // METODO - OBTENER NUEVA UBICACION
    private void solicitarNuevaUbicacion() {
        String udp_not_location = getString(R.string.udp_not_location);

        // Verificar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Si los permisos no han sido otorgados, solicita los permisos necesarios
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Crear solicitud de ubicación
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        // Actualizaciones de ubicación
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    actualizarUIConUbicacion(location);
                    fusedLocationProviderClient.removeLocationUpdates(this); // Detener actualizaciones
                } else {
                    Toast.makeText(UserDataPag.this, udp_not_location, Toast.LENGTH_SHORT).show();
                }
            }
        }, getMainLooper());
    }

    // METODO - ACTUALIZAR UI CON UBICACION
    @SuppressLint("SetTextI18n")
    private void actualizarUIConUbicacion(Location location) {

        String udp_latitude = getString(R.string.udp_latitude);
        String udp_longitude = getString(R.string.udp_longitude);
        String udp_precision = getString(R.string.udp_precision);
        String udp_altitud = getString(R.string.udp_altitud);
        String udp_distance = getString(R.string.udp_distance);

        latitudText.setText(udp_latitude + location.getLatitude());
        longitudText.setText(udp_longitude + location.getLongitude());
        precisionText.setText(udp_precision + location.getAccuracy() + udp_distance);
        altitudText.setText(udp_altitud + location.getAltitude() + udp_distance);

        // Guardar ubicación en Firestore
        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("latitud", location.getLatitude());
        ubicacion.put("longitud", location.getLongitude());
        ubicacion.put("precision", location.getAccuracy());
        ubicacion.put("altitud", location.getAltitude());
        ubicacion.put("usuario", nombreUsuario);

        firestore.collection("ubicaciones")
                .add(ubicacion)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Ubicación guardada con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar la ubicación", Toast.LENGTH_SHORT).show();
                });
    }

    // METODO - SOLICITAR PERMISOS
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
}
