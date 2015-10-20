package com.locname.distribution;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mostafa on 9/12/2015.
 */
public class CreatPlace2 extends AppCompatActivity {
    private SharedPreferences preferences;
    /*
    * parameter i'll post
    * */
    private static String place_name;
    private static String contact_name;
    private static String phone_number;
    private static String mobile_number;
    private static String task_email;
    private static String task_website;
    private static String long_coordinate;
    private static String lat_coordinate;
    private static String place_details;

    private EditText placeName, contactName,
    phoneNumber, mobileNumber, taskEmail, taskWebsite,
    placeDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creat_location2);

        preferences = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);
        /*
        * set Edit text
        * */
        placeName = (EditText) findViewById(R.id.place_name);
        contactName = (EditText) findViewById(R.id.contact_name);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        mobileNumber = (EditText) findViewById(R.id.mobile_number);
        taskEmail = (EditText) findViewById(R.id.task_email);
        taskWebsite = (EditText) findViewById(R.id.task_website);
        placeDetails = (EditText) findViewById(R.id.place_details);
        /*
        * set function when text change
        * */
        placeName.addTextChangedListener(new MyTextWatcher(placeName));
        contactName.addTextChangedListener(new MyTextWatcher(contactName));
        phoneNumber.addTextChangedListener(new MyTextWatcher(phoneNumber));
        mobileNumber.addTextChangedListener(new MyTextWatcher(mobileNumber));
        taskEmail.addTextChangedListener(new MyTextWatcher(taskEmail));
        taskWebsite.addTextChangedListener(new MyTextWatcher(taskWebsite));
        placeDetails.addTextChangedListener(new MyTextWatcher(placeDetails));
        /*
        * get lat and lng
        * */

        long_coordinate = String.valueOf(getIntent().getDoubleExtra("lng", 1));
        lat_coordinate = String.valueOf(getIntent().getDoubleExtra("lat", 1));




    }

    public void save_data(View view) {
//        save_location();
    }
    /*
    * text watcher
    * */
    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.place_name:
                    //do some thing
                    place_name = placeName.getText().toString();
                    break;
                case R.id.contact_name:
                    //do some thing
                    contact_name = contactName.getText().toString();
                    break;
                case R.id.phone_number:
                    //do some thing
                    phone_number = phoneNumber.getText().toString();
                    break;
                case R.id.mobile_number:
                    //do some thing
                    mobile_number = mobileNumber.getText().toString();
                    break;
                case R.id.task_email:
                    //do some thing
                    task_email = taskEmail.getText().toString();
                    break;
                case R.id.task_website:
                    //do some thing
                    task_website = taskWebsite.getText().toString();
                    break;
                case R.id.place_details:
                    //do some thing
                    place_details = placeDetails.getText().toString();
                    break;

            }
        }
    }
    /*
    * call api
    * */
    private void requestData(String uri) {
        // show when user click on login
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Toast.makeText(CreatPlace2.this, response, Toast.LENGTH_LONG).show();
                        pDialog.dismiss();



                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(CreatPlace2.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        pDialog.dismiss();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", preferences.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));
                map.put("place_name", place_name);
                map.put("contact_name", contact_name);
                map.put("phone_number", phone_number);
                map.put("mobile_number", mobile_number);
                map.put("task_email", task_email);
                map.put("task_website", task_website);
                map.put("long_coordinate", long_coordinate);
                map.put("lat_coordinate", lat_coordinate);
                map.put("place_details", place_details);


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
    private void save_location(){
        if (isOnline()) {
            if (preferences.getString(ShareValues.APP_PREFERENCES_TASK_ID, null) != null) {
                requestData("http://distribution.locname.com/laravel/api/trip/tasks/checkin");

            }else {
                Toast.makeText(this, "please restart app", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }

}
