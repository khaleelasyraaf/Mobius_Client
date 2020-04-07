package com.example.mobius;

import org.json.JSONException;
import org.json.JSONObject;

public class frameAttributesGPS extends JSONObject {

    public frameAttributesGPS(double latitude, double longitude)
    {

        try {
            put("Latitude", latitude);
            put("Longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}

