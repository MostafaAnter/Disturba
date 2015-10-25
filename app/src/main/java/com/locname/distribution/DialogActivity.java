package com.locname.distribution;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mostafa on 9/19/2015.
 */
public class DialogActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    Button bt, bt1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity);
        preferences = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);
        bt = (Button) findViewById(R.id.check_in);
        bt1 = (Button) findViewById(R.id.check_out);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ShareValues.APP_PREFERENCES_DIALOG_OPEN, true);
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ShareValues.APP_PREFERENCES_DIALOG_OPEN, false);
        editor.commit();
        finish();
    }

    public void checkIn(View view) {
        //// TODO: 9/28/2015  check in
        checkin();

    }

    public void checkOut(View view) {
        // TODO: 9/28/2015 check out
        checkout();
        /*
        * delete lat and lng that saved
        * */
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(ShareValues.APP_PREFERENCES_TARGET_LATITUDE);
        editor.remove(ShareValues.APP_PREFERENCES_TARGET_LONGITUDE);
        // Save the changes in SharedPreferences
        editor.commit();

    }
    private void checkRequest(String uri) {
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
                            JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                            String mes = jsonObject.optString("check_out_message");
                            Toast.makeText(DialogActivity.this, mes, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        pDialog.dismiss();

                        //// TODO: 10/15/2015 clear last lat and last lng
                        Intent intent = new Intent(DialogActivity.this, TaskActivity.class);
                        startActivity(intent);
                        DialogActivity.this.finish();



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(DialogActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
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



                        try {
                            JSONObject jsonRootObject = new JSONObject(response);//done
                            JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                            String mes = jsonObject.optString("check_in_message");
                            Toast.makeText(DialogActivity.this, mes, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(DialogActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
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

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
    private void checkin(){
        if (isOnline()) {
            if (preferences.getString(ShareValues.APP_PREFERENCES_TASK_ID, null) != null) {
                checkInRequest("http://distribution.locname.com/laravel/api/tasks/checkin");

            }else {
                Toast.makeText(this, "please restart app", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
    private void checkout(){
        if (isOnline()) {
            if (preferences.getString(ShareValues.APP_PREFERENCES_TASK_ID, null) != null) {
                checkRequest("http://distribution.locname.com/laravel/api/tasks/checkout");

            }else {
                Toast.makeText(this, "please restart app", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
}
