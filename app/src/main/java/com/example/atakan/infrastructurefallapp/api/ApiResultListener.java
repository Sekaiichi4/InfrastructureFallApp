package com.example.atakan.infrastructurefallapp.api;

public interface ApiResultListener {
    void requestSuccessfull(String data);
    void requestFailed(String message);
}

