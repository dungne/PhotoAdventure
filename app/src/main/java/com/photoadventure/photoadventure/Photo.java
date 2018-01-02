package com.photoadventure.photoadventure;

/**
 * Created by Sam on 1/2/2018.
 */

public class Photo {
    private String mName;
    private String mURLString;

    public Photo() {
    }

    public Photo(String name, String URL) {
        mName = name;
        mURLString = URL;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getURLString() {
        return mURLString;
    }

    public void setURLString(String URLString) {
        mURLString = URLString;
    }
}
