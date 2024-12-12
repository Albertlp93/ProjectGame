package com.projectgame.projectgame;

import android.annotation.SuppressLint;
import android.content.Context;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UserRepository {
    // ATRIBUTOS
    private final FirebaseFirestore db;

    // CONSTRUCTOR
    public UserRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
    }

    // METODO - Verifica si el usuario existe
    public Single<Boolean> verificarUsuario(String nombre, String contraseña) {
        return Single.create(emitter -> {
            db.collection("usuarios")
                    .document(nombre)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && contraseña.equals(documentSnapshot.getString("contrasena"))) {
                            emitter.onSuccess(true);
                        } else {
                            emitter.onSuccess(false);
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    // METODO - Obtener puntuación de un usuario
    public Single<Integer> obtenerPuntuacion(String nombre) {
        return Single.create(emitter -> {
            db.collection("usuarios")
                    .document(nombre)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long puntuacion = documentSnapshot.getLong("puntuacion");
                            if (puntuacion != null) {
                                emitter.onSuccess(puntuacion.intValue());
                            } else {
                                emitter.onSuccess(0); // Valor por defecto
                            }
                        } else {
                            emitter.onSuccess(0); // Usuario no encontrado, puntuación por defecto
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    // METODO - Crear nuevo usuario
    public Completable crearUsuario(String nombre, int puntuacion) {
        return Completable.create(emitter -> {
            Map<String, Object> data = new HashMap<>();
            data.put("nombre", nombre);
            data.put("puntuacion", puntuacion);

            db.collection("usuarios").document(nombre)
                    .set(data)
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }



    // Clase interna para estructurar los datos del usuario
    private static class Usuario {
        String nombre;
        String contrasena;
        int puntuacion;

        public Usuario(String nombre, String contrasena, int puntuacion) {
            this.nombre = nombre;
            this.contrasena = contrasena;
            this.puntuacion = puntuacion;
        }
    }
}
