package com.example.williamdunnett.mapstest2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final int REQUEST_LOCATION = 1;
    private Button get_loc_btn;
    public Criteria criteria;
    public String bestProvider;
    RequestQueue requestQueue;
    private ProgressBar spinner;


    String newUserIDURL = "http://10.128.116.10/tutorial/newUserID.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        final SharedPreferences prefs = this.getSharedPreferences(
                "com.example.williamdunnett.mapstest2", Context.MODE_PRIVATE);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        get_loc_btn = (Button)  findViewById(R.id.button1);
        get_loc_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Log.d("onCreate", "button pressed");
                getLocation();

                int userIDTest = prefs.getInt("usersID", 0 );
                Log.d("onCreate", String.valueOf(userIDTest));
            }
        });


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
         */

       // getLocation();
    }

    public class ViewDialog {

        public void showDialog(Activity activity, String msg) {

            //TODO get shared prefs working
            
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.new_user_dialog);

            final EditText name = (EditText)findViewById(R.id.insertName);
            final Spinner typeMovement = (Spinner) findViewById(R.id.insertMovement);
            final Spinner sizeGroup = (Spinner) findViewById(R.id.insert_group_size);
            final ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar1);
            final Button newUserbtn = (Button)  findViewById(R.id.newUserbtn);
           // final SharedPreferences prefs = this.getSharedPreferences(
                    //"com.example.williamdunnett.mapstest2", Context.MODE_PRIVATE);

            Button newUserBtn = (Button) dialog.findViewById(R.id.newUserbtn);

            newUserBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final String nameUser, sizeGroupUser, typeMovementUser;
                    // Do something in response to newUserbtn click
                    Log.d("onStart", "newUserbtn pressed");
                    dialog.dismiss();
                    spinner.setVisibility(View.VISIBLE);
                    nameUser = name.getText().toString();
                    sizeGroupUser = String.valueOf(sizeGroup.getSelectedItem());
                    typeMovementUser = String.valueOf(typeMovement.getSelectedItem());


                    StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                            newUserIDURL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("Result", "response: " + response);

                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        //Log.d("Result", obj.toString());
                                        int resultID = obj.getInt("userID");    //result is key for which you need to retrieve data
                                        Log.d("Result", "userID: " + resultID);
                                        //prefs.edit().putInt("usersID", resultID).apply();
                                        //TODO store userID in userPrefs for use in post coordinates
                                    } catch (Throwable t) {
                                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                                    }

                                    spinner.setVisibility(View.GONE);
                                    requestQueue.stop();
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Result", "something went wrong");
                            spinner.setVisibility(View.GONE);
                            requestQueue.stop();
                        }
                    }) {

                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded; charset=UTF-8";
                        }

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {


                            Map<String, String> postParam = new HashMap<String, String>();

                            postParam.put("usersName",nameUser);
                            postParam.put("sizeOfParty",sizeGroupUser);
                            postParam.put("typeOfUser", typeMovementUser);

                            return postParam;
                        }

                    };

                    requestQueue.add(jsonObjRequest);

                }
            });
            dialog.show();

        }
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        ViewDialog alertDialoge = new ViewDialog();
        alertDialoge.showDialog(this, "PUT DIALOG TITLE");

        /*
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.new_user_dialog);
        dialog.setTitle("Dialog box");

        final EditText name = (EditText)findViewById(R.id.insertName);
        final Spinner typeMovement = (Spinner) findViewById(R.id.insertMovement);
        final Spinner sizeGroup = (Spinner) findViewById(R.id.insert_group_size);
        final ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar1);
        final Button newUserbtn = (Button)  findViewById(R.id.newUserbtn);
        final SharedPreferences prefs = this.getSharedPreferences(
                "com.example.williamdunnett.mapstest2", Context.MODE_PRIVATE);

        newUserbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String nameUser, sizeGroupUser, typeMovementUser;
                // Do something in response to newUserbtn click
                Log.d("onStart", "newUserbtn pressed");
                dialog.dismiss();
                spinner.setVisibility(View.VISIBLE);
                nameUser = name.getText().toString();
                sizeGroupUser = String.valueOf(sizeGroup.getSelectedItem());
                typeMovementUser = String.valueOf(typeMovement.getSelectedItem());


                StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                        newUserIDURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("Result", "response: " + response);

                                try {
                                    JSONObject obj = new JSONObject(response);
                                    //Log.d("Result", obj.toString());
                                    int resultID = obj.getInt("userID");    //result is key for which you need to retrieve data
                                    //Log.d("Result", "userID: " + resultID);
                                    prefs.edit().putInt("usersID", resultID).apply();
                                    //TODO store userID in userPrefs for use in post coordinates
                                } catch (Throwable t) {
                                    Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                                }

                                spinner.setVisibility(View.GONE);
                                requestQueue.stop();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Result", "something went wrong");
                        spinner.setVisibility(View.GONE);
                        requestQueue.stop();
                    }
                }) {

                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {


                        Map<String, String> postParam = new HashMap<String, String>();

                        postParam.put("usersName",nameUser);
                        postParam.put("sizeOfParty",sizeGroupUser);
                        postParam.put("typeOfUser", typeMovementUser);

                        return postParam;
                    }

                };

                requestQueue.add(jsonObjRequest);

            }
        });


        dialog.show();
        */
    }


    protected void getLocation() {
        if (isLocationEnabled(MapsActivity.this)) {
            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:

            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("getLocation", "Permission needed");
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

            } else {
                Location location = locationManager.getLastKnownLocation(bestProvider);
                if (location != null) {
                    Log.e("TAG", "GPS is on");
                    double latti = location.getLatitude();
                    double longi = location.getLongitude();
                    //double latti = 80;
                    //double longi = 80;
                    String lattitude = String.valueOf(latti);
                    String longitude = String.valueOf(longi);

                    Log.d("getLocation", "Lattitude = " + lattitude +
                            " Longitude = " + longitude);

                    // Add a marker in Sydney and move the camera
                    LatLng currentLocation = new LatLng(latti, longi);
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("currentLocation"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

                }
                else{
                    //This is what you need:

                   // locationManager.requestLocationUpdates(bestProvider,Long.valueOf(1000) ,Float.valueOf(0), this);
                }
            }

        }
        else
        {
            //prompt user to enable location....
            //.................
        }
    }

    public static boolean isLocationEnabled(Context context)
    {
        int locationMode = 0;
        String locationProviders;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            try
            {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else
        {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
