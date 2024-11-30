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

    //METODO - INSERTAR UBICACIONES EN LA BASE DE DATOS
    public void insertarUbicacion(double latitud, double longitud, int usuarioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String db_locationMessage = context.getString(R.string.db_location_saved, String.valueOf(latitud), String.valueOf(longitud), String.valueOf(usuarioId));
        String db_errorMessage = context.getString(R.string.db_error);

        values.put(COLUMN_LATITUD, latitud);
        values.put(COLUMN_LONGITUD, longitud);
        values.put(COLUMN_USUARIO_ID, usuarioId);

        long resultado = db.insert(TABLE_UBICACIONES, null, values);

        if (resultado != -1) {
            Log.d("DB_INFO", db_locationMessage);
        }
        else {
            Log.e("DB_ERROR", db_errorMessage);
        }
        db.close();
    }

    //METODO - OBTENER UBICACIONES DEL USUARIO
    public int obtenerPuntuacion(String nombreUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();

        int puntuacion = 30; // Valor predeterminado si no se encuentra el usuario
        String db_score_not_found = context.getString(R.string.db_score_not_found);
        String db_user_not_found = context.getString(R.string.db_user_not_found);


        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PUNTUACION + " FROM " + TABLE_USUARIOS + " WHERE " + COLUMN_NOMBRE + "=?", new String[]{nombreUsuario});

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_PUNTUACION);
            if (columnIndex != -1) {
                puntuacion = cursor.getInt(columnIndex);
            }
            else {
                Log.e("DB_ERROR", db_score_not_found);
            }
        }
        else {
            Log.d("DB_INFO", db_user_not_found);
        }

        cursor.close();
        db.close();

        return puntuacion;
    }


    //METODO - VERIFICAR USUARIO
    public boolean verificarUsuario(String nombre, String contraseña) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " + COLUMN_NOMBRE + "=? AND " + COLUMN_CONTRASEÑA + "=?", new String[]{nombre, contraseña});
        boolean existeUsuario = cursor.moveToFirst();
        cursor.close();

        return existeUsuario;
    }

    //METODO - CREAR USUARIO
    public boolean crearUsuario(String nombre, String contraseña, int puntuacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String db_user_created = context.getString(R.string.db_user_created);
        String db_user_not_created = context.getString(R.string.db_user_not_created);

        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_CONTRASEÑA, contraseña);
        values.put(COLUMN_PUNTUACION, puntuacion > 0 ? puntuacion : 30); // Guarda la puntuación

        long resultado = db.insert(TABLE_USUARIOS, null, values);
        boolean userCreated = resultado != -1;

        if (userCreated) {
            Log.d("DB_INFO", db_user_created);
            logAllUsers(); // Registra todos los usuarios después de la inserción
        }
        else {
            Log.d("DB_INFO", db_user_not_created);
        }

        return userCreated;
    }

    //METODO - ACTUALIZAR PUNTUACION
    public boolean actualizarPuntuacion(String nombre, int nuevaPuntuacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_PUNTUACION, nuevaPuntuacion);

        int rowsAffected = db.update(TABLE_USUARIOS, values, COLUMN_NOMBRE + "=?", new String[]{nombre});

        return rowsAffected > 0;
    }

    //METODO LOGEO DE USUARIO
    public void logAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS, null);

        String db_name = context.getString(R.string.db_user_created);
        String db_score = context.getString(R.string.db_user_not_created);
        String db_columns_found = context.getString(R.string.db_columns_found);
        String db_no_users = context.getString(R.string.db_no_users);

        if (cursor.moveToFirst()) {
            do {
                int nombreIndex = cursor.getColumnIndex(COLUMN_NOMBRE);
                int puntuacionIndex = cursor.getColumnIndex(COLUMN_PUNTUACION);

                if (nombreIndex != -1 && puntuacionIndex != -1) {
                    String nombre = cursor.getString(nombreIndex);
                    int puntuacion = cursor.getInt(puntuacionIndex);

                    Log.d("DB_INFO", db_name  + nombre +db_score + puntuacion);
                }
                else {
                    Log.d("DB_INFO", db_columns_found);
                }
            }
            while (cursor.moveToNext());
        }
        else {
            Log.d("DB_INFO", db_no_users);
        }

        cursor.close();
        db.close();
    }
}
