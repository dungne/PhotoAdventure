package com.photoadventure.photoadventure.Model;

/**
 * Created by 전소연 on 1/6/2018.
 */

public class LocationStore {
    double latitude;
    double longitude;
    String name;
    String description;
    String feature;

    public LocationStore() {
    }

    public LocationStore(double latitude, double longitude, String name, String description, String feature) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.description = description;
        this.feature = feature;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

}
