package com.projectgame.projectgame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaseDeDatosHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "usuarios.db";
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla y las columnas
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_CONTRASEÑA = "contraseña";
    public static final String COLUMN_PUNTUACION = "puntuacion"; // Nueva columna para las puntuaciones

    // Consulta para crear la tabla
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT, " +
                    COLUMN_CONTRASEÑA + " TEXT, " +
                    COLUMN_PUNTUACION + " INTEGER DEFAULT 0);";

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
        boolean userCreated = resultado != -1; // Devuelve true si la inserción fue exitosa

        // Registra el resultado de la inserción
        if (userCreated) {
            Log.d("DB_INFO", "Usuario creado correctamente.");
            logAllUsers(); // Registra todos los usuarios después de la inserción
        } else {
            Log.d("DB_INFO", "Error al crear el usuario.");
        }

        return userCreated;
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

    // Método para registrar todos los usuarios en el logcat
    public void logAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase(); // Abrir la base de datos en modo lectura
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS, null); // Realizar la consulta

        // Verificar si hay resultados
        if (cursor.moveToFirst()) {
            do {
                // Obtener el índice de las columnas
                int nombreIndex = cursor.getColumnIndex(COLUMN_NOMBRE);
                int puntuacionIndex = cursor.getColumnIndex(COLUMN_PUNTUACION);

                // Verifica que los índices sean válidos
                if (nombreIndex != -1 && puntuacionIndex != -1) {
                    // Recuperar el nombre y la puntuación de cada registro
                    String nombre = cursor.getString(nombreIndex);
                    int puntuacion = cursor.getInt(puntuacionIndex);

                    // Imprimir los datos en el logcat
                    Log.d("DB_INFO", "Nombre: " + nombre + ", Puntuación: " + puntuacion);
                } else {
                    Log.d("DB_INFO", "Una o más columnas no existen en el cursor.");
                }
            } while (cursor.moveToNext()); // Mover al siguiente registro
        } else {
            // Si no hay registros, imprimir un mensaje
            Log.d("DB_INFO", "No hay usuarios en la base de datos.");
        }

        cursor.close(); // Cerrar el cursor
        db.close(); // Cerrar la base de datos
    }

}
