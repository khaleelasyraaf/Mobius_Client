package com.example.mobius;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.util.Log;
import static android.content.Context.LOCATION_SERVICE;

public class GPStracker extends AppCompatActivity implements LocationListener {
    Context context;

    public GPStracker(Context context){
        super();
        this.context = context;
    }

    public Location getLocation() {

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnabled) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, this);
        } else {
            Toast.makeText(context, "Please enable GPS", Toast.LENGTH_LONG).show();
        }
        } catch (Exception e) {
            e.printStackTrace();
            }
        return null;
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Log.d("Geo_Location", "Latitude" + lat + ", Longitude" + lon);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }
}
