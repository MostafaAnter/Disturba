package com.locname.distribution;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.locname.distribution.model.TaskItem;
import com.locname.distribution.parsers.TaskParser;

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

    private CustomTask adapter, adapter1;
    SharedPreferences mCoordinates;
    private static ArrayList<TaskItem> task_items, all_item, not_active_task_item;
    private ListView activeList, nonactiveList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mCoordinates = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);

        //---------------------------//
        task_items = new ArrayList<>();
        all_item = new ArrayList<>();
        not_active_task_item = new ArrayList<>();
        activeList = (ListView) findViewById(R.id.ListView1);
        nonactiveList = (ListView) findViewById(R.id.ListView2);
        activeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                Object o = activeList.getItemAtPosition(position);
                TaskItem task = (TaskItem) o;
                Toast.makeText(TaskActivity.this, " " + task.getTask_name(), Toast.LENGTH_LONG).show();

                // save task id to sharedPreference
                SharedPreferences.Editor editor = mCoordinates.edit();
                editor.putString(ShareValues.APP_PREFERENCES_TASK_ID, task.getTask_name());
                editor.commit();

                startTask(task.getLat(), task.getLng());


            }


        });
        nonactiveList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(TaskActivity.this, " " + "Not Active !", Toast.LENGTH_LONG).show();
            }
        });
        loadTrips();

    }



    @Override
    protected void onPause() {
        super.onPause();
        finish();
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
        intent.putExtra("latitude", lat);  //lat 27.180768, 31.194286
        intent.putExtra("longitude", lng); //lng 30.045034, 31.258876
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
                        Toast.makeText(TaskActivity.this, "Retry again,Network not available!", Toast.LENGTH_LONG).show();
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
    private void loadTrips(){
        if (isOnline()) {
            requestData("http://distribution.locname.com/laravel/api/tasks");
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
    protected void updateDisplay() {
        for (TaskItem item:all_item) {

            if (item.getTask_details().equalsIgnoreCase("canceled") || item.getTask_details().equalsIgnoreCase("finished")) {
                not_active_task_item.add(item);
            }else {
                task_items.add(item);
            }
        }
         //------------- list--------------//
        adapter = new CustomTask(this, task_items);
        activeList.setAdapter(adapter);


        adapter1 = new CustomTask(this, not_active_task_item);
        nonactiveList.setAdapter(adapter1);


    }


    public void reload(View view) {
        task_items.clear();
        not_active_task_item.clear();
        loadTrips();

    }
}
