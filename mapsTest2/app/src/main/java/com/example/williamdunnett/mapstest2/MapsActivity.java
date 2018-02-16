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
import android.location.LocationListener;
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
import android.widget.LinearLayout;
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
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.datatype.Duration;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final int REQUEST_LOCATION = 1;
    private Button get_loc_btn, update_location_btn;
    public Criteria criteria;
    public String bestProvider;
    RequestQueue requestQueue;
    public ProgressBar spinner;
    public Timer timer1;


    String newUserIDURL = "http://10.128.116.181/tutorial/newUserID.php";
    String updateUserLocationURL = "http://10.128.116.181/tutorial/postCoordinates.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        final SharedPreferences prefs = this.getSharedPreferences(
                "com.example.williamdunnett.mapstest2", Context.MODE_PRIVATE);

        ViewDialog alertDialoge = new ViewDialog();

        //If there is a profile already saved in prefs check if users wants to update it
        int usersID = prefs.getInt("usersID", 0);
        if( usersID != 0 ) {
            //profile saved check if user wants to reuse it
            alertDialoge.showDialog(this, "oldUserProfile");
        } else {
            //no profile previously saved get new one
            alertDialoge.showDialog(this, "newUser");
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        get_loc_btn = (Button)  findViewById(R.id.button1);

        /*
        get_loc_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Log.d("onCreate", "button pressed");
                getLocation();


            }
        });
        */

        update_location_btn  = (Button)  findViewById(R.id.button2);
        update_location_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Log.d("onCreate", "button pressed");
                UpdateLocation updateLocationServer = new UpdateLocation();
                updateLocationServer.updateServer(MapsActivity.this);

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

    public class UpdateLocation {

        public void updateServer(final Activity activity){

            final RequestQueue requestQueue1 = Volley.newRequestQueue(getApplicationContext());

           // requestQueue.getCache().clear();

            final SharedPreferences prefs = activity.getSharedPreferences(
                    "com.example.williamdunnett.mapstest2", Context.MODE_PRIVATE);

            final int userID = prefs.getInt("userID",0);
            final String lattitude = prefs.getString("lattitude", null);
            final String longitude = prefs.getString("longitude", null);

            Log.d("updateUsersLocation", "called");
            Log.d("userID", String.valueOf(userID));
            Log.d("lattitude", lattitude);
            Log.d("longitude", longitude);


            StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                    updateUserLocationURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Result updateuserLocation", "response: " + response);

                            try {
                                JSONObject obj = new JSONObject(response);
                                Log.d("Result", obj.toString());

                            } catch (Throwable t) {
                                Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                            }

                            //spinner.setVisibility(View.GONE);
                            requestQueue1.stop();
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Result", "something went wrong");
                   // spinner.setVisibility(View.GONE);
                    requestQueue1.stop();

                }
            }) {

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    //TODO This is not getting called for some reason
                    Log.d("getParams update coord", "called");
                    Map<String, String> postParam = new HashMap<String, String>();

                    Log.d("userID", String.valueOf(userID));
                    Log.d("lattitude", lattitude);
                    Log.d("longitude", longitude);

                    postParam.put("usersID",String.valueOf(userID));
                    postParam.put("lattitude",lattitude);
                    postParam.put("longitude", longitude);

                    return postParam;
                }

            };

            requestQueue1.add(jsonObjRequest);


        }

    }

    public class ViewDialog {

        public void showDialog(final Activity activity, String msg) {

            final Dialog dialog = new Dialog(activity);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.new_user_dialog);

            final LinearLayout showOld = dialog.findViewById(R.id.oldUserProfile);
            final LinearLayout showNew = dialog.findViewById(R.id.newUserProfile);

            final EditText name = dialog.findViewById(R.id.insertName);

            final Spinner typeMovement = (Spinner) dialog.findViewById(R.id.insertMovement);
            final Spinner sizeGroup = (Spinner) dialog.findViewById(R.id.insert_group_size);

            final ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar1);

            Button newUserBtn = (Button) dialog.findViewById(R.id.newUserbtn);
            Button changeUserBtn = dialog.findViewById(R.id.changeUserBtn);
            Button keepUserBtn = dialog.findViewById(R.id.keepUserBtn);

            TextView oldName = dialog.findViewById(R.id.oldUserProfileName);
            TextView oldGroupSize = dialog.findViewById(R.id.oldUserProfileGroupSize);
            TextView oldMethodMovement = dialog.findViewById(R.id.oldUserProfileMovement);

           final SharedPreferences prefs = activity.getSharedPreferences(
                   "com.example.williamdunnett.mapstest2", Context.MODE_PRIVATE);

            Log.d("showDialog", "msg: " + msg);

            if(msg.equals("oldUserProfile")) {
                showOld.setVisibility(View.VISIBLE);
                showNew.setVisibility(View.GONE);

                oldName.setText(prefs.getString("oldUserProfileName", ""));
                oldGroupSize.setText(prefs.getString("oldUserProfileGroupSize", ""));
                oldMethodMovement.setText(prefs.getString("oldUserProfileMovement", ""));


            } else {
                showOld.setVisibility(View.GONE);
                showNew.setVisibility(View.VISIBLE);
            }


            //Change user button pressed
            changeUserBtn.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     showOld.setVisibility(View.GONE);
                     showNew.setVisibility(View.VISIBLE);
                 }
             });

            //Keep user button pressed
            keepUserBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            newUserBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    final String sizeGroupUser, typeMovementUser, nameUser;


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

                                        addToSharedPrefsInt(activity, "usersID", resultID);
                                        addToSharedPrefsString(activity, "oldUserProfileName", nameUser);
                                        addToSharedPrefsString(activity, "oldUserProfileGroupSize", sizeGroupUser);
                                        addToSharedPrefsString(activity, "oldUserProfileMovement", typeMovementUser);

                                        getLocation();


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

                            Toast.makeText(activity, "Something has gone wrogn please hard quit app and load again", Toast.LENGTH_LONG);

                            getLocation();

                        }
                    }) {

                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded; charset=UTF-8";
                        }

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {

                            Log.d("getParams new user", "called");


                            //String nameUser = MapsActivity.this.name.getText().toString();
                            Log.d("getParams new user", "usersName: " + nameUser);


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

    //@params activity, uniqueID, value
    //Puts a given int into shared prefs
    public void addToSharedPrefsInt(Activity activity, String uniqueID, int value) {

        final SharedPreferences prefs = activity.getSharedPreferences(
                "com.example.williamdunnett.mapstest2", Context.MODE_PRIVATE);

        prefs.edit().putInt(uniqueID, value).apply();
    }

    //@params activity, uniqueID, value
    //Puts a given string into shared prefs
    public void addToSharedPrefsString(Activity activity, String uniqueID, String value) {

        final SharedPreferences prefs = activity.getSharedPreferences(
                "com.example.williamdunnett.mapstest2", Context.MODE_PRIVATE);

        prefs.edit().putString(uniqueID, value).apply();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Log.d("onStart", "called");
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.d("onResume", "called");
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.d("onPause", "called");
    }




    protected void getLocation() {
        boolean gps_enabled = false, network_enabled = false;

        if (isLocationEnabled(MapsActivity.this)) {
            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            Log.d("getLocation", "location is enabled");

            //You can still do this if you like, you might get lucky:

            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("getLocation", "Permission needed");
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

            } else {

                //exceptions will be thrown if provider is not permitted.
                try{gps_enabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
                try{network_enabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

                //don't start listeners if no provider is enabled
                if(!gps_enabled && !network_enabled) {
                    Log.d("getLocation", "gps_enabled false and network_enabled false");
                    return;
                } else {
                    Log.d("getLocation", "gps_enabled: " + gps_enabled + "   network_enabled: " + network_enabled);
                }


                if(gps_enabled)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
                if(network_enabled)
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

                timer1=new Timer();
                timer1.schedule(new GetLastLocation(), 20000);

            }

        }
        else
        {
            Log.d("getLocation", "location is NOT enabled");
            //prompt user to enable location....
            //.................
        }
    }

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            Location net_loc=null, gps_loc=null;
            Boolean gps_enabled=false, network_enabled=false;
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);

            //exceptions will be thrown if provider is not permitted.
            try{gps_enabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
            try{network_enabled=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("getLastLocation", "Permission needed");
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

            } else {
                if(gps_enabled)
                    gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(network_enabled)
                    net_loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            //if there are both values use the latest one
            if(gps_loc!=null && net_loc!=null){
                if(gps_loc.getTime()>net_loc.getTime())
                    Log.d("GetLastKnownLocation", "Both gps_loc: " + gps_loc.toString());
                    //locationResult.gotLocation(gps_loc);
                else
                    Log.d("GetLastKnownLocation", "Both net_loc: " + net_loc.toString());
                    //locationResult.gotLocation(net_loc);
                return;
            }

            if(gps_loc!=null){
                Log.d("GetLastKnownLocation", "gps_loc: " + gps_loc.toString());
                //MapsActivity.UpdateLocation.updateServer(gps_loc);
                return;
            }
            if(net_loc!=null){
                Log.d("GetLastKnownLocation", "net_loc: " + net_loc.toString());
                //locationResult.gotLocation(net_loc);
                return;
            }
            Log.d("GetLastKnownLocation", "Null available");
            //locationResult.gotLocation(null);
        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            //TODO change this line to save location in prefs
            //locationResult.gotLocation(location);
            Log.d("locationListenerGPS","called");
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            Log.d("locationListenerNETWORK","called");

            double latti = location.getLatitude();
            double longi = location.getLongitude();
            //double latti = 80;
            //double longi = 80;
            String lattitude = String.valueOf(latti);
            String longitude = String.valueOf(longi);

            Log.d("getLocation", "Lattitude = " + lattitude +
                    " Longitude = " + longitude);

            // Add a marker in currentLocation  and move the camera
            LatLng currentLocation = new LatLng(latti, longi);
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("currentLocation"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            //mMap.

            //TODO change this line to save location in prefs
            addToSharedPrefsString(MapsActivity.this, "lattitude", lattitude);
            addToSharedPrefsString(MapsActivity.this, "longitude", longitude);
            //locationResult.gotLocation(location);
            //locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGps);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

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
