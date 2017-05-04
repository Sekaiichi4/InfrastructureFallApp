package com.example.atakan.infrastructurefallapp.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class Config {

    private final String emailFrom = "your@phone.com";
    private final String nameFrom = "[Broken] Your phone";
    private String emailTo;
    private String title;
    private String message;
    private boolean enabled;
    private int sensitivity;

    private static SharedPreferences sharedPref;
    private static Gson gson;
    private static final String CONFIG_PREFERENCE_TAG = "FallConfig";
    private static final String CONFIG_DATA_TAG = "FallConfigData";

    private Config() {
        emailTo = "";
        title = "Your phone is broken";
        message = "Hello. I'm your phone and I'm broken.";
        enabled = false;
        sensitivity = 30;
    }

    /**
     * Gets singleton instance
     *
     * @param context Activity that asked for instance
     * @return Config instance
     */
    public static Config getInstance(Context context) {
        // Load shared preferences and gson instance
        sharedPref = context.getSharedPreferences(CONFIG_PREFERENCE_TAG, Context.MODE_PRIVATE);
        gson = new Gson();
        String json = sharedPref.getString(CONFIG_DATA_TAG, "");

        // Converts json to Class instance
        Config u = gson.fromJson(json, Config.class);

        // If the instance does not exist in SP then create it
        if( u == null ) {
            u = new Config();
        }
        return u;
    }

    /**
     * Save config instance into shared pref
     *
     * @param c Activity that asked to save instance
     */
    public static void saveInstance(Config c) {
        SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
        String mUserData = gson.toJson(c);

        sharedPrefEditor.putString(CONFIG_DATA_TAG, mUserData);
        sharedPrefEditor.apply();
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public String getNameFrom() {
        return nameFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }
}
