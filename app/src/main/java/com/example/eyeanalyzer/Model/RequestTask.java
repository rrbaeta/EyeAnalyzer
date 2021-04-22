package com.example.eyeanalyzer.Model;

import android.os.AsyncTask;
import android.util.JsonWriter;

import com.example.eyeanalyzer.Fragments.AddImageFragment;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestTask extends AsyncTask<String, Void, String> {

    private WeakReference<AddImageFragment> fragmentWeakReference;

    private static final String API_URL = "http://192.168.0.40:8080/api/v1/analyze/retina";
    //private String API_URL = "https://retina-neural-net.ew.r.appspot.com/api/v1/analyze/retina";
    private static final String BOUNDARY =  "*****";
    private String responseFinal = "At the start";

    public RequestTask(AddImageFragment addImageFragment){
        fragmentWeakReference = new WeakReference<AddImageFragment>(addImageFragment);
    }

    @Override
    protected String doInBackground(String... imagePath) {

        try {
            HttpURLConnection httpUrlConnection = createHttpConnection();
            File file = new File(imagePath[0]);

            DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
            //DataInputStream dataInputStream = new DataInputStream(httpUrlConnection.getInputStream());

            //request.writeBytes("--" + BOUNDARY + "\r\n");
            //request.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n");
            //request.writeBytes(fileDescription + "\r\n");

            request.writeBytes("--" + BOUNDARY + "\r\n");
            request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n\r\n");
            request.write(FileUtils.readFileToByteArray(file));
            request.writeBytes("\r\n");

            request.writeBytes("--" + BOUNDARY + "--\r\n");
            request.flush();
            int status = httpUrlConnection.getResponseCode();
            System.out.println(status);

            try(BufferedReader br = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                responseFinal = response.toString();
                System.out.println(responseFinal); //Here set text view to the result
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseFinal;
    }

    private HttpURLConnection createHttpConnection() throws IOException {

        HttpURLConnection httpUrlConnection = null;
        URL url = new URL(API_URL);
        httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setUseCaches(false);
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setDoInput(true);

        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
        httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
        //httpUrlConnection.setRequestProperty("Content-Type", "image/jpg");

        return httpUrlConnection;

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        AddImageFragment addImageFragment = fragmentWeakReference.get();

        if (addImageFragment == null || addImageFragment.isRemoving()) {
            return;
        } else {

            String prediction = null;

            JSONObject object = null;
            try {
                object = new JSONObject(s);
                prediction  = object.getString("prediction");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            addImageFragment.displayPrediction("Prediction is " + prediction);
        }
    }
}
