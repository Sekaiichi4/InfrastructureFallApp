package com.example.atakan.infrastructurefallapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.atakan.infrastructurefallapp.model.Config;
import com.example.atakan.infrastructurefallapp.services.FallingService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION = 100;
    public static final String IF_FALLING_SERVICE = "IFFallingService";

    /** Messenger for communicating with the service. */
    FallingService mService;

    private Config config;
    private ToggleButton mEnable;
    private EditText mEmailTo, mTitle, mMessage;
    private SeekBar mSensitivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load app config
        config = Config.getInstance(this);
        assignConfigWidgets();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mEnable.setEnabled(false);
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            bindEnableListener();
        }

        // Bind listener if its enabled
        if (config.isEnabled()) {
            bindFallingService();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mMessageReceiver,
                new IntentFilter(IF_FALLING_SERVICE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindFallingService();
        }
    }

    /**
     * Loading widgets to change settings
     */
    private void assignConfigWidgets() {
        mEmailTo = (EditText) findViewById(R.id.am_email);
        mTitle = (EditText) findViewById(R.id.am_title);
        mMessage = (EditText) findViewById(R.id.am_message);
        mSensitivity = (SeekBar) findViewById(R.id.am_sensitivity);
        mEnable = (ToggleButton) findViewById(R.id.am_enable);

        // Max sensitivity is 100
        mSensitivity.setMax(100);

        updateUI();
    }

    private void bindEnableListener() {
        // Enable or disable accelerometer
        mEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (checkInputs()) {
                        bindFallingService();
                    } else {
                        mEnable.setChecked(false);
                    }
                } else {
                    unbindFallingService();
                }
            }
        });
    }

    private void bindFallingService() {
        Intent i = new Intent(this, FallingService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFallingService() {
        unbindService(mConnection);
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
     * Reload data in widgets
     */
    private void updateUI() {
        mEmailTo.setText(config.getEmailTo());
        mTitle.setText(config.getTitle());
        mMessage.setText(config.getMessage());
        mSensitivity.setProgress(config.getSensitivity());
        mEnable.setChecked(config.isEnabled());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permissions were granted try to get location
                bindEnableListener();
            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(this, "This app will not work without permissions. :(", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FallingService.LocalBinder binder = (FallingService.LocalBinder) service;
            mService = binder.getService();
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            /* Service crashed or was killed */
            config.setEnabled(false);
            Config.saveInstance(config);
            updateUI();
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra("status", -1);
            config = Config.getInstance(MainActivity.this);
            updateUI();
        }
    };
}

