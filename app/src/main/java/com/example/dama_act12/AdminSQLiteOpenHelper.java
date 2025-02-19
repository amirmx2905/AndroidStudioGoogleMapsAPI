package com.example.dama_act12;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase BaseDeDatos) {
        BaseDeDatos.execSQL("create table personas(personaID integer primary key autoincrement, nombre text, sexo text, edad int, direccion text, latitud real, longitud real)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    public Cursor obtenerPersonas(SQLiteDatabase db) {
        return db.rawQuery("SELECT personaID, nombre, sexo, edad, latitud, longitud FROM personas", null);
    }
}
