package com.pig.falldetection;

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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.ajts.androidmads.telegrambotlibrary.Telegram;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private boolean isActive = true;
    private boolean fallDetected = false;
    private boolean timerStarted = false;
    private boolean movementDetected = false;
    ImageView fallPersonIcon;
    TextView statusText;
    Button toggleButton;
    Sensor accelerometer;
    int time;
    AlertDialog alertDialog;
    CountDownTimer timer;
    String location;

    double previousX;
    double previousY;
    double previousZ;

    double a;
    double g = 9.834;
    double gThreshold = 3.5;

    Uri ringtoneUri;
    Ringtone ringtoneSound;

    String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    Telegram telegram = new Telegram("1164943207:AAEkxLuUVIFS-PvO_1z2C1Y6u1POYWFn51Q");

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
        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);

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
        fallDetected = false;

        a = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        double maxThreshold = 3.19;
        double minThreshold = 1.02;

        if (a < maxThreshold && a > minThreshold && isActive && !fallDetected) {
            fallDetected = true;
            previousX = event.values[0];
            previousY = event.values[1];
            previousZ = event.values[2];
        }

        if (fallDetected && !timerStarted) {
            timerStarted = true;
            movementDetected = false;
            Toast.makeText(getApplicationContext(), "FALL", Toast.LENGTH_SHORT).show();
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                timer = new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        Log.i("A-VALUE", String.valueOf(a));
                        if (a < g - gThreshold || a > g + gThreshold) {
                            movementDetected = true;
                        }
                    }

                    public void onFinish() {
                        if (!movementDetected) {
                            fallDetected();
                        }
                        timerStarted = false;
                    }
                }.start();
            }, 2000);
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
                location = getLocation();
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

    public boolean isNetworkPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 0);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 0);
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
        return "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
    }

    private void fallDetected() {
        ConstraintLayout fallAlertLayout= (ConstraintLayout) getLayoutInflater().inflate(R.layout.fall_alert,  null);
        TextView timerTextView = fallAlertLayout.findViewById(R.id.timer);
        Button cancelBtn = fallAlertLayout.findViewById(R.id.im_ok_button);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        alertDialog = builder.create();
        alertDialog.setView(fallAlertLayout);
        alertDialog.show();


        if (ringtoneSound != null) {
            ringtoneSound.play();
        }
        time = (int) State.instance.getDuration() * 1000;

        timer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                time = time - 1000;
                timerTextView.setText(String.valueOf(time/1000+1));
            }

            public void onFinish() {
                alertDialog.cancel();
                fallDetected = false;
                if (ringtoneSound != null) {
                    ringtoneSound.stop();
                }
                startSms();

                // Telegram
                Thread thread = new Thread(() -> {
                    try  {
                        if (isNetworkPermissionGranted()) {
                            sendMessageToTelegram();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        }.start();

        cancelBtn.setOnClickListener(v -> {
            alertDialog.cancel();
            timer.cancel();
            if (ringtoneSound != null) {
                ringtoneSound.stop();
            }
        });
    }

    private void startSms() {
        if (isSMSPermissionGranted()) {
            sendSms();
        }
        Toast.makeText(getApplicationContext(), "Mesajul a fost trimis", Toast.LENGTH_SHORT).show();
    }

    private void sendMessageToTelegram() throws IOException {
        String locationFormatted = String.format("<a href=\"%s\">Location</a>", location);
        String locEncoded = URLEncoder.encode(locationFormatted, "UTF-8");

        urlString = "https://api.telegram.org/bot1164943207:AAEkxLuUVIFS-PvO_1z2C1Y6u1POYWFn51Q/sendMessage?chat_id=-1001252607178&parse_mode=html&text=" + locEncoded;
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        StringBuilder sb = new StringBuilder();
        InputStream is = new BufferedInputStream(conn.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String inputLine = "";
        while ((inputLine = br.readLine()) != null) {
            sb.append(inputLine);
        }
    }
}
