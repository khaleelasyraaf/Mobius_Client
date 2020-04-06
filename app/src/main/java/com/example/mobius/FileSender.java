package com.example.mobius;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

public class FileSender extends AsyncTask<String, Boolean, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
        String zipPath = params[0];
        String zipName = params[1];
        // TODO put ip in env-variable
        String serverUrl = "http://IP:Port"+"/files";
        File file = new File(zipPath+zipName);
        Log.d("File name", "zipName: "+zipName+" file.getName(): "+file.getName());
        RequestBody postBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("zip", file.getName(),
                        RequestBody.create(MediaType.parse("application/zip"), file))
                .build();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(postBody)
                // TODO insert API-key here
                .addHeader("API-key", "<API-Key>")
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.d("SendToServer", "Worked: "+response.body().string());
            return true;
        } catch (IOException e) {
            Log.d("SendToServer", "Error: "+e.toString());
            e.printStackTrace();
            return false;
        }

    }
}
