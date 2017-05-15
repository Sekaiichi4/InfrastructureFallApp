package com.example.atakan.infrastructurefallapp.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.atakan.infrastructurefallapp.MainActivity;
import com.example.atakan.infrastructurefallapp.api.ApiResultListener;
import com.example.atakan.infrastructurefallapp.api.ApiService;
import com.example.atakan.infrastructurefallapp.model.Config;
import com.example.atakan.infrastructurefallapp.model.EmailModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class FallingService extends Service implements SensorEventListener, ApiResultListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public FallingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FallingService.this;
        }
    }

    private Config config;
    private float xValue, yValue, zValue;
    private Sensor mySensor;
    private SensorManager SM;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unbindListener();
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Load app config
        config = Config.getInstance(this);

        // Google API client for Location
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        bindListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindListener();
        return START_STICKY;
    }

    /**
     * Checks if app has permissions and gets current location.
     * - After that is tries to start location updates
     */
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(mGoogleApiClient.isConnected()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
    }

    /**
     * Binds listener for Accelerometer and tries to start Location updates
     */
    private void bindListener() {
        //Create our Sensor Manager
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        //Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    /**
     * Disable listener and location updates
     */
    private void unbindListener() {
        SM.unregisterListener(this, mySensor);
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void sendEmail() {
        // Tries to get location again
        getLocation();

        // Gets message from config and adds Location
        String message = config.getMessage();
        if (mLastLocation != null) {
            message += " Last known location: " + mLastLocation.getLatitude() + " (lat) and " + mLastLocation.getLongitude() + "(long).";
        }

        // Set email headers
        Map<String, String> headers = new HashMap<>();
        headers.put("from", config.getEmailFrom());
        headers.put("from-name", config.getNameFrom());

        // Build email model
        EmailModel e = new EmailModel(
                config.getEmailTo(),
                config.getTitle(),
                message,
                headers,
                null
        );

        // Parse class to json with Gson
        Gson gson = new Gson();
        String jsonEmail = gson.toJson(e);

        // Send email
        ApiService as = new ApiService(this);
        as.sendPostApi(jsonEmail);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xValue = event.values[0];
        yValue = event.values[1];
        zValue = event.values[2];

        if (Math.abs(xValue) > config.getSensitivity()
                || Math.abs(yValue) > config.getSensitivity()
                || Math.abs(zValue) > config.getSensitivity()) {
            Toast.makeText(this, "It Hurts Yo!!!", Toast.LENGTH_SHORT).show();

            // Disable sensor listener, save config and update UI()
            unbindListener();
            config.setEnabled(false);
            Config.saveInstance(config);
            
            // Sending email
            sendEmail();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not gonna use now
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /* Api returns - After response is returned then service is stopped */
    @Override
    public void requestSuccessfull(String data) {
        sendMessageToActivity();
        stopSelf();
    }

    @Override
    public void requestFailed(String message) {
        sendMessageToActivity();
        stopSelf();
    }

    /**
     * Notify activity that the Service was stopped.
     */
    private void sendMessageToActivity() {
        Intent intent = new Intent(MainActivity.IF_FALLING_SERVICE);
        intent.putExtra("status", 0);
        sendBroadcast(intent);
    }
}
