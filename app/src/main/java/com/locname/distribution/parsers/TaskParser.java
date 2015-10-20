package com.locname.distribution.parsers;

import com.locname.distribution.model.TaskItem;
import com.locname.distribution.model.TripItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mostafa on 9/28/2015.
 */
public class TaskParser {
    public static ArrayList<TaskItem> parseFeed(String content) {

        try {
            JSONObject jsonRootObject = new JSONObject(content);//done
            //Get the instance of JSONArray that contains JSONObjects
            int status = Integer.parseInt(jsonRootObject.optString("status").toString());

            if(status != 200){

                return null;

            }else {



                JSONArray jsonRowsArray = jsonRootObject.optJSONArray("response");
                ArrayList<TaskItem> flowerList = new ArrayList<>();

                for (int i = 0; i < jsonRowsArray.length(); i++) {


                    JSONObject obj = jsonRowsArray.getJSONObject(i);

                    TaskItem trip = new TaskItem();
                    trip.setTask_name(obj.optString("place_name").toString());
                    trip.setTask_id(obj.optString("id").toString());
                    trip.setStatus(Integer.parseInt(obj.optString("status").toString()));
                    trip.setTask_details(obj.optString("place_details"));
                    trip.setLat(Double.parseDouble(obj.optString("latitude").toString()));
                    trip.setLng(Double.parseDouble(obj.optString("longtude").toString()));


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
