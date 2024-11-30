package com.projectgame.projectgame;

import android.content.Context;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UserRepository {
    //ATRIBUTOS
    private BaseDeDatosHelper dbHelper;
    private final Context context;

    //CONSTRUCTOR
    public UserRepository(Context context) {
        this.context = context; // Inicializar el contexto
        dbHelper = new BaseDeDatosHelper(context);
    }

    //METODO - Verifica si el usuario existe
    public Single<Boolean> verificarUsuario(String nombre, String contraseña) {
        return Single.create(emitter -> {
            boolean existe = dbHelper.verificarUsuario(nombre, contraseña);
            emitter.onSuccess(existe);
        });
    }

    //METODO - Crear nuevo usuario
    public Completable crearUsuario(String nombre, String contraseña) {

        String udp_not_location = context.getString(R.string.udp_not_location);
        return Completable.create(emitter -> {
            int puntuacionInicial = 0; // O cualquier valor que desees usar
            boolean result = dbHelper.crearUsuario(nombre, contraseña, puntuacionInicial);
            if (result) {
                emitter.onComplete();
            }
            else {
                emitter.onError(new Exception(udp_not_location));
            }
        });
    }

}
