package com.locname.distribution.parsers;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.locname.distribution.model.Flower;

public class FlowerJSONParser {
	
	public static List<Flower> parseFeed(String content) {
	
		try {
			JSONObject  jsonRootObject = new JSONObject(content);//done
			//Get the instance of JSONArray that contains JSONObjects
			JSONArray jsonRowsArray = jsonRootObject.optJSONArray("rows");
			List<Flower> flowerList = new ArrayList<>();
			
			for (int i = 0; i < jsonRowsArray.length(); i++) {
				
				JSONObject obj = jsonRowsArray.getJSONObject(i);
				//Get the instance of JSONArray that contains JSONObjects
				JSONArray jsonElementsArray = obj.optJSONArray("elements");
				for (int j = 0; j < jsonElementsArray.length() ; j++) {
					JSONObject obj1 = jsonElementsArray.getJSONObject(j);
					JSONObject obj11 = obj1.optJSONObject("distance");
					JSONObject obj12 = obj1.optJSONObject("duration");
				    Flower flower = new Flower();
					flower.setDistance(obj11.getString("text"));
					flower.setDuration(obj12.getString("text"));
					flowerList.add(flower);

				}



			}
			
			return flowerList;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
