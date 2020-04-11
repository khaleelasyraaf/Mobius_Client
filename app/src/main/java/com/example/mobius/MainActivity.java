package com.example.mobius;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.internal.gmsg.HttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // UI controls
    private Button mySensorsRequestBtn;

    private Switch switchWalk, switchBike, switchTrainBus, switchCar;
    private ImageView walkIcon, bikeIcon, trainIcon, busIcon, carIcon;

    public static final String SHARED_PREFS = "sharedPrefs";

    private SensorManager SM;
    Sensor myAccelerometer, myGyroscope;
    Context context;

    private Handler mHandler = new Handler();

    private SimpleLocation mLocation;

    public static final String SWITCH_WALK = "switchWalk";
    public static final String SWITCH_BIKE = "switchBike";
    public static final String SWITCH_TRAIN_BUS = "switchTrainBus";
    public static final String SWITCH_CAR = "switchCar";

    private Boolean switchWalkOnOff, switchBikeOnOff, switchTrainBusOnOff, switchCarOnOff;

    String FILENAME1 = new Date().getTime() + "_sensors.csv";
    String FILENAME2 = new Date().getTime() + "_gps.csv";
    String FILENAME3 = new Date().getTime() + "_selfreport.csv";

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

        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);


        setContentView(R.layout.activity_main);
        context = this;

        // Create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer sensor
        myAccelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Gyroscope sensor
        myGyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Register sensor listener
        SM.registerListener(MainActivity.this, myAccelerometer, 1000000000); // SensorManager.SENSOR_DELAY_FASTEST , SENSOR_DELAY_GAME, SENSOR_DELAY_NORMAL
        SM.registerListener(MainActivity.this, myGyroscope, 1000000000);

        mLocation = new SimpleLocation(this);
        mLocation.setBlurRadius(5000);

        if (!mLocation.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(this);
        }

        //region Widgets

        //UI widgets
        mySensorsRequestBtn = (Button) findViewById(R.id.sensorsRequestBtn);

        switchWalk = (Switch)findViewById(R.id.switchButtonWalk);
        switchBike = (Switch)findViewById(R.id.switchButtonBike);
        switchTrainBus = (Switch)findViewById(R.id.switchButtonTrainBus);
        switchCar = (Switch)findViewById(R.id.switchButtonCar);

        walkIcon = (ImageView)findViewById(R.id.walkIcon);
        bikeIcon = (ImageView)findViewById(R.id.bikeIcon);
        trainIcon = (ImageView)findViewById(R.id.trainIcon);
        busIcon = (ImageView)findViewById(R.id.busIcon);
        carIcon = (ImageView)findViewById(R.id.carIcon);

        //endregion

        //Subscribe to handle the button click
        mySensorsRequestBtn.setOnClickListener(myOnSensorsRequestClickHandler);


        String sensorNameList = "Time, Acc_x, Acc_y, Acc_z, Gyro_x, Gyro_y, Gyro_z";
        String gpsNameList = "Time, Latitude, Longitude";
        String selfreportNameList = "Time, Transportation Mode, Status";
        FileHelper.saveToFile(dataPath, sensorNameList, FILENAME1);
        FileHelper.saveToFile(dataPath, gpsNameList, FILENAME2);
        FileHelper.saveToFile(dataPath, selfreportNameList, FILENAME3);

        //region Switches

        switchWalk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
                Date today = new Date();
                String dateToStr = format.format(today);

                String walkYes = "";
                String walkNo = "";

                if (isChecked)
                {
                    Toast.makeText(MainActivity.this, "On", Toast.LENGTH_SHORT).show();

                    switchBike.setEnabled(false);
                    switchTrainBus.setEnabled(false);
                    switchCar.setEnabled(false);

                    bikeIcon.setColorFilter(Color.argb(150,200,200,200));
                    trainIcon.setColorFilter(Color.argb(150,200,200,200));
                    busIcon.setColorFilter(Color.argb(150,200,200,200));
                    carIcon.setColorFilter(Color.argb(150,200,200,200));

                    walkYes = ", Walking, Yes";
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Off", Toast.LENGTH_SHORT).show();

                    switchBike.setEnabled(true);
                    switchTrainBus.setEnabled(true);
                    switchCar.setEnabled(true);

                    bikeIcon.setColorFilter(null);
                    trainIcon.setColorFilter(null);
                    busIcon.setColorFilter(null);
                    carIcon.setColorFilter(null);

                    walkNo = ", Walking, No";
                }

                String walkData = dateToStr + walkYes+walkNo;
                FileHelper.saveToFile(dataPath, walkData, FILENAME3);
                saveSelfReportData();
            }
        });

        switchBike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
                Date today = new Date();
                String dateToStr = format.format(today);

                String bikeYes = "";
                String bikeNo = "";

                if (isChecked)
                {
                    Toast.makeText(MainActivity.this, "On", Toast.LENGTH_SHORT).show();

                    switchWalk.setEnabled(false);
                    switchTrainBus.setEnabled(false);
                    switchCar.setEnabled(false);

                    walkIcon.setColorFilter(Color.argb(150,200,200,200));
                    trainIcon.setColorFilter(Color.argb(150,200,200,200));
                    busIcon.setColorFilter(Color.argb(150,200,200,200));
                    carIcon.setColorFilter(Color.argb(150,200,200,200));

                    bikeYes = ", Biking, Yes";
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Off", Toast.LENGTH_SHORT).show();

                    switchWalk.setEnabled(true);
                    switchTrainBus.setEnabled(true);
                    switchCar.setEnabled(true);

                    walkIcon.setColorFilter(null);
                    trainIcon.setColorFilter(null);
                    busIcon.setColorFilter(null);
                    carIcon.setColorFilter(null);

                    bikeNo = ", Biking, No";
                }

                String bikeData = dateToStr + bikeYes+bikeNo;
                FileHelper.saveToFile(dataPath, bikeData, FILENAME3);
                saveSelfReportData();
            }
        });

        switchTrainBus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
                Date today = new Date();
                String dateToStr = format.format(today);

                String trainbusYes = "";
                String trainbusNo = "";

                if (isChecked)
                {
                    Toast.makeText(MainActivity.this, "On", Toast.LENGTH_SHORT).show();

                    switchWalk.setEnabled(false);
                    switchBike.setEnabled(false);
                    switchCar.setEnabled(false);

                    walkIcon.setColorFilter(Color.argb(150,200,200,200));
                    bikeIcon.setColorFilter(Color.argb(150,200,200,200));
                    carIcon.setColorFilter(Color.argb(150,200,200,200));

                    trainbusYes = ", Train/Bus, Yes";
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Off", Toast.LENGTH_SHORT).show();

                    switchWalk.setEnabled(true);
                    switchBike.setEnabled(true);
                    switchCar.setEnabled(true);

                    walkIcon.setColorFilter(null);
                    bikeIcon.setColorFilter(null);
                    carIcon.setColorFilter(null);

                    trainbusNo = ", Train/Bus, No";
                }

                String trainbusData = dateToStr + trainbusYes+trainbusNo;
                FileHelper.saveToFile(dataPath, trainbusData, FILENAME3);
                saveSelfReportData();
            }
        });

        switchCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
                Date today = new Date();
                String dateToStr = format.format(today);

                String carYes = "";
                String carNo = "";

                if (isChecked)
                {
                    Toast.makeText(MainActivity.this, "On", Toast.LENGTH_SHORT).show();

                    switchWalk.setEnabled(false);
                    switchBike.setEnabled(false);
                    switchTrainBus.setEnabled(false);

                    walkIcon.setColorFilter(Color.argb(150,200,200,200));
                    bikeIcon.setColorFilter(Color.argb(150,200,200,200));
                    trainIcon.setColorFilter(Color.argb(150,200,200,200));
                    busIcon.setColorFilter(Color.argb(150,200,200,200));

                    carYes = ", Car, Yes";
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Off", Toast.LENGTH_SHORT).show();

                    switchWalk.setEnabled(true);
                    switchBike.setEnabled(true);
                    switchTrainBus.setEnabled(true);

                    walkIcon.setColorFilter(null);
                    bikeIcon.setColorFilter(null);
                    trainIcon.setColorFilter(null);
                    busIcon.setColorFilter(null);

                    carNo = ", Car, No";
                }

                String carData = dateToStr + carYes+carNo;
                FileHelper.saveToFile(dataPath, carData, FILENAME3);
                saveSelfReportData();
            }
        });

        //endregion

        loadSelfReportData();
        updateViews();

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

            Toast.makeText(MainActivity.this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
            Log.d("GPS", "Lat: " + latitude + " Long: " + longitude);

            mHandler.postDelayed(this, 10000);

            saveGPSData();

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

        String sensorData = dateToStr+","+Acc_X+","+Acc_Y+","+Acc_Z+","+Gyro_X+","+Gyro_Y+","+Gyro_Z;
        FileHelper.saveToFile(dataPath, sensorData, FILENAME1);
    }

    public void saveGPSData() {
        final double latitude = mLocation.getLatitude();
        final double longitude = mLocation.getLongitude();

        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
        Date today = new Date();
        String dateToStr = format.format(today);

        String gpsData = dateToStr+","+latitude+","+longitude;
        FileHelper.saveToFile(dataPath, gpsData, FILENAME2);
    }


    boolean filesZipped = false;
    @Override
    protected void onStop() {
        super.onStop();
        String zipName = new Date().getTime() + ".zip";
        if (FileHelper.zip(dataPath, zipPath, zipName, filesZipped)){
            Toast.makeText(MainActivity.this,"Zip successfully.",Toast.LENGTH_LONG).show();
            new FileSender().execute(zipPath, zipName);
        }
    }


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

            }
            else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                sensorName = "Gyroscope";
                sensorNameShort = "Gyro";

            }

            Log.d(sensorName, sensorNameShort+"_X:" + event.values[0] +
                    " "+sensorNameShort+"_Y:" + event.values[1] +
                    " "+sensorNameShort+"_Z:" + event.values[2]);

            Toast.makeText(MainActivity.this, "Sensors activated", Toast.LENGTH_SHORT).show();

            saveSensorData(event);
        }

    }


    // Button that sends the sensor data when clicked
    private OnClickListener myOnSensorsRequestClickHandler = new OnClickListener() {
        @Override
                public void onClick(View sensors) {

                IsDataRequested = !IsDataRequested;
                Log.d("Sensors", "Sensors Button Pressed");


        }
    };


    public void saveSelfReportData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(SWITCH_WALK, switchWalk.isChecked());
        editor.putBoolean(SWITCH_BIKE, switchBike.isChecked());
        editor.putBoolean(SWITCH_TRAIN_BUS, switchTrainBus.isChecked());
        editor.putBoolean(SWITCH_CAR, switchCar.isChecked());

        editor.apply();
    }

    public void loadSelfReportData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        switchWalkOnOff = sharedPreferences.getBoolean(SWITCH_WALK, false);
        switchBikeOnOff = sharedPreferences.getBoolean(SWITCH_BIKE, false);
        switchTrainBusOnOff = sharedPreferences.getBoolean(SWITCH_TRAIN_BUS, false);
        switchCarOnOff = sharedPreferences.getBoolean(SWITCH_CAR, false);
    }

    public void updateViews() {

        switchWalk.setChecked(switchWalkOnOff);
        switchBike.setChecked(switchBikeOnOff);
        switchTrainBus.setChecked(switchTrainBusOnOff);
        switchCar.setChecked(switchCarOnOff);

    }

}
