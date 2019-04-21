package com.example.wesley.fitness;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "fitness.db";//Create database
    public static final String TABLE_NAME = "user";//Create user table
    public static final String COL_1 ="ID";// Column name for auto increment
    public static final String COL_2 ="username";// Column name
    public static final String COL_3 ="password";// Column name
    public static final String COL_4 ="distance";// Column name
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    //Function for creating table
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists "+TABLE_NAME+ " (ID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, distance DOUBLE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME); //Drop older table if exists
        onCreate(db);
    }
    //Function for get the user/s data and order by their feets with DESC
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ TABLE_NAME + " order by " + COL_4 + " DESC ",null);
        return res;
    }
//    public Cursor checkUser(String username) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery("select * from "+ TABLE_NAME + " where " + COL_2 + " = "+ username,null);                     //The sort order
//        return cursor;
//    }
}
