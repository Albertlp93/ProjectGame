package com.projectgame.projectgame;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BaseDeDatosHelper {

    private final FirebaseFirestore db;
    private final Context context;

    public BaseDeDatosHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    // METODO - CREAR USUARIO
    public void crearUsuario(String nombre, String contrasena, int puntuacion) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", nombre);
        usuario.put("contrasena", contrasena);
        usuario.put("puntuacion", puntuacion);

        db.collection("usuarios")
                .document(nombre)
                .set(usuario)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario creado exitosamente: " + nombre))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al crear usuario", e));
    }

    // METODO - VERIFICAR USUARIO
    public void verificarUsuario(String nombre, String contrasena, VerificarUsuarioCallback callback) {
        db.collection("usuarios")
                .document(nombre)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getString("contrasena").equals(contrasena)) {
                        callback.onUsuarioVerificado(true);
                    } else {
                        callback.onUsuarioVerificado(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al verificar usuario", e);
                    callback.onUsuarioVerificado(false);
                });
    }

    // METODO - ACTUALIZAR PUNTUACION
    public void actualizarPuntuacion(String nombre, int nuevaPuntuacion) {
        db.collection("usuarios")
                .document(nombre)
                .update("puntuacion", nuevaPuntuacion)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Puntuación actualizada correctamente para: " + nombre))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar puntuación", e));
    }

    // METODO - OBTENER PUNTUACION
    public void obtenerPuntuacion(String nombre, ObtenerPuntuacionCallback callback) {
        db.collection("usuarios")
                .document(nombre)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int puntuacion = documentSnapshot.getLong("puntuacion").intValue();
                        callback.onPuntuacionObtenida(puntuacion);
                    } else {
                        callback.onPuntuacionObtenida(30); // Valor por defecto
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener puntuación", e);
                    callback.onPuntuacionObtenida(30); // Valor por defecto
                });
    }

    // METODO - INSERTAR UBICACION
    public void insertarUbicacion(double latitud, double longitud, String usuarioId) {
        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("latitud", latitud);
        ubicacion.put("longitud", longitud);
        ubicacion.put("timestamp", System.currentTimeMillis());
        ubicacion.put("usuarioId", usuarioId);

        db.collection("ubicaciones")
                .add(ubicacion)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Ubicación guardada correctamente: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al guardar ubicación", e));
    }

    // METODO - LOG DE TODOS LOS USUARIOS
    public void logAllUsers() {
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (var doc : queryDocumentSnapshots.getDocuments()) {
                            String nombre = doc.getString("nombre");
                            int puntuacion = doc.getLong("puntuacion").intValue();
                            Log.d("Firestore", "Usuario: " + nombre + ", Puntuación: " + puntuacion);
                        }
                    } else {
                        Log.d("Firestore", "No se encontraron usuarios.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener usuarios", e));
    }

    // CALLBACKS
    public interface VerificarUsuarioCallback {
        void onUsuarioVerificado(boolean existe);
    }

    public interface ObtenerPuntuacionCallback {
        void onPuntuacionObtenida(int puntuacion);
    }
}
