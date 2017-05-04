package com.example.atakan.infrastructurefallapp.api;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

class ApiAsyncTask extends AsyncTask<String, Void, String> {

    private ApiResultListener apiResultListener;

    ApiAsyncTask(Context context){
        apiResultListener = (ApiResultListener) context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            switch (params[0]) {
                case "POST":
                    sendPostApi(params[1], params[2]);
                    break;
                case "GET":
                    sendGetApi(params[1]);
                    break;
                default:
                    sendPostApi(params[1], params[2]);
                    break;
            }
        } catch (IOException e) {
            apiResultListener.requestFailed("IOException in ApiAsyncTask.");
        } catch (JSONException e) {
            apiResultListener.requestFailed("JSONException in ApiAsyncTask.");
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if( result != null ) {
            apiResultListener.requestSuccessfull(result);
        } else {
            apiResultListener.requestFailed("No data returned from the server.");
        }
        super.onPostExecute(result);
    }

    /**
     * Sends POST request to the API
     *
     * @param urlString url to post data to
     * @param jsonData data to send
     * @return json string returned from the server
     * @throws IOException
     * @throws JSONException
     */
    private String sendPostApi(String urlString, String jsonData) throws IOException, JSONException {
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        HashMap<String, String> headers = ApiService.getHeaders();
        if(!headers.isEmpty()) {
            for(Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonData);
        writer.close();
        os.close();

        int responseCode = connection.getResponseCode();
        Log.d("ApiAsyncTask", "Responce code: " + responseCode);
        String result = "";

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String line;
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            result = sb.toString();
        }
        connection.disconnect();
        return result;

    }

    /**
     * Sends GET request to the API
     *
     * @param urlString url to post data to
     * @return json string from the server
     * @throws IOException
     * @throws JSONException
     */
    @NonNull
    private String sendGetApi(String urlString) throws IOException, JSONException {
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        HashMap<String, String> headers = ApiService.getHeaders();
        if(!headers.isEmpty()) {
            for(Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        InputStreamReader in = new InputStreamReader(connection.getInputStream());

        StringBuilder jsonResult = new StringBuilder();

        int read;
        char[] buff = new char[1024];

        while((read = in.read(buff)) != -1) {
            jsonResult.append(buff, 0, read);
        }

        return jsonResult.toString();
    }
}
