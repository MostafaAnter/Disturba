package com.locname.distribution;

/**
 * Created by Mostafa on 9/6/2015.
 */
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class LocalWordService extends Service {

    private static Location mLastLocation;
    private SharedPreferences preferences;

    // to get current location
    private LocationTracker locationTracker ;


    private ArrayList<String> list = new ArrayList<String>();

    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        LocalWordService getService() {
            return LocalWordService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // El gdeed
        preferences = getSharedPreferences(ShareValues.APP_PREFERENCES, MODE_PRIVATE);

        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        //Toast.makeText(this, "startttttt", Toast.LENGTH_SHORT).show();

        locationTracker = new LocationTracker(this) {
            @Override
            public void onLocationFound(@NonNull Location location) {
                if (location != null) {
                    mLastLocation = location;
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // El gdeed

                    if (preferences.getString(ShareValues.APP_PREFERENCES_TARGET_LATITUDE, null) != null &&
                            preferences.getString(ShareValues.APP_PREFERENCES_TARGET_LONGITUDE, null) != null) {

                        if (calculateDistance(latitude, longitude, Double.parseDouble(preferences.getString(ShareValues.APP_PREFERENCES_TARGET_LATITUDE, null)),
                                Double.parseDouble(preferences.getString(ShareValues.APP_PREFERENCES_TARGET_LONGITUDE, null))) <= 20.0) {

                            if (preferences.getBoolean(ShareValues.APP_PREFERENCES_DIALOG_OPEN, false) == false) {
                                launchActivity();
                            }
//                            String boo =" Dialog is" + preferences.getBoolean(ShareValues.APP_PREFERENCES_DIALOG_OPEN, false);
//                            Toast.makeText(LocalWordService.this,boo , Toast.LENGTH_SHORT).show();


                        }
                    }







                    String text = getCurrentTime() + "       " + latitude + longitude;
                    list.add(text);
                    //Toast.makeText(LocalWordService.this, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTimeout() {

            }
        };


        // post params
        if (mLastLocation != null) {
            log();
        }


        if (list.size() >= 20) {
            list.remove(0);
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LocalWordService.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_media_route_on_2_mono_dark)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    public List<String> getWordList() {
        return list;
    }

    //Time format HH:MM:SS
    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();

        return(new StringBuilder()
                .append(c.get(Calendar.HOUR_OF_DAY)).append(":")
                .append(c.get(Calendar.MINUTE)).append(":")
                .append(c.get(Calendar.SECOND)).append(" ")).toString();
    }
    //to get distance between two points
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
    private void launchActivity() {
        Intent intent = new Intent(this, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }
    //- post parameter
    private void requestData(String uri) {

        StringRequest request = new StringRequest(Request.Method.POST, uri,

                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                       // Toast.makeText(LocalWordService.this, response, Toast.LENGTH_LONG).show();

                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError ex) {
                        Toast.makeText(LocalWordService.this, ex.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("token", preferences.getString(ShareValues.APP_PREFERENCES_ACCESS_TOKEN, null));
                map.put("trip_id", preferences.getString(ShareValues.APP_PREFERENCES_TRIP_ID, null));
                map.put("current_long",String.valueOf(mLastLocation.getLongitude()));
                map.put("current_lat", String.valueOf(mLastLocation.getLatitude()));
                map.put("target_long", preferences.getString(ShareValues.APP_PREFERENCES_TARGET_LONGITUDE, null));
                map.put("target_lat", preferences.getString(ShareValues.APP_PREFERENCES_TARGET_LATITUDE, null));

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
    private void log(){
        if (isOnline()) {
            if (preferences.getString(ShareValues.APP_PREFERENCES_TASK_ID, null) != null) {
                requestData("http://distribution.locname.com/laravel/api/trip/log");

            }else {
                Toast.makeText(this, "please restart app", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
        }
    }
}