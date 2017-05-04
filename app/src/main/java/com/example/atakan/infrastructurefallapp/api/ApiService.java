package com.example.atakan.infrastructurefallapp.api;

import android.content.Context;

import java.util.HashMap;

public class ApiService {

    private Context context;
    private final String urlString = "https://hva-infrastructure.herokuapp.com/email";

    public ApiService(Context context) {
        this.context = context;
        checkListener();
    }

    public void sendPostApi(String jsonSend) {
        ApiAsyncTask asyncTaskApi = new ApiAsyncTask(context);
        asyncTaskApi.execute("POST", urlString, jsonSend);
    }

    public void sendGetApi() {
        ApiAsyncTask asyncTaskApi = new ApiAsyncTask(context);
        asyncTaskApi.execute("GET", urlString);
    }

    static HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        return headers;
    }

    private void checkListener() {
        try {
            ApiResultListener arl = (ApiResultListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ApiResultListener.");
        }
    }
}
