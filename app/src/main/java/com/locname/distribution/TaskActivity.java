package com.locname.distribution;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.locname.distribution.model.TaskItem;
import com.locname.distribution.model.TripItem;
import com.locname.distribution.parsers.TaskParser;
import com.locname.distribution.parsers.TripParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mostafa on 9/12/2015.
 */
public class TaskActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private CustomTask adapter;
    SharedPreferences mCoordinates;
    private static ArrayList<TaskItem> task_items, all_item;
    private ListView activeList;
    private static String TRIP_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TRIP_ID = getIntent().getStringExtra(TripActivity.TRIP_ID);
        mCoordinates = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);

        //---------------------------//
        task_items = new ArrayList<>();
        all_item = new ArrayList<>();
        activeList = (ListView) findViewById(R.id.ListView1);
        activeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                Object o = activeList.getItemAtPosition(position);
                TaskItem task = (TaskItem) o;
                Toast.makeText(TaskActivity.this, " " + task.getTask_name(), Toast.LENGTH_LONG).show();

                // save task id to sharedPreference
                SharedPreferences.Editor editor = mCoordinates.edit();
                editor.putString(ShareValues.APP_PREFERENCES_TASK_ID, task.getTask_id());
                editor.commit();

                startTask(task.getLat(), task.getLng());


            }


        });
        loadTrips();

    }



    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reload_menue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



   // // TODO: 9/26/2015  add two parameter lat & lng in signeture
    public void startTask(double lat, double lng) {
        //put sharedPreference
        SharedPreferences.Editor editor = mCoordinates.edit();
        editor.putString(ShareValues.APP_PREFERENCES_TARGET_LATITUDE, String.valueOf(lat));
        editor.putString(ShareValues.APP_PREFERENCES_TARGET_LONGITUDE, String.valueOf(lng));
        editor.commit();

        // lat and lng for test    30.049119, 31.196332

        Intent intent = new Intent(this, DirectionActivity.class);
        intent.putExtra("latitude", /*lat*/30.045249);  //lat 27.180768, 31.194286
        intent.putExtra("longitude", /*lng*/31.258876); //lng 30.045034, 31.258876
        startActivity(intent);

    }


    private void requestData(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
//                        flowerList = FlowerJSONParser.parseFeed(response);
//                        updateDisplay();

                        if (TaskParser.parseFeed(response) != null) {
                            all_item = TaskParser.parseFeed(response);
                            updateDisplay();
                        }else {
                            Toast.makeText(TaskActivity.this, "there is no task", Toast.LENGTH_SHORT).show();
                        }

                        pDialog.dismiss();



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(TaskActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));
                map.put("trip_id", TRIP_ID);


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
    private void loadTrips(){
        if (isOnline()) {
            if (mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null) != null) {
                requestData("http://distribution.locname.com/laravel/api/trip/tasks");

            }else {
                Toast.makeText(this, "please enter email and password", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
    protected void updateDisplay() {
        for (TaskItem item:all_item
             ) {
            if (item.getStatus() == 0){
                task_items.add(item);
            }

        }

         //------------- list--------------//
        adapter = new CustomTask(this, task_items);
        activeList.setAdapter(adapter);




    }


    public void end_trip(View view) {
        loadTripEnd();
    }

    public void start_trip(View view) {
        loadTripStar();
    }
    private void requestStartTrip(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRootObject = new JSONObject(response);//done
                            int status = Integer.parseInt(jsonRootObject.optString("status").toString());
                            if (status != 200){
                                Toast.makeText(TaskActivity.this, "please Retry!", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(TaskActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }




                        pDialog.dismiss();



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(TaskActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));
                map.put("trip_id", TRIP_ID);
                map.put("status", "1");


                return map;
            }


        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }
    private void requestEndTrip(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(TaskActivity.this, response, Toast.LENGTH_SHORT).show();
//                        try {
//                            JSONObject jsonRootObject = new JSONObject(response);//done
//                            int status = Integer.parseInt(jsonRootObject.optString("status").toString());
//                            if (status != 200){
//                                Toast.makeText(TaskActivity.this, "please Retry!", Toast.LENGTH_SHORT).show();
//                            }else {
//                                Toast.makeText(TaskActivity.this, "Done!", Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }


                        pDialog.dismiss();



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(TaskActivity.this, "error", Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));
                map.put("trip_id", TRIP_ID);
                map.put("status", "2");


                return map;
            }


        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }
    private void loadTripEnd(){
        if (isOnline()) {
            if (mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null) != null) {
                requestEndTrip("http://192.168.43.159/laravel_4.2/api/trip/finish");

            }else {
                Toast.makeText(this, "please restart app!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
    private void loadTripStar(){
        if (isOnline()) {
            if (mCoordinates.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null) != null) {
                requestStartTrip("http://192.168.43.159/laravel_4.2/api/trip/start");

            }else {
                Toast.makeText(this, "please restart app!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
}
