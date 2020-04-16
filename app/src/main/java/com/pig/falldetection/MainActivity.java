package com.pig.falldetection;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private boolean isActive = true;
    private boolean fallDetected = false;
    ImageView fallPersonIcon;
    TextView statusText;
    Button toggleButton;
    Sensor accelerometer;
    int time;
    AlertDialog alertDialog;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarColor();
        setContentView(R.layout.activity_main);
        fallPersonIcon = findViewById(R.id.fallPersonIcon);
        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);
        isActive = State.instance.getDetectionStatus();
        setFallIcon();

        toggleButton.setOnClickListener(v -> {
            isActive = !isActive;
            State.instance.toggleDetectionStatus();
            setFallIcon();
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setFallIcon() {
        if (isActive) {
            toggleButton.setText("OPRESTE DETECTIA");
            toggleButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0);
            fallPersonIcon.setImageResource(R.drawable.fall_person_darkgreen);
            statusText.setText("DETECTIA ESTE PORNITA");
        } else {
            toggleButton.setText("PORNESTE DETECTIA");
            toggleButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
            fallPersonIcon.setImageResource(R.drawable.fall_person);
            statusText.setText("DETECTIA ESTE OPRITA");
        }
    }

    private void setActionBarColor() {
        ActionBar actionBar;
        actionBar = getSupportActionBar();

        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#004ba0"));

        actionBar.setBackgroundDrawable(colorDrawable);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        double a = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        double g = 9.81;
        double G = a / g;
        double maxThreshold = 3.19;
        double minThreshold = 1.02;

        if (a < maxThreshold && a > minThreshold && isActive && !fallDetected) {
            fallDetected = true;
            fallDetected();
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

    public void sendSms() {
        ArrayList<ListItem> listItems = State.instance.getState();

        listItems.forEach(listItem -> {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(listItem.phone, null, getLocation(), null, null);
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        });
    }

    public boolean isSMSPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    public boolean isLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permisiune acceptata", Toast.LENGTH_SHORT).show();
                    //send sms here call your method
                    sendSms();
                } else {
                    Toast.makeText(this, "Permisiune refuzata", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @SuppressLint("MissingPermission")
    private String getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!isLocationPermissionGranted()) {
            return null;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, new LocationListener() {
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }

            @Override
            public void onLocationChanged(final Location location) {
            }
        });
        Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        double longitude = myLocation.getLongitude();
        double latitude = myLocation.getLatitude();
        return "https://www.google.com/maps/search/?api=1&query=" + latitude + ","+longitude;
    }

    private void fallDetected() {
        ConstraintLayout fallAlertLayout= (ConstraintLayout) getLayoutInflater().inflate(R.layout.fall_alert,  null);
        TextView timerTextView = fallAlertLayout.findViewById(R.id.timer);
        Button cancelBtn = fallAlertLayout.findViewById(R.id.im_ok_button);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        alertDialog = builder.create();
        alertDialog.setView(fallAlertLayout);
        alertDialog.show();
        time = (int) State.instance.getDuration() * 1000;

        timer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                time = time - 1000;
                timerTextView.setText(String.valueOf(time/1000+1));
            }

            public void onFinish() {
                alertDialog.cancel();
                fallDetected = false;
                startSms();
            }
        }.start();

        cancelBtn.setOnClickListener(v -> {
            alertDialog.cancel();
            timer.cancel();
        });
    }

    private void startSms() {
        if (isSMSPermissionGranted()) {
            sendSms();
        }
        Toast.makeText(getApplicationContext(), "Mesajul a fost trimis", Toast.LENGTH_SHORT).show();
    }
}
