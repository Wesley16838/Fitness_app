package com.example.wesley.fitness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOError;
import java.util.concurrent.ExecutionException;

public class login extends AppCompatActivity {
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Button btn_login, btn_signup;
    EditText uname, pword;
    Cursor cursor;
    String Uname, Pword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        openHelper = new DatabaseHelper(this);
        db = openHelper.getReadableDatabase();
        btn_login =  findViewById(R.id.btn_login);
        uname = findViewById(R.id.username);
        pword = findViewById(R.id.password);
    }

    //Go to dashboard activity
    public void onClicklogin(View v) throws ExecutionException, InterruptedException {

        Uname = uname.getText().toString().trim();
        Pword = pword.getText().toString().trim();

        try {
            //If user don't fill username's field ot password's field
            if (Uname.matches("")|| Pword.matches("")) {
                Toast.makeText(getApplicationContext(),"Please fill all fields!",Toast.LENGTH_LONG).show();
            }else{
            //Get user's data and store in the local storage
                cursor = db.rawQuery("Select * from "+DatabaseHelper.TABLE_NAME + " WHERE " + DatabaseHelper.COL_2 + " =? AND " + DatabaseHelper.COL_3 + " =? ", new String[]{Uname,Pword});
                if(cursor != null){
                    if(cursor.getCount()>0){
                        while(cursor.moveToNext()){
                            String username = cursor.getString(1);
                            String password = cursor.getString(2);
                            Double feets = cursor.getDouble(3);
                            String feet = String.valueOf(feets);
                            Log.i("cursur",cursor.toString());
                            Toast.makeText(getApplicationContext(), "Login successfully!", Toast.LENGTH_LONG).show();
                            SharedPreferences shared = getSharedPreferences("info", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.putString("feet", feet);//convert double to string
                            editor.apply();
                            Intent intent = new Intent(login.this, dashboard.class);
                            finish();
                            startActivity(intent);
                        }

                    }else{
                        Toast.makeText(getApplicationContext(), "Account doesn't exist!", Toast.LENGTH_LONG).show();
                    }
                }
            }

        } catch (IOError e) {
            e.printStackTrace();
        }
    }
    public void onClickGoRegister(View v){

        Intent intent = new Intent(
                this,Register.class);
        finish();
        startActivity(intent);
    }
}
