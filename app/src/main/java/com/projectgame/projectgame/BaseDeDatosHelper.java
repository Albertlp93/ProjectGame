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

    //METODO - CREAR USUARIO
    public void crearUsuario(String nombre, String contrasena, int puntuacion) {

        String db_user_created     = context.getString(R.string.db_user_created);
        String db_user_not_created = context.getString(R.string.db_user_created);

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", nombre);
        usuario.put("contrasena", contrasena);
        usuario.put("puntuacion", puntuacion);

        db.collection("usuarios")
                .document(nombre)
                .set(usuario)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", db_user_created + nombre))
                .addOnFailureListener(e -> Log.e("Firestore", db_user_not_created, e));
    }

    //METODO - VERIFICAR USUARIO
    public void verificarUsuario(String nombre, String contrasena, VerificarUsuarioCallback callback) {

        String db_error_verify = context.getString(R.string.db_error_verify);

        db.collection("usuarios")
                .document(nombre)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getString("contrasena").equals(contrasena)) {
                        callback.onUsuarioVerificado(true);
                    }
                    else {
                        callback.onUsuarioVerificado(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", db_error_verify, e);
                    callback.onUsuarioVerificado(false);
                });
    }


    //METODO - ACTUALIZAR PUNTUACION
    public void actualizarPuntuacion(String nombre, int nuevaPuntuacion) {

        String db_score_updated     = context.getString(R.string.db_score_updated);
        String db_score_not_updated = context.getString(R.string.db_score_not_updated);

        db.collection("usuarios")
                .document(nombre)
                .update("puntuacion", nuevaPuntuacion)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", db_score_updated + nombre))
                .addOnFailureListener(e -> Log.e("Firestore", db_score_not_updated, e));
    }

    //METODO - OBTENER PUNTUACION
    public void obtenerPuntuacion(String nombre, ObtenerPuntuacionCallback callback) {

        String db_score_error = context.getString(R.string.db_score_error);

        db.collection("usuarios")
                .document(nombre)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int puntuacion = documentSnapshot.getLong("puntuacion").intValue();
                        callback.onPuntuacionObtenida(puntuacion);
                    }
                    else {
                        callback.onPuntuacionObtenida(30); // Valor por defecto
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", db_score_error, e);
                    callback.onPuntuacionObtenida(30); // Valor por defecto
                });
    }

    //METODO - INSERTAR UBICACION
    public void insertarUbicacion(double latitud, double longitud, String usuarioId) {

        String db_locacion_sav = context.getString(R.string.db_locacion_sav);
        String db_error = context.getString(R.string.db_error);

        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("latitud", latitud);
        ubicacion.put("longitud", longitud);
        ubicacion.put("timestamp", System.currentTimeMillis());
        ubicacion.put("usuarioId", usuarioId);

        db.collection("ubicaciones")
                .add(ubicacion)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", db_locacion_sav + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("Firestore", db_error, e));
    }

    //METODO - LOG DE TODOS LOS USUARIOS
    public void logAllUsers() {

        String db_user            = context.getString(R.string.db_user);
        String db_score           = context.getString(R.string.db_score);
        String db_user_error      = context.getString(R.string.db_user_error);
        String db_users_not_found = context.getString(R.string.db_users_not_found);


        db.collection("usuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (var doc : queryDocumentSnapshots.getDocuments()) {
                            String nombre = doc.getString("nombre");
                            int puntuacion = doc.getLong("puntuacion").intValue();
                            Log.d("Firestore", db_user + nombre + db_score + puntuacion);
                        }
                    }
                    else {
                        Log.d("Firestore", db_users_not_found);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", db_user_error, e));
    }

    //CALLBACKS
    public interface VerificarUsuarioCallback {
        void onUsuarioVerificado(boolean existe);
    }

    public interface ObtenerPuntuacionCallback {
        void onPuntuacionObtenida(int puntuacion);
    }
}
