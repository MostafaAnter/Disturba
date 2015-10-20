package com.locname.distribution.model;

/**
 * Created by Mostafa on 9/26/2015.
 */
public class TripItem {
    private String trip_name;
    private String trip_description;
    private String trip_id;
    private int visibility;

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public String getTrip_name() {
        return trip_name;
    }

    public void setTrip_name(String trip_name) {
        this.trip_name = trip_name;
    }
    public String getTrip_description() {
        return trip_description;
    }

    public void setTrip_description(String trip_description) {
        this.trip_description = trip_description;
    }


}
