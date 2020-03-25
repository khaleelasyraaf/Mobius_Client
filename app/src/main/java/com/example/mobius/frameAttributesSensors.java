package com.example.mobius;

import org.json.JSONException;
import org.json.JSONObject;

public class frameAttributesSensors extends JSONObject {

    public frameAttributesSensors(float Acc_X, float Acc_Y, float Acc_Z, float Gyro_X, float Gyro_Y, float Gyro_Z)
    {

        try {
            put("Acc_X", Acc_X);
            put("Acc_Y", Acc_Y);
            put("Acc_Z", Acc_Z);
            put("Gyro_Y", Gyro_Y);
            put("Gyro_X", Gyro_X);
            put("Gyro_Z", Gyro_Z);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
