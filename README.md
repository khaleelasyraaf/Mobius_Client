# Mobius Client App
This is an Android application for recording Sensor data such as Gyroscope, Accelerometer and GPS data and saving it in CSV-files. After a recording it is possible to upload the files to a server.

## How to use
1. A session is recorded by starting the app (accepting the neccessary app requirements) and entering a User-ID at the top of the app. 
    - The ID is used for naming of the sensor files and also uploading the data to a server.
2. Pressing "Start" begins the listening of the sensors. 
    - While the app is running it is displayed at the top of the app for how long the recording is running. 
    - The user can change via different sliders the current mode of transportation which creates an entry in the selfreport data CSV-file.
3. The user stops the recording by pressing the "Stop" button (the previous "Start" button).
    - He can decide to start the recording again, which will continue to record sensor readings to the previously created files. 
4. After stopping the recording the user can press the "Upload" button which zips the CSV-files and deletes the raw data files. 
    - Then the zip files are attempted to be send to the server using the User-ID and the `SERVER-URL` specified in a `env`-file under `app/assets`.


## The three CSV-files: 
 - Selfreport data (the transportation modes that can be selected via switches)
 - GPS data
 - Sensor data. 
 
They have a heading row which names the time and the sensor reading, followed by every row being an entry and therefore a sensor reading
 
## Transportation modes:
 - Walking
 - Biking
 - Bus/Train
 - Car
 
## Sensor sampling rate
By default the GPS data is collected every 10 seconds and the other sensors are sampled with 60Hz (every 17ms).
Specified in the `MainActivity.java` under `mGPSRunnable` and `mSensorsRunnable`.
 
---
### Known Issues
- GPS data shows `0.0, 0.0`
    - This is common after starting the app the first time due to the newly acquired GPS permission. Restarting the app should fix this.
  
