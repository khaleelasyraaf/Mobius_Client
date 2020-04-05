package com.example.mobius;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileSender2 extends AsyncTask<String, Boolean, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
        String zipPath = params[0];
        String zipName = params[1];
        // TODO put ip in env-variable
        String serverUrl = "<IP:PORT>"+"/files/"+zipName;
        File file = new File(zipPath+zipName);
        Log.d("File name", "zipName: "+zipName+" file.getName(): "+file.getName());
        String charset = "UTF-8";

        // creates a unique boundary based on time stamp
        String LINE_FEED = "\r\n";
        String boundary = "===" + System.currentTimeMillis() + "===";
        URL url = null;
        try {
            url = new URL(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);    // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        // TODO Insert API-key here
        httpConn.setRequestProperty("API-key", "<API-KEY>");
        OutputStream outputStream = null;
        try {
            outputStream = httpConn.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                    true);
            // Add File
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append(
                    "Content-Disposition: form-data; name=\"" + zipName
                            + "\"; filename=\"" + zipName + "\"")
                    .append(LINE_FEED);
            writer.append(
                    "Content-Type: "
                            + "application/zip")
                    .append(LINE_FEED);
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(LINE_FEED);
            writer.flush();
            // End the request
            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();
            int status = httpConn.getResponseCode();
            Log.d("SendToServer", "Request statuscode: "+status);
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


}
