package com.locname.distribution;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.locname.distribution.parsers.TaskParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mostafa on 9/12/2015.
 */
public class WelcomeActivity extends AppCompatActivity {
    // define toolbar
    private Toolbar toolbar;

    //sharedPreference
    private SharedPreferences mCoordinates;

    private static final int GPS_ERRORDIALOG_REQUEST = 9001;

    //active service
    private static final String TAG = "GpsTrackerActivity";
    private LocalWordService s;
    private boolean currentlyTracking;
    private int intervalInMinutes = 1;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;
    IBinder binder = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get sharedPreference
        mCoordinates = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);

        //activate service
        startAlarmManager();
        Intent intent= new Intent(this, LocalWordService.class);
        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);


    }
    private void startAlarmManager() {
        Log.d(TAG, "startAlarmManager");

        Context context = getBaseContext();
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                intervalInMinutes * 30000, // 30000 = 30 sec
                pendingIntent);
    }
    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        Intent intent= new Intent(this, LocalWordService.class);
        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);


    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            LocalWordService.LocalBinder b = (LocalWordService.LocalBinder) binder;
            s = b.getService();
            Toast.makeText(WelcomeActivity.this, "Connected", Toast.LENGTH_SHORT)
                    .show();
        }

        public void onServiceDisconnected(ComponentName className) {
            s = null;
        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.saveLocation:
                if (servicesOK()) {
                    Intent intent = new Intent(this, CreatPlace1.class);
                    startActivity(intent);
                }
                break;

            case R.id.logout:
                // show dialog
                final ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage("logout...");
                pDialog.show();
                SharedPreferences.Editor editor = mCoordinates.edit();
                editor.clear();   // clear all data saved
                editor.commit(); // commit changes
                pDialog.dismiss();
                finish();

                Intent intent = new Intent(WelcomeActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.stopWorking:
                finishWork();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToTasks(View view) {

        // check if there is google service or not
        if (servicesOK()) {
            startWork();
//            Intent intent = new Intent(WelcomeActivity.this, TripActivity.class);
//            startActivity(intent);

        }

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
    /*
    * call api
    * */
    private void requestData(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Starting work...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
//
                            Toast.makeText(WelcomeActivity.this, response, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(WelcomeActivity.this, TripActivity.class);
                        startActivity(intent);


                        pDialog.dismiss();



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(WelcomeActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));



                return map;
            }


        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }
    private void requestFinishData(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Finishing work...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
//
                        Toast.makeText(WelcomeActivity.this, response, Toast.LENGTH_SHORT).show();

                        pDialog.dismiss();



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(WelcomeActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));



                return map;
            }


        };

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
    /*
    * call start work api
    * */
    private void startWork(){
        if (isOnline()) {
            if (mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null) != null) {
                requestData("http://distribution.locname.com/laravel/api/work/start");

            }else {
                Toast.makeText(this, "please Logout and Login again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
    /*
    * call finish work api
    * */
    private void finishWork(){
        if (isOnline()) {
            if (mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null) != null) {
                requestFinishData("http://distribution.locname.com/laravel/api/work/finish");

            }else {
                Toast.makeText(this, "please Logout and Login again ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }



}
