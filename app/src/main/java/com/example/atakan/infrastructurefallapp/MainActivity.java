package com.example.atakan.infrastructurefallapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private float xValue, yValue, zValue;
    private TextView xText, yText, zText;
    private Sensor mySensor;
    private SensorManager SM;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Create our Sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        //Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        //Assign TextView
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        xValue = event.values[0];
        yValue = event.values[1];
        zValue = event.values[2];

        xText.setText("X: " + event.values[0]);
        yText.setText("Y: " + event.values[1]);
        zText.setText("Z: " + event.values[2]);

        if(xValue > 30 || yValue > 30 || zValue > 30)
        {
            Toast.makeText(this, "It Hurts Yo!!!", Toast.LENGTH_SHORT).show();
            SendEmail();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // not gonna use now
    }

    public void SendEmail()
    {
        Intent emailIntent = null, chooser= null;

        emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("plain/text");
        emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        String[] to={"mert1996@live.nl"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Phone Fell :<");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hey sucker, your phone kinda fell...be careful next time!");
        //emailIntent.setType("message/rfc822");
        //chooser = Intent.createChooser(emailIntent, "Send Mail");
        startActivity(emailIntent);

    }

    public void SendEmail1()
    {
        Intent emailIntent = null, chooser= null;

        emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setData(Uri.parse("mailto:"));
        //emailIntent.setType("plain/text");
        //emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        String[] to={"mert1996@live.nl"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Phone Fell :<");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hey sucker, your phone kinda fell...be careful next time!");
        emailIntent.setType("message/rfc822");
        //chooser = Intent.createChooser(emailIntent, "Send Mail");
        startActivity(emailIntent);

    }

    public void SendEmail2()
    {
        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("mert1996@live.nl") +
                "?subject=" + Uri.encode("Phone Fell :<") +
                "&body=" + Uri.encode("Hey sucker, your phone kinda fell...be careful next time!");
        Uri uri = Uri.parse(uriText);

        send.setData(uri);

        startActivity(Intent.createChooser(send, "Send Email..."));

    }

}

