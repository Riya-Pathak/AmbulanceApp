package com.example.shubhamr.myfirstmapactivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainControlActivity extends AppCompatActivity {

    Button available,navigatevictim;
    GPSTracker gps;
    String availablity = "Y";
  //  String id = "AMBSHARDA002";
    AlertDialog.Builder builder;
 double mylatitude,mylongitude;
    String idofambulance;

    public static String filename = "MySharedString";

    public SharedPreferences someData;





    String availeee = "http://35.154.177.242/ambulance/avail.php";
    Session session;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_control);

        session = new Session(this);

    


        gps = new GPSTracker(MainControlActivity.this);

        available = (Button) findViewById(R.id.available);
        builder = new AlertDialog.Builder(MainControlActivity.this);
        navigatevictim = (Button) findViewById(R.id.navigate);

        //one time code
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);

        //shared prefrence to save data
        someData = getSharedPreferences(filename,0);





        //code for lines to run once
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun)
        {
            Bundle bundle=getIntent().getExtras();
            idofambulance = bundle.getString("idofambulance");
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            editor.commit();
            firstuse();
        }





        available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetworkConnected()) {


                    someData = getSharedPreferences(filename, 0);
                    final String idofambulancestring = someData.getString("SharedString","couldn't load data");



                        mylocation();
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, availeee,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        try {
                                            JSONArray jsonArray = new JSONArray(response);
                                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                                        } catch (JSONException e) {
                                            e.printStackTrace();

                                        }

                                    }


                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                error.printStackTrace();


                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("avail", availablity);
                                params.put("id", idofambulancestring);
                                params.put("latitude", String.valueOf(mylatitude));
                                params.put("longitude", String.valueOf(mylongitude));


                                return params;
                            }
                        };
                        MySingleton.getInstance(MainControlActivity.this).addToRequestque(stringRequest);
                        Toast.makeText(getApplicationContext(), "Sending Request...", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Request send Successfully", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainControlActivity.this,UserSens.class);

                    Bundle bundle = new Bundle();
                    bundle.putDouble("latitudeofambulance",mylatitude);
                    bundle.putDouble("longitudeofambulance",mylongitude);
                    intent.putExtras(bundle);

                    startActivity(intent);



                }



                else
                {
                    displayAlert("Turn On Internet");
                }


            }
        });




    }

    private void firstuse() {

        SharedPreferences.Editor editor = someData.edit();
        editor.putString("SharedString", idofambulance);
        editor.commit();
    }

    private void mylocation() {

        // create class object


        gps = new GPSTracker(MainControlActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            mylatitude = gps.getLatitude();
            mylongitude = gps.getLongitude();
            Log.i("LoginSuccess","value of latitude is"+mylatitude);
            Log.i("LoginSuccess","value of longitude is"+mylongitude);






          //  Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + mylatitude + "\nLong: " + mylongitude, Toast.LENGTH_LONG).show();
        }else{



            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();

        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.logout:
                session.setLoggedin(false);
                startActivity(new Intent(this, Login.class));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private boolean isNetworkConnected() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public void displayAlert(String message)
    {
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
