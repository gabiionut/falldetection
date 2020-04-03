package com.pig.falldetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private boolean isActive = true;
    ImageView fallPersonIcon;
    TextView statusText;
    Button toggleButton;
    Sensor accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fallPersonIcon = findViewById(R.id.fallPersonIcon);
        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isActive = !isActive;
                if (isActive) {
                    toggleButton.setText("PAUSE FALL DETECTION");
                    ((Button) v).setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0);
                    fallPersonIcon.setImageResource(R.drawable.fall_person_darkgreen);
                    statusText.setText("Detection status: ON");
                } else {
                    toggleButton.setText("RESUME FALL DETECTION");
                    ((Button) v).setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
                    fallPersonIcon.setImageResource(R.drawable.fall_person);
                    statusText.setText("Detection status: OFF");
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Log.d(TAG, "onSensorChanged: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
        double x =  event.values[0];
        double y =  event.values[1];
        double z =  event.values[2];

        double a = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        double g = 9.81;
        double G = a/g;
        double maxThreshold = 3.19;
        double minThreshold = 1.02;
        Log.d(TAG, "G: " + G);

        if (a < maxThreshold && a > minThreshold) {
            Log.d(TAG, "Fall detected!!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public void onSettingsAction(MenuItem mi) {
        Intent myIntent = new Intent(getBaseContext(), Settings.class);
        startActivity(myIntent);
    }
}
