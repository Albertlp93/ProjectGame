package com.projectgame.projectgame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDeDatosHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "usuarios.db";
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla y las columnas
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_CONTRASEÑA = "contraseña";
    public static final String COLUMN_PUNTUACION = "puntuacion"; // Nueva columna para las puntuaciones

    // Actualiza la consulta para crear la tabla
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT, " +
                    COLUMN_CONTRASEÑA + " TEXT, " + // Se mantiene la contraseña
                    COLUMN_PUNTUACION + " INTEGER DEFAULT 0);"; // Agrega la puntuación

    public BaseDeDatosHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE); // Crear la tabla usuarios
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    // Método para verificar si el usuario existe
    public boolean verificarUsuario(String nombre, String contraseña) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " +
                COLUMN_NOMBRE + "=? AND " + COLUMN_CONTRASEÑA + "=?", new String[]{nombre, contraseña});

        boolean existeUsuario = cursor.moveToFirst();
        cursor.close();
        return existeUsuario;
    }

    // Método para insertar un nuevo usuario
    public boolean crearUsuario(String nombre, String contraseña, int puntuacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_CONTRASEÑA, contraseña);
        values.put(COLUMN_PUNTUACION, puntuacion); // Guarda la puntuación

        long resultado = db.insert(TABLE_USUARIOS, null, values);
        return resultado != -1; // Devuelve true si la inserción fue exitosa
    }

    // Método para agregar un nuevo usuario (si solo quieres guardar el nombre)
    public boolean agregarUsuario(String nombreUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombreUsuario);

        long resultado = db.insert(TABLE_USUARIOS, null, values);
        db.close();
        return resultado != -1;
    }

    // Método para actualizar la puntuación de un usuario
    public boolean actualizarPuntuacion(String nombre, int nuevaPuntuacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PUNTUACION, nuevaPuntuacion);

        int rowsAffected = db.update(TABLE_USUARIOS, values,
                COLUMN_NOMBRE + "=?", new String[]{nombre});
        return rowsAffected > 0; // Devuelve true si la actualización fue exitosa
    }

}
