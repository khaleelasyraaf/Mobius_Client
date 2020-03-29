package com.example.mobius;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class FileSender extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        String zipPath = params[0];
        String zipName = params[1];
        // TODO put ip in env-variable
        String url = "<ip-to-server>"+"/files/"+zipName;
        String charset = "UTF-8";
        File binaryFile = new File(zipPath+zipName);
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        URLConnection connection = null;
        try {
            connection = new URL(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
            // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            // Add Header
            // TODO add proper key
            writer.append("API-key: " + "invalidKey").append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            // Send the file now
            FileInputStream inputStream = new FileInputStream(zipPath+zipName);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
            inputStream.close();

            writer.append(CRLF);
            writer.flush();

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Upload maybe worked";
    }
}
