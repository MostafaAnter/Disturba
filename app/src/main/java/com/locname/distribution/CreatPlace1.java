package com.locname.distribution;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mostafa on 9/12/2015.
 */

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CreatPlace1 extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {
    /*
    * variable to get location without gps
    * */
    private static double lat, lng;
    private static String mcc;  //Mobile Country Code
    private static String mnc;  //mobile network code
    private static String cellid; //Cell ID
    private static String lac;  //Location Area Code
    private String API_KEY = "AIzaSyA_4z2vgpIPuK0HvOxdCuEazf4jphtyLSo";

    private Toolbar toolbar;

    // information element
    Geocoder geocoder;
    List<Address> addresses;
    Circle shape;

   // get current location values
    // LogCat tag
    private static final String TAG = CreatPlace1.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    // end current location values



    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    GoogleMap mMap;

    Marker mMarker;


    @SuppressWarnings("unused")
    private static final double SEATTLE_LAT = 47.60621,
            SEATTLE_LNG =-122.33207,
            SYDNEY_LAT = -33.867487,
            SYDNEY_LNG = 151.20699,
            NEWYORK_LAT = 40.714353,
            NEWYORK_LNG = -74.005973;
    private static final float DEFAULTZOOM = 18;
    private static final String LOGTAG = "Maps";

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {
            setContentView(R.layout.creat_location1);
            tv = (TextView) findViewById(R.id.text1);

            //set toolbar
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            if (initMap()) {
                // Building the GoogleApi client
                buildGoogleApiClient();
                createLocationRequest();
                //test
                if (checkGPS()) {
                    displayLocation();
                }else {
                    try {
                        getCellLoc();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
            else {
                Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            setContentView(R.layout.activity_main);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.direction_menu, menu);
        return true;
    }

    public boolean servicesOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFrag =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFrag.getMap();
        }
        return (mMap != null);
    }

    protected void gotoCurrentLocation() throws IOException {

        if (mLastLocation == null) {
            Toast.makeText(this, "Current location isn't available", Toast.LENGTH_SHORT).show();
        }
        else {
            LatLng ll = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFAULTZOOM);
            mMap.animateCamera(update);

            // location information
             geocoder = new Geocoder(this, Locale.getDefault());
             addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            StringBuilder sb = new StringBuilder();
            if(address != null){
                sb.append(address);
            }
            if (city!=null){
                sb.append(city);
                sb.append(",");
            }
            if (state!=null){
                sb.append(state);
                sb.append(",");
            }
            if (country!=null){
                sb.append(country);
                sb.append(",");
            }
            if (postalCode!=null){
                sb.append(postalCode);
                sb.append(",");
            }
            if (knownName!=null){
                sb.append(knownName);
            }


            if (mMarker != null){
                mMap.clear();
            }
            MarkerOptions options = new MarkerOptions()
                    .title(address)
                    .position(ll);
            mMarker = mMap.addMarker(options);
            shape = drawCircle(ll);

            tv.setText(sb.toString());
        }
    }

    private void gotoLocation(double lat, double lng,
                              float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MapStateManager mgr = new MapStateManager(this);
        mgr.saveMapState(mMap);

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
//			This is part of the answer to the code challenge
            mMap.setMapType(mgr.getSavedMapType());
        }
        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    public void next(View view) {

        if (mLastLocation != null) {
            Intent intent = new Intent(this, CreatPlace2.class);
            //send mLastLocation
            intent.putExtra("lat", mLastLocation.getLatitude());
            intent.putExtra("lng", mLastLocation.getLongitude());
            startActivity(intent);
        }
        else
            return;
    }
    //current location funcion
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        finish();
    }
    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            try {
                gotoCurrentLocation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String text = latitude + ", " + longitude;
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(this, "(Couldn't get the location. Make sure location is enabled on the device)"
                    , Toast.LENGTH_SHORT);
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }
    private Circle drawCircle(LatLng ll) {


        CircleOptions options = new CircleOptions()
                .center(ll)
                .radius(40) //meters surround us
                .fillColor(0x330000FF)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);


        return mMap.addCircle(options);
    }
    /*
    * get Location without Gps
    * */
    private void getGsmCellLocation() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
        String networkOperator = telephonyManager.getNetworkOperator();

        mcc = networkOperator.substring(0, 3);
        mnc = networkOperator.substring(3);
        cellid= "" + cellLocation.getCid();
        lac = "" + cellLocation.getLac();

    }

    //- post parameter
    private void requestLocation(String uri) throws JSONException {



        /*
        * set json object i will send as param in my request
        * */
        JSONObject cellTower = new JSONObject();
        cellTower.put("cellId", cellid);
        cellTower.put("locationAreaCode", lac);
        cellTower.put("mobileCountryCode", mcc);
        cellTower.put("mobileNetworkCode", mnc);

        JSONArray cellTowers = new JSONArray();
        cellTowers.put(cellTower);

        JSONObject rootObject = new JSONObject();
        rootObject.put("cellTowers", cellTowers);


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri, rootObject,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {


                        Log.d("content", jsonObject.toString());


                        // parse json
                        try {
                            JSONObject object = jsonObject.getJSONObject("location");
                            lat = object.getDouble("lat");
                            lng = object.getDouble("lng");

                            Toast.makeText(CreatPlace1.this, "" + lat + "," + lng, Toast.LENGTH_SHORT).show();
                            // intialize lastLocation
                            mLastLocation = new Location("dummyprovider");
                            mLastLocation.setLatitude(lat);
                            mLastLocation.setLongitude(lng);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            gotoCurrentLocation();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(CreatPlace1.this, "error message", Toast.LENGTH_LONG).show();

                    }

                }
        );


        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void getCellLoc() throws JSONException {

        //get cell information first
        getGsmCellLocation();
        if (isOnline()) {

            requestLocation("https://www.googleapis.com/geolocation/v1/geolocate?key=" + API_KEY);


        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
    public boolean checkGPS(){
        LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);;
        boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (enabled){
            return true;
        }else
        {
            return false;
        }
    }

}
