package com.example.wesley.fitness;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOError;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// Using SQlite to store local data in the local mechine

public class Register extends AppCompatActivity {
    SQLiteOpenHelper openHelper;
    SQLiteDatabase db;
    private EditText username, password, c_password;
    private Button btn_register, btn_login;
    private ProgressBar loading;

    String uname;
    String pword;
    String cpword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        openHelper = new DatabaseHelper(this);
        loading = findViewById(R.id.loading);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        c_password = findViewById(R.id.c_password);




    }
    public void onClickRegister(View v) throws ExecutionException, InterruptedException {

        uname = username.getText().toString().trim();
        pword = password.getText().toString().trim();
        cpword = c_password.getText().toString().trim();
        Log.i("test", "test");

        try {

            //Throw the error in the Toast if user make some mistakes.
            //Don't fill all fields
            if (uname.matches("")|| pword.matches("")) {
                Toast.makeText(getApplicationContext(),"Please fill all fields!",Toast.LENGTH_LONG).show();
            }else if(!pword.equals(cpword)){
            //Password and comfirm password don't match
                Toast.makeText(getApplicationContext(),"Password doesn't match!",Toast.LENGTH_LONG).show();
            }else{
                //If there isn't any error, insert data to SQlite database

                db = openHelper.getWritableDatabase();
                inserdata(uname, pword, 0);
                Toast.makeText(getApplicationContext(), "Register successgully!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Register.this, login.class);
                finish();
                startActivity(intent);
            }

        } catch (IOError e) {
            e.printStackTrace();
        }
    }

    //Go to Login in activity
    public void onClickGoLogin(View v){

        Intent intent = new Intent(
                this,login.class);
        finish();
        startActivity(intent);
    }

    //Function for inserting data
    //Please go to see DatabaseHelper.class for more detail
    public void inserdata(String uname, String pword, double feet){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COL_2, uname);
        contentValues.put(DatabaseHelper.COL_3, pword);
        contentValues.put(DatabaseHelper.COL_4, feet);
        long id = db.insert(DatabaseHelper.TABLE_NAME, null, contentValues);

    }


}
