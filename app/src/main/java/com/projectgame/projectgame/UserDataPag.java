package com.projectgame.projectgame;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDataPag extends AppCompatActivity {

    private static final String FIREBASE_BASE_URL = "https://firestore.googleapis.com/v1/projects/projectgame-fp061/";
    private static final String COLLECTION_NAME = "ubicaciones";

    private Button buttonVolver, btnObtenerUbicacion;
    private TextView latitudText, longitudText, precisionText, altitudText;
    private String nombreUsuario;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FirebaseAPI firebaseAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_userdata_pag);

        buttonVolver = findViewById(R.id.buttonVolver);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);

        latitudText = findViewById(R.id.latitudText);
        longitudText = findViewById(R.id.longitudText);
        precisionText = findViewById(R.id.precisionText);
        altitudText = findViewById(R.id.altitudText);

        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        firebaseAPI = RetrofitClient.getClient(FIREBASE_BASE_URL).create(FirebaseAPI.class);

        buttonVolver.setOnClickListener(v -> {
            Intent intent = new Intent(UserDataPag.this, ThirdPag.class);
            intent.putExtra("nombreUsuario", nombreUsuario);
            startActivity(intent);
            finish();
        });

        btnObtenerUbicacion.setOnClickListener(v -> obtenerUbicacion());
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                actualizarUIConUbicacion(location);
            } else {
                solicitarNuevaUbicacion();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, getString(R.string.udp_not_location), Toast.LENGTH_SHORT).show();
        });
    }

    private void solicitarNuevaUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    actualizarUIConUbicacion(location);
                    fusedLocationProviderClient.removeLocationUpdates(this);
                }
            }
        }, getMainLooper());
    }

    @SuppressLint("SetTextI18n")
    private void actualizarUIConUbicacion(Location location) {
        latitudText.setText(getString(R.string.udp_latitude) + location.getLatitude());
        longitudText.setText(getString(R.string.udp_longitude) + location.getLongitude());
        precisionText.setText(getString(R.string.udp_precision) + location.getAccuracy());
        altitudText.setText(getString(R.string.udp_altitud) + location.getAltitude());

        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("latitud", location.getLatitude());
        ubicacion.put("longitud", location.getLongitude());
        ubicacion.put("precision", location.getAccuracy());
        ubicacion.put("altitud", location.getAltitude());
        ubicacion.put("usuario", nombreUsuario);

        guardarUbicacionEnFirebase(ubicacion);
    }

    private void guardarUbicacionEnFirebase(Map<String, Object> ubicacion) {
        firebaseAPI.saveDocument(COLLECTION_NAME, ubicacion).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UserDataPag.this, "Ubicación guardada con éxito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserDataPag.this, "Error al guardar la ubicación: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseAPI", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(UserDataPag.this, "Error de conexión al guardar ubicación", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseAPI", "Error en la solicitud", t);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Llama al método de la superclase
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Verifica el resultado de la solicitud de permisos
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Si los permisos fueron otorgados, obtiene la ubicación
            obtenerUbicacion();
        } else {
            // Si los permisos fueron denegados, muestra un mensaje al usuario
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }

}
