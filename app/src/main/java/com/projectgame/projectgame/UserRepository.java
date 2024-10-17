package com.projectgame.projectgame;

import android.content.Context;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UserRepository {
    private BaseDeDatosHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new BaseDeDatosHelper(context);
    }

    // Método para verificar si el usuario existe
    public Single<Boolean> verificarUsuario(String nombre, String contraseña) {
        return Single.create(emitter -> {
            boolean existe = dbHelper.verificarUsuario(nombre, contraseña);
            emitter.onSuccess(existe);
        });
    }

    // Método para crear un nuevo usuario
    public Completable crearUsuario(String nombre, String contraseña) {
        return Completable.create(emitter -> {
            boolean result = dbHelper.crearUsuario(nombre, contraseña);
            if (result) {
                emitter.onComplete();
            } else {
                emitter.onError(new Exception("Error al crear el usuario"));
            }
        });
    }
}
