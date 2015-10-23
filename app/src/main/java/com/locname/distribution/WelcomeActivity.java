package com.locname.distribution;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.locname.distribution.profile_utils.ImageIntentHandler;
import com.locname.distribution.profile_utils.ImageUtils;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mostafa on 9/12/2015.
 */
public class WelcomeActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener {

    private static int status;

    CircleImageView mImageView;
    Button mButtonPick;
    ImageIntentHandler.ImagePair mImagePair;


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

    TextView tv1, tv;
    SwitchCompat switchCompat;

    /*
    * flage to mange switch programary or humanly
    * */

    private static Boolean mFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        tv1 = (TextView) findViewById(R.id.user_name);
        tv = (TextView) findViewById(R.id.user_last_name);
        //set switch button
        switchCompat = (SwitchCompat) findViewById(R.id
                .switch_compat);
        switchCompat.setSwitchPadding(40);
        switchCompat.setOnCheckedChangeListener(this);

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get sharedPreference
        mCoordinates = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);
        tv1.setText(mCoordinates.getString(ShareValues.APP_PREFERENCES_USER_NAME, " "));
        tv.setText(mCoordinates.getString(ShareValues.APP_PREFERENCES_USER_LAST_NAME, " "));

        switchCompat.setChecked(mCoordinates.getBoolean(ShareValues.APP_PREFERENCES_START_WORK_STATE, false));




        //get image path
        String path = mCoordinates.getString(ShareValues.APP_PREFERENCES_PROFILE_PATH, "");
        if (path != ""){


            loadImageFromStorage(path);
            Log.d("path",mCoordinates.getString(ShareValues.APP_PREFERENCES_PROFILE_PATH, ""));
        }

        //set image and controller for it
        mImageView = (CircleImageView) findViewById(R.id.profile_image);
        mButtonPick = (Button) findViewById(R.id.button_pick);
        //set when click
        mButtonPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImagePair = new ImageIntentHandler.ImagePair(mImageView, null);
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, ImageIntentHandler.REQUEST_GALLERY);

            }
        });


        //activate service
        startAlarmManager();
        Intent intent= new Intent(this, LocalWordService.class);
        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);


    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        ImageIntentHandler intentHandler =
                new ImageIntentHandler(WelcomeActivity.this, mImagePair)
                        .folder("Disturba Profile")
                        .sizeDp(300,400);
        intentHandler.handleIntent(requestCode, resultCode, data);


        // get bitmap and save
        mImageView.buildDrawingCache();
        Bitmap bitmap = mImageView.getDrawingCache();
        String picturePath = saveToInternalSorage(bitmap);


        SharedPreferences.Editor editor = mCoordinates.edit();
        editor.putString(ShareValues.APP_PREFERENCES_PROFILE_PATH, picturePath );
        editor.commit();
    }

    private String saveToInternalSorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }
    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            mImageView = (CircleImageView) findViewById(R.id.profile_image);
            mImageView.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

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


            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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

                        try {
                            JSONObject jsonRootObject = jsonRootObject = new JSONObject(response);
                            status= Integer.parseInt(jsonRootObject.optString("status").toString());
                            if(status == 601){
                                JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                                String mes = jsonObject.optString("work_status");
                                Toast.makeText(WelcomeActivity.this, mes, Toast.LENGTH_SHORT).show();
                            }else if (status == 530){
                                JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                                String mes = jsonObject.optString("work_status");
                                Toast.makeText(WelcomeActivity.this, mes, Toast.LENGTH_SHORT).show();
                            }else if (status == 200){
                                JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                                String mes = jsonObject.optString("work_status");
                                Toast.makeText(WelcomeActivity.this, mes, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(WelcomeActivity.this, "some thing wrong!!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pDialog.dismiss();

//                        if (status == 200) {
//                            Intent intent = new Intent(WelcomeActivity.this, TaskActivity.class);
//                            startActivity(intent);
//                        }


                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(WelcomeActivity.this, "Retry again! network not available", Toast.LENGTH_LONG).show();
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

                        try {
                            JSONObject jsonRootObject = jsonRootObject = new JSONObject(response);
                            status= Integer.parseInt(jsonRootObject.optString("status").toString());
                            if(status == 601){
                                JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                                String mes = jsonObject.optString("work_status");
                                Toast.makeText(WelcomeActivity.this, mes, Toast.LENGTH_SHORT).show();
                            }else if (status == 530){
                                JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                                String mes = jsonObject.optString("work_status");
                                Toast.makeText(WelcomeActivity.this, mes, Toast.LENGTH_SHORT).show();
                            }else if (status == 200){
                                JSONObject jsonObject = jsonRootObject.getJSONObject("response");
                                String mes = jsonObject.optString("work_status");
                                Toast.makeText(WelcomeActivity.this, mes, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(WelcomeActivity.this, "some thing wrong!!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(WelcomeActivity.this, "Retry again! network not available", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "please login again!!", Toast.LENGTH_SHORT).show();
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


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.switch_compat:
                if (mFlag) {
                    if (isChecked){
                        //start work
                        startWork();//
                        SharedPreferences.Editor editor = mCoordinates.edit();
                        editor.putBoolean(ShareValues.APP_PREFERENCES_START_WORK_STATE, isChecked);
                        editor.commit();
                        Log.i("switch_compat", isChecked + "");
                    }else {
                        //end work
                        finishWork();
                        SharedPreferences.Editor editor = mCoordinates.edit();
                        editor.putBoolean(ShareValues.APP_PREFERENCES_START_WORK_STATE, isChecked);
                        editor.commit();
                        Log.i("switch_compat", isChecked + "");
                    }
                    break;
                }else {
                    mFlag = true;
                    break;
                }

        }
    }

    public void getTasks(View view) {
        Intent intent = new Intent(WelcomeActivity.this, TaskActivity.class);
                            startActivity(intent);
    }
}
