package com.locname.distribution;

/**
 * Created by Mostafa on 9/12/2015.
 */
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.github.polok.routedrawer.RouteDrawer;
import com.github.polok.routedrawer.RouteRest;
import com.github.polok.routedrawer.model.Routes;
import com.github.polok.routedrawer.model.TravelMode;
import com.github.polok.routedrawer.parser.RouteJsonParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.locname.distribution.model.Flower;
import com.locname.distribution.parsers.FlowerJSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;


public class DirectionActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, RoutingListener {

    private SharedPreferences preferences;

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


    // list of distance and duration
    List<Flower> flowerList;
    private Polyline polyline;

    private static LatLng end;
    private static LatLng start;


    //marker to current location
    Marker marker;
    Marker startMarker, endMarker;


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

    private static final float DEFAULTZOOM = 18;
    // direction activity ui
    TextView tvTime, tvDistance, tvAddress;

    //distance duration url
    private static String matrixAPI;

    // repeat task
    private int mInterval = 30000; // 30 seconds by default, can be changed later
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {
            setContentView(R.layout.direction_activity);
            //set toolbar
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            preferences = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);

            tvTime = (TextView) findViewById(R.id.timeText);
            tvDistance = (TextView) findViewById(R.id.distanceText);
            tvAddress = (TextView) findViewById(R.id.addressText);

            if (initMap()) {
                // Building the GoogleApi client
                buildGoogleApiClient();
                createLocationRequest();
                //test
                try {
                    displayLocation();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //repeat task
                mHandler = new Handler();
                startRepeatingTask();


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

    private void gotoLocation(double lat, double lng) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mMap.moveCamera(update);
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

        // must stop
        stopRepeatingTask();
        stopLocationUpdates();
    }
    /**
     * Method to display the location on UI
     * */
    //edit to manpulate with routing
    private void displayLocation() throws IOException {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            // start drow route
            start = new LatLng(latitude, longitude);
            end = new LatLng(getIntent().getDoubleExtra("latitude", 1), getIntent().getDoubleExtra("longitude", 1) );
            route();

            String text = latitude + ", " + longitude;
//            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            // location information
            locationInformation();
            //request distance
            requestData(createUri());

        } else {

            Toast.makeText(this, "(Couldn't get the location. Make sure location is enabled on the device)"
                    , Toast.LENGTH_SHORT);
        }
    }

    private void locationInformation() throws IOException {
        geocoder = new Geocoder(this, Locale.getDefault());
        addresses = geocoder.getFromLocation(getIntent().getDoubleExtra("latitude", 1), getIntent().getDoubleExtra("longitude", 1), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

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
        tvAddress.setText(sb.toString());
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
        try {
            displayLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }

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


//        Toast.makeText(getApplicationContext(), "Location changed!",
//                Toast.LENGTH_SHORT).show();




        requestData(createUri());

    }
    //routing functions
    public void route()
    {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(start, end)
                .build();
        routing.execute();

    }


    @Override
    public void onRoutingFailure() {
        // The Routing request failed

        Toast.makeText(this,"plz, check you inter net connection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {
        // The Routing Request starts
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route)
    {
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        // move camera
        mMap.moveCamera(center);
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULTZOOM));


        if(polyline!=null)
            polyline.remove();

        polyline=null;
        //adds route to the map.
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(R.color.colorPrimaryDark));
        polyOptions.width(8);
        polyOptions.addAll(mPolyOptions.getPoints());
        polyline=mMap.addPolyline(polyOptions);

        // Start marker
        if (startMarker != null){
            startMarker.remove();
        }
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        startMarker = mMap.addMarker(options);

        // End marker
        if (endMarker != null){
            endMarker.remove();
        }
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        endMarker = mMap.addMarker(options);
    }

    @Override
    public void onRoutingCancelled() {
        Log.i(TAG, "Routing was cancelled.");
    }


    public void start_navigation(View view) {
        //from specific location to specific location

//        String navigationPath = "http://maps.google.com/maps?saddr=" + currentLat + "," + currentLng + "&daddr=" +
//                desLat + "," + desLng;
//
        String navigationPath = "http://maps.google.com/maps?daddr=" + end.latitude + "," + end.longitude;
        goNavigation(navigationPath);

    }
    private void goNavigation(String navPath){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                //from current location to specific location
                // Uri.parse("http://maps.google.com/maps?daddr=20.5666,45.345"));
                // from specific location to specific location
                Uri.parse(navPath));
        startActivity(intent);

    }

    public void checkOut(View view) {
        //// TODO: 10/22/2015 checkout
        check_out();
    }

    public void checkIn(View view) {
        //// TODO: 10/22/2015 check in
        check_in();

    }

    //distance duration task
    private class MyTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
//			updateDisplay("Starting task");

        }

        @Override
        protected String doInBackground(String... params) {

            String content = HttpManager.getData(params[0]);
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            // surround with try catch because if user internet closed dont crached
            try {
                flowerList = FlowerJSONParser.parseFeed(result);
                updateDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        @Override
        protected void onProgressUpdate(String... values) {
//			updateDisplay(values[0]);
        }

    }
    private void requestData(String uri) {
        MyTask task = new MyTask();
        task.execute(uri);
    }
    protected void updateDisplay() {

        if (flowerList != null) {
            for (Flower flower : flowerList) {
                tvDistance.setText(flower.getDistance());
                tvTime.setText(flower.getDuration());
            }
        }

    }
    protected String createUri(){

        // currentLocation coordinates
        double lastLatitude = mLastLocation.getLatitude();
        double lastLongitude = mLastLocation.getLongitude();
        double targetLatitude = getIntent().getDoubleExtra("latitude", 1);
        double targetLongitude = getIntent().getDoubleExtra("longitude", 1);
        matrixAPI = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                lastLatitude + "," + lastLongitude + "&destinations=" + targetLatitude + "," +
                targetLongitude + "&mode=driving&language=en-EN&key=AIzaSyANwq1jlh0_Q9eINnenCTzGdVh1EBM3pcs";
        String uri = matrixAPI;
        return uri;

    }
    /*
    * set function that repeat
    * */
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            //updateStatus(); //this function can change value of mInterval.
            repeatFun();
            mHandler.postDelayed(mStatusChecker, mInterval);

        }
    };
    private void repeatFun(){

        if (checkGPS()) {
            if (mLastLocation != null) {
                mMap.clear();
                //route();
                try {
                    displayLocation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Starting the location updates
                startLocationUpdates();

            }
        }else if( mLastLocation == null){
            try {
                getCellLoc();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;

        }else if (!checkGPS()){
            if (mLastLocation != null){
                return;

            }
        }


    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
    /*
    * get distance between two locations
    * */
    public static double calculateDistance(double startLat, double startLng,double endLat, double endLng) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = startLat;
        double lat2 = endLat;
        double lon1 = startLng;
        double lon2 = endLng;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));


        return Radius * c * 1000;//result in meters
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
                            start = new LatLng(lat, lng);
                            Toast.makeText(DirectionActivity.this, "" + lat + "," + lng, Toast.LENGTH_SHORT).show();
                            // intialize lastLocation
                            mLastLocation = new Location("dummyprovider");
                            mLastLocation.setLatitude(lat);
                            mLastLocation.setLongitude(lng);
                            if (start != null) {
                                drawPathWithoutGps();
                            }
                            //request distance
                            requestData(createUri());
                            //set location information text
                            try {
                                locationInformation();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(DirectionActivity.this, "error message", Toast.LENGTH_LONG).show();

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

    private void drawPathWithoutGps(){


        end = new LatLng(getIntent().getDoubleExtra("latitude", 1), getIntent().getDoubleExtra("longitude", 1) );

        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .waypoints(start, end)
                .build();
        routing.execute();


//        final RouteDrawer routeDrawer = new RouteDrawer.RouteDrawerBuilder(mMap)
//                .withColor(Color.BLUE)
//                .withWidth(8)
//                .withAlpha(0.5f)
//                .withMarkerIcon(null)
//                .build();
//
//        RouteRest routeRest = new RouteRest();
//
//        routeRest.getJsonDirections(start, new LatLng(getIntent().getDoubleExtra("latitude", 1), getIntent().getDoubleExtra("longitude", 1)), TravelMode.DRIVING)
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(new Func1<String, Routes>() {
//                    @Override
//                    public Routes call(String s) {
//                        return new RouteJsonParser<Routes>().parse(s, Routes.class);
//                    }
//                })
//                .subscribe(new Action1<Routes>() {
//                    @Override
//                    public void call(Routes r) {
//                        routeDrawer.drawPath(r);
//                        //my touch
//                        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
//                        // move camera
//                        mMap.moveCamera(center);
//                        // Zoom in the Google Map
//                        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULTZOOM));
//                    }
//                });



    }

    /*
    * set check in out request
    * */
    private void checkRequest(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Toast.makeText(DirectionActivity.this, response, Toast.LENGTH_LONG).show();

                        try {
                            JSONObject jsonRootObject = new JSONObject(response);//done
                            JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                            String mes = jsonObject.optString("check_out_date");
                            Toast.makeText(DirectionActivity.this, mes, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        pDialog.dismiss();

                        //// TODO: 10/15/2015 clear last lat and last lng
                        Intent intent = new Intent(DirectionActivity.this, TaskActivity.class);
                        startActivity(intent);
                        DirectionActivity.this.finish();



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(DirectionActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", preferences.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));
                map.put("task_code", preferences.getString(ShareValues.APP_PREFERENCES_TASK_ID, null));


                return map;
            }


        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }

    private void checkInRequest(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Toast.makeText(DirectionActivity.this, response, Toast.LENGTH_LONG).show();

                        try {
                            JSONObject jsonRootObject = new JSONObject(response);//done
                            JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                            String mes = jsonObject.optString("check_in_date");
                            Toast.makeText(DirectionActivity.this, mes, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        pDialog.dismiss();

                        //// TODO: 10/15/2015 clear last lat and last lng



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(DirectionActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", preferences.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));
                map.put("task_code", preferences.getString(ShareValues.APP_PREFERENCES_TASK_ID, null));


                return map;
            }


        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }


    private void check_in(){
        if (isOnline()) {
            checkInRequest("http://distribution.locname.com/laravel/api/trip/tasks/checkin");
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
    private void check_out(){
        if (isOnline()) {
            checkRequest("http://distribution.locname.com/laravel/api/trip/tasks/checkout");
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }








}