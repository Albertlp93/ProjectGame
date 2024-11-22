package com.projectgame.projectgame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaseDeDatosHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "usuarios.db";
    private static final int DATABASE_VERSION = 2; // Incrementamos la versión para agregar una nueva tabla

    // Tabla de usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_CONTRASEÑA = "contraseña";
    public static final String COLUMN_PUNTUACION = "puntuacion";

    // Tabla de ubicaciones
    public static final String TABLE_UBICACIONES = "ubicaciones";
    public static final String COLUMN_UBICACION_ID = "id";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_USUARIO_ID = "usuario_id";

    // Consultas para crear tablas
    private static final String CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT, " +
                    COLUMN_CONTRASEÑA + " TEXT, " +
                    COLUMN_PUNTUACION + " INTEGER DEFAULT 0);";

    private static final String CREATE_TABLE_UBICACIONES =
            "CREATE TABLE " + TABLE_UBICACIONES + " (" +
                    COLUMN_UBICACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LATITUD + " REAL, " +
                    COLUMN_LONGITUD + " REAL, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    COLUMN_USUARIO_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_USUARIO_ID + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_ID + "));";

    public BaseDeDatosHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USUARIOS); // Crear la tabla de usuarios
        db.execSQL(CREATE_TABLE_UBICACIONES); // Crear la tabla de ubicaciones
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_UBICACIONES); // Agregar la tabla de ubicaciones en la versión 2
        }
    }

    // Método para insertar ubicaciones en la base de datos
    public void insertarUbicacion(double latitud, double longitud, int usuarioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUD, latitud);
        values.put(COLUMN_LONGITUD, longitud);
        values.put(COLUMN_USUARIO_ID, usuarioId);

        long resultado = db.insert(TABLE_UBICACIONES, null, values);

        if (resultado != -1) {
            Log.d("DB_INFO", "Ubicación guardada: Latitud=" + latitud + ", Longitud=" + longitud + ", UsuarioID=" + usuarioId);
        } else {
            Log.e("DB_ERROR", "Error al guardar la ubicación.");
        }
        db.close();
    }

    // Método para obtener todas las ubicaciones de un usuario
    public int obtenerPuntuacion(String nombreUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        int puntuacion = 30; // Valor predeterminado si no se encuentra el usuario

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PUNTUACION + " FROM " + TABLE_USUARIOS +
                " WHERE " + COLUMN_NOMBRE + "=?", new String[]{nombreUsuario});

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_PUNTUACION);
            if (columnIndex != -1) {
                puntuacion = cursor.getInt(columnIndex);
            } else {
                Log.e("DB_ERROR", "La columna 'puntuacion' no existe en el cursor.");
            }
        } else {
            Log.d("DB_INFO", "Usuario no encontrado, puntuación inicial establecida en 30.");
        }

        cursor.close();
        db.close();
        return puntuacion;
    }


    // Otros métodos (verificarUsuario, crearUsuario, etc.) permanecen iguales
    public boolean verificarUsuario(String nombre, String contraseña) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " +
                COLUMN_NOMBRE + "=? AND " + COLUMN_CONTRASEÑA + "=?", new String[]{nombre, contraseña});

        boolean existeUsuario = cursor.moveToFirst();
        cursor.close();
        return existeUsuario;
    }

    public boolean crearUsuario(String nombre, String contraseña, int puntuacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_CONTRASEÑA, contraseña);
        values.put(COLUMN_PUNTUACION, puntuacion > 0 ? puntuacion : 30); // Guarda la puntuación

        long resultado = db.insert(TABLE_USUARIOS, null, values);
        boolean userCreated = resultado != -1;

        if (userCreated) {
            Log.d("DB_INFO", "Usuario creado correctamente.");
            logAllUsers(); // Registra todos los usuarios después de la inserción
        } else {
            Log.d("DB_INFO", "Error al crear el usuario.");
        }

        return userCreated;
    }

    public boolean actualizarPuntuacion(String nombre, int nuevaPuntuacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PUNTUACION, nuevaPuntuacion);

        int rowsAffected = db.update(TABLE_USUARIOS, values, COLUMN_NOMBRE + "=?", new String[]{nombre});
        return rowsAffected > 0;
    }

    public void logAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS, null);

        if (cursor.moveToFirst()) {
            do {
                int nombreIndex = cursor.getColumnIndex(COLUMN_NOMBRE);
                int puntuacionIndex = cursor.getColumnIndex(COLUMN_PUNTUACION);

                if (nombreIndex != -1 && puntuacionIndex != -1) {
                    String nombre = cursor.getString(nombreIndex);
                    int puntuacion = cursor.getInt(puntuacionIndex);
                    Log.d("DB_INFO", "Nombre: " + nombre + ", Puntuación: " + puntuacion);
                } else {
                    Log.d("DB_INFO", "Una o más columnas no existen en el cursor.");
                }
            } while (cursor.moveToNext());
        } else {
            Log.d("DB_INFO", "No hay usuarios en la base de datos.");
        }

        cursor.close();
        db.close();
    }
}
