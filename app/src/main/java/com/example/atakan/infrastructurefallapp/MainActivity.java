package com.example.atakan.infrastructurefallapp;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import com.example.atakan.infrastructurefallapp.api.ApiService;
import com.google.android.gms.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.atakan.infrastructurefallapp.api.ApiResultListener;
import com.example.atakan.infrastructurefallapp.model.Config;
import com.example.atakan.infrastructurefallapp.model.EmailModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements SensorEventListener, ApiResultListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = "MainActivityDebug";
    private final int REQUEST_LOCATION = 100;

    private float xValue, yValue, zValue;
    private TextView xText, yText, zText;
    private Sensor mySensor;
    private SensorManager SM;
    private Config config;
    private ToggleButton mEnable;
    private EditText mEmailTo, mTitle, mMessage;
    private SeekBar mSensitivity;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load app config
        config = Config.getInstance(this);
        asignConfigWidgets();

        //Assign Widgets
        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);

        // Enable or disable accelerometer
        mEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (checkInputs()) {
                        bindListener();
                    } else {
                        mEnable.setChecked(false);
                    }
                } else {
                    unbindListener();
                }
            }
        });

        // Google API client for Location
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Location is updated every 3-5 minutes
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000 * 300);
        mLocationRequest.setFastestInterval(1000 * 180);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Bind listener if its enabled
        if (config.isEnabled()) {
            bindListener();
        }
    }

    /**
     * Loading widgets to change settings
     */
    private void asignConfigWidgets() {
        mEmailTo = (EditText) findViewById(R.id.am_email);
        mTitle = (EditText) findViewById(R.id.am_title);
        mMessage = (EditText) findViewById(R.id.am_message);
        mSensitivity = (SeekBar) findViewById(R.id.am_sensitivity);
        mEnable = (ToggleButton) findViewById(R.id.am_enable);

        // Max sensitivity is 100
        mSensitivity.setMax(100);

        updateUI();
    }

    /**
     * Reload data in widgets
     */
    private void updateUI() {
        mEmailTo.setText(config.getEmailTo());
        mTitle.setText(config.getTitle());
        mMessage.setText(config.getMessage());
        mSensitivity.setProgress(config.getSensitivity());
        mEnable.setChecked(config.isEnabled());
    }

    /**
     * All config data must be set and if they are they are saved into SharedPref
     *
     * @return true/false are all inputs correct?
     */
    private boolean checkInputs() {
        boolean result = true;
        String emailTo = mEmailTo.getText().toString();
        String title = mTitle.getText().toString();
        String message = mMessage.getText().toString();

        // Checking input fields for empty and email for validity
        if (TextUtils.isEmpty(emailTo)) {
            setErrorText(mEmailTo, getString(R.string.required));
            result = false;
        } else if (!isEmailValid(mEmailTo)) {
            setErrorText(mEmailTo, getString(R.string.wrong_email));
            result = false;
        }

        if (TextUtils.isEmpty(title)) {
            setErrorText(mTitle, getString(R.string.required));
            result = false;
        }

        if (TextUtils.isEmpty(message)) {
            setErrorText(mMessage, getString(R.string.required));
            result = false;
        }

        // Saving config
        if (result) {
            config.setEmailTo(emailTo);
            config.setTitle(title);
            config.setMessage(message);
            config.setSensitivity(mSensitivity.getProgress());
            config.setEnabled(mEnable.isChecked());
            Config.saveInstance(config);
        }

        return result;
    }

    /**
     * Function to check if the email is valid
     *
     * @param editText that should contain email
     * @return true/false if email is valid
     */
    public static boolean isEmailValid(EditText editText) {
        String email = editText.getText().toString();
        boolean isValid = false;

        // Email reg-ex
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        } else {
            editText.setError("Must be email address!");
        }
        return isValid;
    }

    /**
     * Shows error in edit texts (red color)
     *
     * @param editText In which edit text
     * @param message Message of the edit text
     */
    private void setErrorText(EditText editText, String message) {
        int RGB = Color.argb(255, 255, 0, 0);

        ForegroundColorSpan fgcspan = new ForegroundColorSpan(RGB);
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(message);
        ssbuilder.setSpan(fgcspan, 0, message.length(), 0);
        editText.setError(ssbuilder);
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

        // Try to start location updates
        if(mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    /**
     * Disable listener and location updates
     */
    private void unbindListener() {
        SM.unregisterListener(this, mySensor);
        stopLocationUpdates();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xValue = event.values[0];
        yValue = event.values[1];
        zValue = event.values[2];

        xText.setText("X: " + event.values[0]);
        yText.setText("Y: " + event.values[1]);
        zText.setText("Z: " + event.values[2]);

        if (Math.abs(xValue) > config.getSensitivity()
                || Math.abs(yValue) > config.getSensitivity()
                || Math.abs(zValue) > config.getSensitivity()) {
            Toast.makeText(this, "It Hurts Yo!!!", Toast.LENGTH_SHORT).show();

            // Disable sensor listener, save config and update UI()
            unbindListener();
            config.setEnabled(false);
            Config.saveInstance(config);
            updateUI();

            // Sending email
            sendEmail();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not gonna use now
    }

    public void sendEmail() {
        // Gets message from config and adds Location
        String message = config.getMessage();
        if (l != null) {
            message += " Last known location: " + l.getLatitude() + " (lat) and " + l.getLongitude() + "(long).";
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
    protected void onStart() {
        // Connect Google API client
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Stops updates and disconnects GAC
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    /**
     * If enabled then it tries to start location updates (needs to have persmission check)
     */
    protected void startLocationUpdates() {
        if(config.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    /**
     * Disabling location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void requestSuccessfull(String data) {
    }

    @Override
    public void requestFailed(String message) {
    }

    @Override
    public void onLocationChanged(Location location) {
        // Update location
        l = location;
    }

    /**
     * Checks if app has permissions and gets current location.
     * - After that is tries to start location updates
     */
    private void getLocation() {
        // We can now safely use the API we requested access to
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // Gets location
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permissions were granted try to get location
                getLocation();
            } else {
                // Permission was denied or request was cancelled
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

