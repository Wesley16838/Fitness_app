package com.example.wesley.fitness;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
import java.util.List;


//I userd google api to locate the location for each 3 seconds and calculate their distance
//I didn't choose Accelerometer because I thought Accelerometer is suitable for calculating how many steps not how long.
public class dashboard extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    SQLiteOpenHelper openHelper;
    SQLiteDatabase db;
    DatabaseHelper myDb;
    private TextView textView, textView_lat, textView_lng, textView_username, textView_milestone, textView_reminder;

    private Button btnGetLastLocation, VIEWALL;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;

    private boolean getService = false;     //google location service

    int stone = 1000;///basic
    int count_num = 0; /// this variable is used to test whether user walk in 1 hr or not
    int count = 0; /////// this variable is used to identify the user log in first time or not
    double tmp_lat; ////// temporary variable for lattitude
    double tmp_lng; ////// temporary variable for longtitude
    Boolean _stop = true; /// To stop Loop for executing function when log out

    private LocationManager status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        myDb = new DatabaseHelper(this);

        SharedPreferences shared = getSharedPreferences("info", Context.MODE_PRIVATE);//Get the data from local storage
        String username = shared.getString("username","");
        String feet = shared.getString("feet","");

        Double feets = Double.parseDouble(feet);//////////
        openHelper = new DatabaseHelper(this);
        btnGetLastLocation = (Button) findViewById(R.id.locate);
        btnGetLastLocation.setOnClickListener(btnGetLastLocationOnClickListener);
        VIEWALL = (Button) findViewById(R.id.viewall);
        textView = (TextView) findViewById(R.id.mile);
        textView_lng = (TextView) findViewById(R.id.lng);
        textView_lat = (TextView) findViewById(R.id.lat);
        textView_username = (TextView) findViewById(R.id.username);
        textView_milestone = (TextView) findViewById(R.id.milestone);
        textView_reminder = (TextView) findViewById(R.id.reminder);
        status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        textView_username.setText(username);
        textView.setText(feet);
        viewAll();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //if no permission for google location service

            requestLocationPermission(); // ask for turn on permission
            return;
        }

        //Execute the function for each 3 seconds
        Thread t=new Thread(){


            @Override
            public void run(){

                while(_stop == true){

                    try {

                            Thread.sleep(3000);  //1000ms = 1 sec

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    getMyLocation();
//                                    Log.i("test", "test1");
//                                textView.setText(String.valueOf(num));/////result mile
                                }
                            });


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        t.start();

    }

    //The function for all users to create leaderboard
    //Leaderboard between all local users
    public void viewAll() {

        VIEWALL.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDb.getAllData();
                        if(res.getCount() == 0) {
                            // show message
                            showMessage("Error","Nothing found");
                            return;
                        }
                        int i = 1;
                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {

                            buffer.append("Ranking :"+ String.valueOf(i) + "\n");
                            buffer.append("Name :"+ res.getString(1)+"\n");
                            buffer.append("Feets :"+ (int)res.getDouble(3)+"\n");
                            i++;
                        }

                        // Show all data
                        showMessage("Leaderboard",buffer.toString());
                    }
                }
        );
    }
    // Leaderboard between all local users
    public void showMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
    ///Upgrade mile data in SQlite database and local storage when log out
    public void onClicklogout(View v){
        SharedPreferences shared = getSharedPreferences("info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.clear();
        editor.commit();
        _stop = false;
        Intent intent = new Intent(
                this,login.class);
        finish();
        startActivity(intent);
    }

    //Method to calculate the distance between two location with longtitude and lattitude
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            return (dist*5280); // transfer mile to feet
        }
    }


    private void requestLocationPermission(){
        // when version 6.0 <
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // if permission
            int hasPermission = checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION);
            int hasPermission2 = checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION);


            // if no permission
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION_PERMISSION);
            }

            else {


            }
        }
    }

    View.OnClickListener btnGetLastLocationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(status.isProviderEnabled(LocationManager.GPS_PROVIDER)&& status.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {

                if(mGoogleApiClient != null){
                    if(mGoogleApiClient.isConnected()){
                        getMyLocation();
                    }else{
                        Toast.makeText(dashboard.this,
                                "!mGoogleApiClient.isConnected()", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(dashboard.this,
                            "mGoogleApiClient == null", Toast.LENGTH_LONG).show();
                }


            } else {
                Toast.makeText(dashboard.this,"Please turn on google location",Toast.LENGTH_LONG).show();
                getService = true; //turn on google location service
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }


        }
    };

    //Get and store locally the distance walked
    //update data for each 3 seconds
    public void upgradedata(double mile){
        SharedPreferences shared = getSharedPreferences("info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString("feet", String.valueOf(mile));//convert double to string
        editor.apply();
        String username = shared.getString("username","");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COL_4, mile);
        db = openHelper.getWritableDatabase();
        db.update(DatabaseHelper.TABLE_NAME, contentValues, "username" + " = ? " , new String [] {String.valueOf(username)});

    }


    //Get my current location
    //Feedback on achieving milestones (multiples of 1000 feet)
    //Whenever the person is at office, periodic reminder to stand up and walk every 1 hour.
    //Display daily statistics (in text format) on the main-screen.
    private void getMyLocation(){
        SharedPreferences shared = getSharedPreferences("info", Context.MODE_PRIVATE);
        String feet = shared.getString("feet","");


        if(feet != null && feet.length() > 0){
            try{
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                double feets = Double.parseDouble(feet);
                int t = (int)feets/1000;
                if(t<1){
                    stone = 1000;
                }else{
                    stone = t*1000;
                }
                if (mLastLocation != null) { // If google service works
                    double lat = mLastLocation.getLatitude();
                    double lng = mLastLocation.getLongitude();
                    if(count == 0){
                        //first location
                        tmp_lat = lat;
                        tmp_lng = lng;
                        Log.i("TEST123","1");
                        textView_lng.setText(
                                String.valueOf(lng));
                        textView_lat.setText(
                                String.valueOf(lat));
                        if(feets >= stone){ //Feedback on achieving milestones (multiples of 1000 feet)
                            textView_milestone.setText("Congratulation for your milestone: " + stone);
                            stone+=1000;
                        }
                        textView.setText(String.valueOf((int)feets));//Display daily statistics (in text format) on the main-screen.
                        Toast.makeText(dashboard.this,
                                String.valueOf(tmp_lat) + "\n"
                                        + String.valueOf(tmp_lng),
                                Toast.LENGTH_LONG).show();

                    }else{
                        Log.i("TEST123","2");
                        //second location or more
                        double dis = distance( tmp_lat , tmp_lng, lat, lng); //// Can change variable to test this app works or not --> 37.4219983, -122.083, 37.4219983, -122.084
                        Log.i("TEST123",String.valueOf(dis));
                        if(dis == 0){
                            count_num++;
                            if(count_num == 1200){  //Whenever the person is at office, periodic reminder to stand up and walk every 1 hour.
                                textView_reminder.setText("Hey, please stand up and walk!!!!!!");
                                count_num= 0;
                            }
                        }else{
                            textView_reminder.setText("");
                            count_num = 0;
                        }
                        feets = feets + dis;

                        textView.setText(String.valueOf((int)feets));//Display daily statistics (in text format) on the main-screen.

                        if(feets >= stone){
                            textView_milestone.setText("Contrat for your milestone " + stone);
                            stone+=1000;
                        }

                        tmp_lat = lat;
                        tmp_lng = lng;
                        Toast.makeText(dashboard.this,
                                String.valueOf(mLastLocation.getLatitude()) + "\n"
                                        + String.valueOf(mLastLocation.getLongitude()),
                                Toast.LENGTH_LONG).show();
                        upgradedata(feets);
                    }

                }else{
//                    Toast.makeText(dashboard.this,
//                            "mLastLocation == null",
//                            Toast.LENGTH_LONG).show();
                }
                count++;


            } catch (SecurityException e){
                Toast.makeText(dashboard.this,
                        "SecurityException:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(dashboard.this,
                "onConnectionSuspended: " + String.valueOf(i),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(dashboard.this,
                "onConnectionFailed: \n" + connectionResult.toString(),
                Toast.LENGTH_LONG).show();
    }
}