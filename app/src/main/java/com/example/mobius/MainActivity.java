package com.example.mobius;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;


public class MainActivity extends AppCompatActivity implements SensorEventListener, DialogID.DialogIDListener {

    // UI controls
    private Button myAddIdBtn, myUploadBtn;
    private ToggleButton myStartStopToggle;

    private TextView myTextID;

    private Switch switchWalk, switchBike, switchTrainBus, switchCar;
    private ImageView walkIcon, bikeIcon, trainIcon, busIcon, carIcon;

    public static final String SHARED_PREFS = "sharedPrefs";

    public static final String SWITCH_WALK = "switchWalk";
    public static final String SWITCH_BIKE = "switchBike";
    public static final String SWITCH_TRAIN_BUS = "switchTrainBus";
    public static final String SWITCH_CAR = "switchCar";

    public static final String TOGGLE_START_STOP = "toggleStartStop";

    private Chronometer myChronometer;
    private long pauseOffset;
    private boolean chronometerRunning;

    private SensorManager SM;
    Sensor myAccelerometer, myGyroscope;
    Context context;

    private Handler mHandler = new Handler();

    private SimpleLocation mLocation;

    private Boolean switchWalkOnOff, switchBikeOnOff, switchTrainBusOnOff, switchCarOnOff,
            toggleStartStopOnOff;

    boolean IsDataRequested = false;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    Date today = new Date();
    String currentDatetime = format.format(today);

    String FILENAME1;
    String FILENAME2;
    String FILENAME3;

    boolean isIDgiven;

    private String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String dataPath = SDPath + "/Mobius/data/";
    private String zipPath = SDPath + "/Mobius/zip/";
    private String unzipPath = SDPath + "/Mobius/unzip/";


    /** Called when the activity is created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "onCreate");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
//        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        setContentView(R.layout.activity_main);
        context = this;

        // Create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);
        // Accelerometer sensor
        myAccelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Gyroscope sensor
        myGyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mLocation = new SimpleLocation(this);
        mLocation.setBlurRadius(5);

        if (!mLocation.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(this);
        }

        //region Widgets

        //UI widgets
        myStartStopToggle = (ToggleButton)findViewById(R.id.toggleStartStopBtn);
        myAddIdBtn = (Button) findViewById(R.id.buttonAddID);
        myUploadBtn = (Button) findViewById(R.id.buttonUpload);

        myTextID = (TextView) findViewById(R.id.textID);

        switchWalk = (Switch)findViewById(R.id.switchButtonWalk);
        switchBike = (Switch)findViewById(R.id.switchButtonBike);
        switchTrainBus = (Switch)findViewById(R.id.switchButtonTrainBus);
        switchCar = (Switch)findViewById(R.id.switchButtonCar);

        walkIcon = (ImageView)findViewById(R.id.walkIcon);
        bikeIcon = (ImageView)findViewById(R.id.bikeIcon);
        trainIcon = (ImageView)findViewById(R.id.trainIcon);
        busIcon = (ImageView)findViewById(R.id.busIcon);
        carIcon = (ImageView)findViewById(R.id.carIcon);

        myChronometer = findViewById(R.id.chronometer);

        //endregion

        myStartStopToggle.setOnClickListener(myStartStopClickHandler);

        //region Switches
        switchWalk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeSliders(isChecked, "Walking");
            }
        });
        switchBike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeSliders(isChecked, "Biking");
            }
        });
        switchTrainBus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeSliders(isChecked, "Train/Bus");
            }
        });
        switchCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeSliders(isChecked, "Car");
            }
        });
        //endregion

        FILENAME1 = currentDatetime + "_sensors.csv";
        FILENAME2 = currentDatetime + "_gps.csv";
        FILENAME3 = currentDatetime + "_selfreport.csv";

        isIDgiven = false;

        myUploadBtn.setEnabled(false);

        switchWalk.setEnabled(false);
        switchBike.setEnabled(false);
        switchTrainBus.setEnabled(false);
        switchCar.setEnabled(false);
    }


    private View.OnClickListener myStartStopClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (myStartStopToggle.isChecked())
            {
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(TOGGLE_START_STOP, true);
                editor.apply();

                startEverything();
                saveToggle();

                myUploadBtn.setEnabled(false);
            }
            else
            {
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(TOGGLE_START_STOP, false);
                editor.apply();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setTitle("Are you sure?");
                builder.setMessage("Do you want to stop the sensors?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopEverything();
                                saveToggle();
                                myUploadBtn.setEnabled(true);
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myStartStopToggle.setChecked(true);
                        myUploadBtn.setEnabled(false);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    };


    private void changeSliders(boolean isChecked, String mode){
        Date today = new Date();
        String dateToStr = format.format(today);

        if (isChecked)
        {
            // Disable all sliders
            switchBike.setEnabled(false);
            switchTrainBus.setEnabled(false);
            switchCar.setEnabled(false);
            switchWalk.setEnabled(false);
            // Grey Icons out
            bikeIcon.setColorFilter(Color.argb(150,200,200,200));
            trainIcon.setColorFilter(Color.argb(150,200,200,200));
            busIcon.setColorFilter(Color.argb(150,200,200,200));
            carIcon.setColorFilter(Color.argb(150,200,200,200));
            walkIcon.setColorFilter(Color.argb(150,200,200,200));
        }
        else
        {
            // Enable the sliders
            switchBike.setEnabled(true);
            switchTrainBus.setEnabled(true);
            switchCar.setEnabled(true);
            switchWalk.setEnabled(true);
            // Colorize icons
            bikeIcon.clearColorFilter();
            trainIcon.clearColorFilter();
            busIcon.clearColorFilter();
            carIcon.clearColorFilter();
            walkIcon.clearColorFilter();

        }
        // Change only the one that was selected back on
        if (isChecked){
            switch (mode){
                case "Walking":
                    switchWalk.setEnabled(true);
                    walkIcon.clearColorFilter();
                    break;
                case "Biking":
                    switchBike.setEnabled(true);
                    bikeIcon.clearColorFilter();
                    break;
                case "Car":
                    switchCar.setEnabled(true);
                    carIcon.clearColorFilter();
                    break;
                case "Train/Bus":
                    switchTrainBus.setEnabled(true);
                    busIcon.clearColorFilter();
                    trainIcon.clearColorFilter();
                    break;
            }
        }

//        Toast.makeText(MainActivity.this, mode + " " + isChecked, Toast.LENGTH_SHORT).show();
        Log.d("Transport Mode", mode + " " + isChecked);
        String walkData = dateToStr + ", " + mode + ", " + isChecked;
        FileHelper.saveToFile(dataPath, walkData, FILENAME3);
        saveSelfReportSwitches();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "onStart");
        Log.d("ID", myTextID.getText().toString());

        // Checks textView for user ID
        isIDgiven = !myTextID.getText().toString().equals("");
        Log.d("ID", "" + isIDgiven);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "onPause");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "onResume");

        loadSelfReportSwitches();
        loadToggle();
        updateViews();
    }

//    Disable back button to avoid onStop() and onDestroy()
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed()
    {
         //super.onBackPressed();
    }

    boolean filesZipped = false;
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "onDestroy");
    }

    public void saveSelfReportSwitches() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(SWITCH_WALK, switchWalk.isChecked());
        editor.putBoolean(SWITCH_BIKE, switchBike.isChecked());
        editor.putBoolean(SWITCH_TRAIN_BUS, switchTrainBus.isChecked());
        editor.putBoolean(SWITCH_CAR, switchCar.isChecked());

        editor.apply();
    }

    public void loadSelfReportSwitches() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        switchWalkOnOff = sharedPreferences.getBoolean(SWITCH_WALK, false);
        switchBikeOnOff = sharedPreferences.getBoolean(SWITCH_BIKE, false);
        switchTrainBusOnOff = sharedPreferences.getBoolean(SWITCH_TRAIN_BUS, false);
        switchCarOnOff = sharedPreferences.getBoolean(SWITCH_CAR, false);

    }

    public void saveToggle() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(TOGGLE_START_STOP, myStartStopToggle.isChecked());

        editor.apply();
    }

    public void loadToggle() {
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        myStartStopToggle.setChecked(sharedPrefs.getBoolean(TOGGLE_START_STOP, false));
    }


    public void updateViews() {
        switchWalk.setChecked(switchWalkOnOff);
        switchBike.setChecked(switchBikeOnOff);
        switchTrainBus.setChecked(switchTrainBusOnOff);
        switchCar.setChecked(switchCarOnOff);

        //myStartStopToggle.setChecked(toggleStartStopOnOff);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    // Obtain the sensor data from the phone
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (IsDataRequested) {
            String sensorName = "Unknown";
            String sensorNameShort = "UnknownShort";

            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                sensorName = "Accelerometer";
                sensorNameShort = "Acc";

                IsDataRequested = false;
            }
            else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                sensorName = "Gyroscope";
                sensorNameShort = "Gyro";

                IsDataRequested = false;
            }

            Log.d(sensorName, sensorNameShort+"_X:" + event.values[0] +
                    " "+sensorNameShort+"_Y:" + event.values[1] +
                    " "+sensorNameShort+"_Z:" + event.values[2]);

            saveSensorData(event);
        }

    }

    public void startService() {
        Intent serviceIntent = new Intent(this, AppService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, AppService.class);
        stopService(serviceIntent);
    }

    private Runnable mGPSRunnable = new Runnable() {
        @Override
        public void run() {
            final double latitude = mLocation.getLatitude();
            final double longitude = mLocation.getLongitude();
            //Toast.makeText(MainActivity.this, "Latitude: " + latitude + ", Longitude" + longitude, Toast.LENGTH_SHORT).show();
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

    private final Runnable mSensorsRunnable = new Runnable() {
        @Override
        public void run() {
            IsDataRequested = true;
            mHandler.postDelayed(this, 17);
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

        Date today = new Date();
        String dateToStr = format.format(today);

        String sensorData = dateToStr+","+Acc_X+","+Acc_Y+","+Acc_Z+","+Gyro_X+","+Gyro_Y+","+Gyro_Z;
        FileHelper.saveToFile(dataPath, sensorData, FILENAME1);
    }

    public void saveGPSData() {
        final double latitude = mLocation.getLatitude();
        final double longitude = mLocation.getLongitude();

        Date today = new Date();
        String dateToStr = format.format(today);

        String gpsData = dateToStr+","+latitude+","+longitude;
        FileHelper.saveToFile(dataPath, gpsData, FILENAME2);
    }

    // Button that sends the sensor data when clicked
    public void startEverything() {

        if (isIDgiven) {
            // Create folders (if they don't exist already)
            File file = new File(dataPath+FILENAME1);

            if(!file.exists()) {
                String sensorNameList = "Time, Acc_x, Acc_y, Acc_z, Gyro_x, Gyro_y, Gyro_z";
                String gpsNameList = "Time, Latitude, Longitude";
                String selfreportNameList = "Time, Transportation Mode, Status";
                FileHelper.saveToFile(dataPath, sensorNameList, FILENAME1);
                FileHelper.saveToFile(dataPath, gpsNameList, FILENAME2);
                FileHelper.saveToFile(dataPath, selfreportNameList, FILENAME3);
            }

            // Register sensor listener
            SM.registerListener(MainActivity.this, myAccelerometer, SENSOR_DELAY_GAME); // SensorManager.SENSOR_DELAY_FASTEST , SENSOR_DELAY_GAME, SENSOR_DELAY_NORMAL
            SM.registerListener(MainActivity.this, myGyroscope, SENSOR_DELAY_GAME);

            IsDataRequested = true;

            switchWalk.setEnabled(true);
            switchBike.setEnabled(true);
            switchTrainBus.setEnabled(true);
            switchCar.setEnabled(true);

            startSensors();
            startGPS();
            startService();

            startChronometer();

            Log.d("Sensors", "Sensors Button Pressed");
//        Toast.makeText(MainActivity.this, "Sensors activated", Toast.LENGTH_SHORT).show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setTitle("ID needed");
            builder.setMessage("Please key in your user ID.");
            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

            myStartStopToggle.setChecked(false);
            Log.d("ID", "Not given");
        }
    }

    public void stopEverything() {
        IsDataRequested = false;
        SM.unregisterListener(MainActivity.this);
        stopGPS();
        stopService();

        pauseChronometer();

        Log.d("Sensors", "Sensors stopped");
    }

    public void startChronometer() {
        if(!chronometerRunning) {
            myChronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            myChronometer.start();
            chronometerRunning = true;
        }
    }

    public void pauseChronometer() {
        if(chronometerRunning) {
            myChronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - myChronometer.getBase();
            chronometerRunning = false;
        }
    }

    public void resetChronometer() {
        myChronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }

    public void addID(View v) {
        openDialog();
    }

    public void openDialog() {
        DialogID dialogID = new DialogID();
        dialogID.show(getSupportFragmentManager(), "id dialog");
    }

    @Override
    public void applyTexts(String userID) {
        myTextID.setText(userID);

        isIDgiven = !myTextID.getText().toString().equals("");

        if (isIDgiven) {

            FILENAME1 = "ID_" + myTextID.getText().toString() + "_" + FILENAME1;
            FILENAME2 = "ID_" + myTextID.getText().toString() + "_" + FILENAME2;
            FILENAME3 = "ID_" + myTextID.getText().toString() + "_" + FILENAME3;

            Log.d("ID", "" + myTextID.getText().toString() + "_" + isIDgiven);
            Toast.makeText(MainActivity.this, "Confirmed.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Log.d("ID", "No ID given");
        }
    }

    public void uploadFiles(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("Done for today?");
        builder.setMessage("The files will be uploaded.");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String zipName = "ID_" + myTextID.getText().toString() + "_" + currentDatetime + ".zip";
                        if (FileHelper.zip(dataPath, zipPath, zipName, filesZipped)){
                            // TODO DONT REMEMBER TO ACTIVATE THIS AGAIN
//                          new FileSender().execute(zipPath, zipName, myTextID.getText().toString());
                            // delete Files in data Folder (they just got zipped)
                            java.io.File files = new java.io.File(dataPath);
                            java.io.File[] fileList = files.listFiles();
                            for (java.io.File file : fileList) {
                                file.delete();
                            }
                            Log.d("Delete", "Data Files deleted");
                        }
                        resetChronometer();
                        myUploadBtn.setEnabled(false);
                        Toast.makeText(MainActivity.this, "Files have been uploaded.", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
