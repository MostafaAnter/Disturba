package com.locname.distribution.parsers;

import com.locname.distribution.model.Flower;
import com.locname.distribution.model.TripItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mostafa on 9/27/2015.
 */
public class TripParser {
    public static ArrayList<TripItem> parseFeed(String content) {

        try {
            JSONObject jsonRootObject = new JSONObject(content);//done
            //Get the instance of JSONArray that contains JSONObjects
            int status = Integer.parseInt(jsonRootObject.optString("status").toString());

            if(status != 200){

                return null;

            }else {

            JSONObject jsonObject = jsonRootObject.getJSONObject("response");

            JSONArray jsonRowsArray = jsonObject.optJSONArray("trip");
            ArrayList<TripItem> flowerList = new ArrayList<>();

            for (int i = 0; i < jsonRowsArray.length(); i++) {


                JSONObject obj = jsonRowsArray.getJSONObject(i);

                TripItem trip = new TripItem();
                trip.setTrip_name(obj.optString("trip_name").toString());
                trip.setVisibility(Integer.parseInt(obj.optString("visibility").toString()));
                trip.setTrip_description(obj.optString("trip_details"));
                trip.setTrip_id(obj.optString("id"));

                flowerList.add(trip);


            }

            return flowerList;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
