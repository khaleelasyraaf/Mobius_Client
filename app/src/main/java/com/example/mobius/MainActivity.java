package com.example.mobius;

import android.Manifest;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // UI controls
    private Button mySensorsRequestBtn;
    private TextView AccXText,AccYText, AccZText, GyroXText, GyroYText, GyroZText;

    private SensorManager SM;
    Sensor myAccelerometer, myGyroscope;
    Context context;

    private Handler mHandler = new Handler();

    private SimpleLocation mLocation;

    String FILENAME1 = new Date().getTime() + "_sensors.csv";
    String FILENAME2 = new Date().getTime() + "_gps.csv";

    private String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String dataPath = SDPath + "/Mobius/data/";
    private String zipPath = SDPath + "/Mobius/zip/";
    private String unzipPath = SDPath + "/Mobius/unzip/";


    /** Called when the activity is created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        setContentView(R.layout.activity_main);
        context = this;

        // Create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer sensor
        myAccelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Gyroscope sensor
        myGyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Register sensor listener
        SM.registerListener(MainActivity.this, myAccelerometer, 10000); // SensorManager.SENSOR_DELAY_FASTEST , SENSOR_DELAY_GAME, SENSOR_DELAY_NORMAL
        SM.registerListener(MainActivity.this, myGyroscope, 10000);

        mLocation = new SimpleLocation(this);
        mLocation.setBlurRadius(5000);

        if (!mLocation.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(this);
        }

        //region Widgets

        //UI widgets
        mySensorsRequestBtn = (Button) findViewById(R.id.sensorsRequestBtn);

        AccXText = (TextView) findViewById(R.id.AccXText);
        AccYText = (TextView) findViewById(R.id.AccYText);
        AccZText = (TextView) findViewById(R.id.AccZText);

        GyroXText = (TextView) findViewById(R.id.GyroXText);
        GyroYText = (TextView) findViewById(R.id.GyroYText);
        GyroZText = (TextView) findViewById(R.id.GyroZText);

        //endregion

        //Subscribe to handle the button click
        mySensorsRequestBtn.setOnClickListener(myOnSensorsRequestClickHandler);

        String sensorNameList = "Time, Acc_x, Acc_y, Acc_z, Gyro_x, Gyro_y, Gyro_z";
        String gpsNameList = "Time, Latitude, Longitude";
        FileHelper.saveToFile(dataPath, sensorNameList, FILENAME1);
        FileHelper.saveToFile(dataPath, gpsNameList, FILENAME2);
        startGPS();
        //stopGPS();

        //startSensors();
        //stopSensors();

    }

    private Runnable mGPSRunnable = new Runnable() {
        @Override
        public void run() {
            final double latitude = mLocation.getLatitude();
            final double longitude = mLocation.getLongitude();

            Toast.makeText(MainActivity.this, "Latitude: " + latitude + ", Longitude" + longitude, Toast.LENGTH_SHORT).show();
            Log.d("GPS", "Lat: " + latitude + " Long: " + longitude);
            mHandler.postDelayed(this, 10000);

            saveGPSData();
            //writeFileExternalStorage();
        }
    };

    public void startGPS() {
        mGPSRunnable.run();
    }

    public void stopGPS() {
        mHandler.removeCallbacks(mGPSRunnable);
    }

    private Runnable mSensorsRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, 10000);
            //stopSensors();
        }
    };

    public void startSensors() {
        mSensorsRunnable.run();
    }

    public void stopSensors() {
        mHandler.removeCallbacks(mSensorsRunnable);
    }

    public void saveSensorData(SensorEvent v) {
        float Acc_X = v.values[0];
        float Acc_Y = v.values[1];
        float Acc_Z = v.values[2];
        float Gyro_X = v.values[0];
        float Gyro_Y = v.values[1];
        float Gyro_Z = v.values[2];

        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
        Date today = new Date();
        String dateToStr = format.format(today);
//        frameAttributesSensors frameAttrs = new frameAttributesSensors(Acc_X, Acc_Y, Acc_Z, Gyro_X, Gyro_Y, Gyro_Z);
//
//        JSONObject myJsonObject = new JSONObject();
//        try {
//            myJsonObject.put("frameStamp", dateToStr);
//            myJsonObject.put("frameAttributes", frameAttrs);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        String sensorData = dateToStr+","+Acc_X+","+Acc_Y+","+Acc_Z+","+Gyro_X+","+Gyro_Y+","+Gyro_Z;
        FileHelper.saveToFile(dataPath, sensorData, FILENAME1);
    }

    public void saveGPSData() {
        final double latitude = mLocation.getLatitude();
        final double longitude = mLocation.getLongitude();

        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
        Date today = new Date();
        String dateToStr = format.format(today);
//        frameAttributesGPS frameAttrs = new frameAttributesGPS(latitude, longitude);
//
//        JSONObject myJsonObject = new JSONObject();
//        try {
//            myJsonObject.put("frameStamp", dateToStr);
//            myJsonObject.put("frameAttributes", frameAttrs);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        String gpsData = dateToStr+","+latitude+","+longitude;
        FileHelper.saveToFile(dataPath, gpsData, FILENAME2);
    }

    boolean filesZipped = false;
    @Override
    protected void onStop() {
        super.onStop();

        if (FileHelper.zip(dataPath, zipPath, new Date().getTime() + ".zip", filesZipped)){
            Toast.makeText(MainActivity.this,"Zip successfully.",Toast.LENGTH_LONG).show();
        }
    }

    /*public void writeGPSFileExternalStorage() {

        String state = Environment.getExternalStorageState();

        final double latitude = mLocation.getLatitude();
        final double longitude = mLocation.getLongitude();

        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String dateToStr = format.format(today);

        String locationString =  "\n" + dateToStr + " Latitude:" + latitude + " Longitude:" + longitude + "\n";

        String APP_PATH_SD_CARD = "/Mobius";

        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SD_CARD;

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        catch(Exception e){
        }

        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory(), FILENAME2);


        FileOutputStream outputStream;
        try {
            file.createNewFile();
            //second argument of FileOutputStream constructor indicates whether to append or create new file if one exists
            outputStream = new FileOutputStream(file, true);

            outputStream.write(locationString.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    // Obtain the sensor data from the phone
    boolean IsDataRequested = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (IsDataRequested) {
            String sensorName = "Unknown";
            String sensorNameShort = "UnknownShort";
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                sensorName = "Accelerometer";
                sensorNameShort = "Acc";
                AccXText.setText("AccX:" + event.values[0]);
                AccYText.setText("AccY:" + event.values[1]);
                AccZText.setText("AccZ:" + event.values[2]);
            }
            else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                sensorName = "Gyroscope";
                sensorNameShort = "Gyro";
                GyroXText.setText("GyroX:" + event.values[0]);
                GyroYText.setText("GyroY:" + event.values[1]);
                GyroZText.setText("GyroZ:" + event.values[2]);
            }
            Log.d(sensorName, sensorNameShort+"_X:" + event.values[0] +
                    " "+sensorNameShort+"_Y:" + event.values[1] +
                    " "+sensorNameShort+"_Z:" + event.values[2]);
//            Toast.makeText(MainActivity.this, sensorNameShort+"_X: " + event.values[0] +
//                    ", "+sensorNameShort+"_Y: " + event.values[1] +
//                    ", "+sensorNameShort+"_Z: " + event.values[2], Toast.LENGTH_SHORT).show();
            saveSensorData(event);
        }

    }



    // Button that sends the sensor data when clicked
    private OnClickListener myOnSensorsRequestClickHandler = new OnClickListener() {
        @Override
                public void onClick(View sensors) {

                IsDataRequested = !IsDataRequested;
                Log.d("Sensors", "Sensors Button Pressed");



                //GPStracker g = new GPStracker(getApplicationContext());
                //Location l = g.getLocation();


                //createJSON obj = new createJSON();
                //JSONObject jsonObject = obj.makeJSONObject();



        }
    };


}
