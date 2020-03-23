package com.example.mobius;

import org.json.JSONException;
import org.json.JSONObject;

public class createJSON extends JSONObject {

    public JSONObject makeJSONObject(float Acc_X, float Acc_Y, float Acc_Z, float Gyro_X, float Gyro_Y, float Gyro_Z)
    {
        JSONObject obj = new JSONObject();

        try {
            obj.put("Acc_X", Acc_X);
            obj.put("Acc_Y", Acc_Y);
            obj.put("Acc_Z", Acc_Z);
            obj.put("Gyro_Y", Gyro_Y);
            obj.put("Gyro_X", Gyro_X);
            obj.put("Gyro_Z", Gyro_Z);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

}
