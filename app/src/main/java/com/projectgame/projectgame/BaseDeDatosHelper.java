package com.projectgame.projectgame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaseDeDatosHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "usuarios.db";
    private static final int DATABASE_VERSION = 2; //Numero de tablas existentes en BBDD (INCREMENTAR EN CASO DE USARR AS TABLAS)
    private final Context context;

//TABLAS - DEFINICION
    //USUARIOS
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_CONTRASEÑA = "contraseña";
    public static final String COLUMN_PUNTUACION = "puntuacion";

    //UBICACIONES
    public static final String TABLE_UBICACIONES = "ubicaciones";
    public static final String COLUMN_UBICACION_ID = "id";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_USUARIO_ID = "usuario_id";

//TABLAS - CONSULTAS DE CREACION
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

    //METODO - CONSTRUCTOR
    public BaseDeDatosHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

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

        //INICIALIZACION TEXTOS
        String db_locationMessage = context.getString(R.string.db_location_saved, String.valueOf(latitud), String.valueOf(longitud), String.valueOf(usuarioId));
        String db_errorMessage = context.getString(R.string.db_error);

        long resultado = db.insert(TABLE_UBICACIONES, null, values);

        if (resultado != -1) {
            Log.d("DB_INFO", db_locationMessage);
        }
        else {
            Log.e("DB_ERROR", db_errorMessage);
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
