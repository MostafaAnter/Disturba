package com.locname.distribution;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.locname.distribution.model.TripItem;
import com.locname.distribution.parsers.TripParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mostafa on 9/26/2015.
 */
public class TripActivity extends AppCompatActivity {
    private Toolbar toolbar;

    public static String TRIP_ID = "id";
    private CustomTrip adapter, adapter1;
    Intent intent;
    ListView nonactiveList, activeList;

    ArrayList<TripItem> all_item, active_items, nonactive_items  ;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);
        all_item = new ArrayList<>();
        active_items = new ArrayList<>();
        nonactive_items = new ArrayList<>();


        activeList = (ListView) findViewById(R.id.ListView1);
        nonactiveList = (ListView) findViewById(R.id.ListView2);

        activeList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                Object o = activeList.getItemAtPosition(position);
                TripItem trip = (TripItem) o;
                Toast.makeText(TripActivity.this, " " + trip.getTrip_name(), Toast.LENGTH_LONG).show();
                //save trip id to share it in service
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(ShareValues.APP_PREFERENCES_TRIP_ID, trip.getTrip_id());
                editor.commit();
                //start activity
                intent = new Intent(TripActivity.this, TaskActivity.class);
                intent.putExtra(TRIP_ID, trip.getTrip_id());
                startActivity(intent);

            }

        });
        nonactiveList.setOnItemClickListener(new OnItemClickListener() {


            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                Object o = activeList.getItemAtPosition(position);
                TripItem trip = (TripItem) o;
                Toast.makeText(TripActivity.this, " " + /*trip.getTrip_name()*/"Not Active !", Toast.LENGTH_LONG).show();


            }


        });

        loadTrips();


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

    protected void updateDisplay() {



        for (TripItem item : all_item){
            if (item.getVisibility() == 0){
                nonactive_items.add(item);
            }if (item.getVisibility() == 1){
                active_items.add(item);
            }
        }

        //Use FlowerAdapter to display data
        //-------------active list--------------//
        adapter = new CustomTrip(this, active_items);
        activeList.setAdapter(adapter);


        //----------nonactive list-----------------//

        adapter1 = new CustomTrip(this, nonactive_items);
        nonactiveList.setAdapter(adapter1);


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

                        if (TripParser.parseFeed(response) != null) {
                            all_item = TripParser.parseFeed(response);
                            updateDisplay();
                        } else {
                            Toast.makeText(TripActivity.this, "there is no tripp", Toast.LENGTH_SHORT).show();
                        }

                        pDialog.dismiss();


                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(TripActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", preferences.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));


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
            if (preferences.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null) != null) {
                requestData("http://distribution.locname.com/laravel/api/trip");

            }else {
                Toast.makeText(this, "please Logout and Login again!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
}
